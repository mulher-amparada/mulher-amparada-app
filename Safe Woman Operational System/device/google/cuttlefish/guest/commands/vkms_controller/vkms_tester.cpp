/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "vkms_tester.h"  // NOLINT(build/include_subdir)

#include <android-base/file.h>
#include <android-base/unique_fd.h>
#include <cutils/properties.h>
#include <errno.h>
#include <inttypes.h>
#include <log/log.h>
#include <sys/wait.h>
#include <unistd.h>

#include <cassert>
#include <chrono>  // NOLINT(build/c++11)
#include <cstdio>
#include <cstdlib>
#include <filesystem>  // NOLINT(build/c++17)
#include <memory>
#include <string>
#include <string_view>
#include <thread>  // NOLINT(build/c++11)
#include <unordered_map>
#include <vector>

namespace cuttlefish {
namespace vkms_controller {

const std::unordered_map<VkmsTester::DrmResource, std::string_view>
    VkmsTester::kDrmResourceBase = {
        {DrmResource::kConnector, "connectors/CONNECTOR_"},
        {DrmResource::kCrtc, "crtcs/CRTC_"},
        {DrmResource::kEncoder, "encoders/ENCODER_"},
        {DrmResource::kPlane, "planes/PLANE_"},
};

namespace {
// `/config/vkms` is the base directory for VKMS in ConfigFS. `my-vkms` is the
// chosen name of the VKMS instance which can be anything.
const std::filesystem::path kVkmsBaseDir = "/config/vkms/my-vkms";

// https://cs.android.com/android/platform/superproject/main/+/main:external/libdrm/xf86drmMode.h;l=190
enum class ConnectorStatus {
  kConnected = 1,
  kDisconnected = 2,
  kUnknown = 3,
};

// Create a map of the base directory for each resource type to maintain
bool WriteResourceConfig(std::string_view resourceBase, int index,
                         const std::string& node, const std::string& value) {
  std::filesystem::path dir =
      std::filesystem::path(kVkmsBaseDir) /
      (std::string(resourceBase) + std::to_string(index));
  std::filesystem::path path = dir / node;
  if (!android::base::WriteStringToFile(value, path.string())) {
    ALOGE("Failed to write '%s' to %s: %s", value.c_str(),
          path.string().c_str(), strerror(errno));
    return false;
  }
  return true;
}

bool LinkResources(std::string_view srcResourceBase, int srcIdx,
                   std::string_view targetResourceBase, int targetIdx) {
  std::string targetName =
      std::string(targetResourceBase) + std::to_string(targetIdx);
  std::filesystem::path srcDir =
      std::filesystem::path(kVkmsBaseDir) /
      (std::string(srcResourceBase) + std::to_string(srcIdx));
  std::filesystem::path symlinkPath = srcDir / ("possible_" + targetName);
  std::filesystem::path targetDir =
      std::filesystem::path(kVkmsBaseDir) / targetName;

  std::error_code ec;
  std::filesystem::create_symlink(targetDir, symlinkPath, ec);
  if (ec && ec != std::errc::file_exists) {
    ALOGE("Failed to create symlink at %s pointing to %s: %s",
          symlinkPath.string().c_str(), targetDir.string().c_str(),
          ec.message().c_str());
    return false;
  }
  return true;
}
}  // namespace

// static
std::unique_ptr<VkmsTester> VkmsTester::CreateWithBuilders(
    const std::vector<VkmsConnectorBuilder>& builders) {
  if (builders.empty()) {
    ALOGE(
        "Empty configuration provided. At least one connector must be "
        "specified.");
    return nullptr;
  }

  auto tester =
      std::unique_ptr<VkmsTester>(new VkmsTester(builders.size(), builders));

  if (!tester->mInitialized) {
    ALOGE("Failed to initialize VkmsTester with Builder Config");
    return nullptr;
  }

  return tester;
}

// static
void VkmsTester::ForceDeleteVkmsDir() { ShutdownAndCleanUpVkms(); }

VkmsTester::VkmsTester(size_t displaysCount,
                       const std::vector<VkmsConnectorBuilder>& builders) {
  mInitialized = ToggleHwc3(false) && ToggleVkmsAsDisplayDriver(true) &&
                 SetupDisplays(displaysCount, builders) && ToggleVkms(true) &&
                 ToggleHwc3(true);
  if (!mInitialized) {
    ALOGE("Failed to set up VKMS");
    ShutdownAndCleanUpVkms();
    return;
  }
}

bool VkmsTester::ToggleConnector(int connectorIndex, bool enable) {
  return SetConnectorStatus(connectorIndex, enable);
}

// static
bool VkmsTester::ToggleVkmsAsDisplayDriver(bool enable) {
  // Set HWC to use VKMS as the display driver.
  std::string propertyValue = enable ? "/dev/dri/card1" : "/dev/dri/card0";
  if (property_set("vendor.hwc.drm.device", propertyValue.c_str()) != 0) {
    ALOGE("Failed to set vendor.hwc.drm.device property to %s",
          propertyValue.c_str());
    return false;
  }
  ALOGI("Successfully set vendor.hwc.drm.device property");
  // On Disabling VKMS, we don't need to do anything else.
  if (!enable) {
    return true;
  }

  // Create VKMS directory if we're enabling VKMS.
  std::error_code ec;
  std::filesystem::create_directories(kVkmsBaseDir, ec);
  if (ec) {
    ALOGE("Failed to create directory %s: %s", kVkmsBaseDir.string().c_str(),
          ec.message().c_str());
    return false;
  }

  ALOGI("Successfully created directory %s or already exists",
        kVkmsBaseDir.string().c_str());
  return true;
}

bool VkmsTester::SetupDisplays(
    int displaysCount, const std::vector<VkmsConnectorBuilder>& builders) {
  bool isExplicitConfig = !builders.empty();
  if (isExplicitConfig && displaysCount != static_cast<int>(builders.size())) {
    ALOGE("Mismatch between requested displays count and builder config size");
    return false;
  }

  for (int i = 0; i < displaysCount; ++i) {
    CreateResource(DrmResource::kCrtc, i);
    SetCrtcWriteback(i, true);
    CreateResource(DrmResource::kEncoder, i);
    LinkToCrtc(DrmResource::kEncoder, i, i);

    CreateResource(DrmResource::kConnector, i);
    if (isExplicitConfig) {
      SetConnectorStatus(i, builders[i].mEnabledAtStart);
      SetConnectorType(i, builders[i].mType);
      if (builders[i].mMonitorName.type != MonitorName::Type::UNSET) {
        SetConnectorEdid(i, builders[i].mMonitorName);
      }
    } else {
      SetConnectorStatus(i, false);
      // Set connector type, eDP for first one, DP for the rest
      SetConnectorType(
          i, i == 0 ? ConnectorType::keDP : ConnectorType::kDisplayPort);
    }
    LinkConnectorToEncoder(i, i);

    int additionalOverlays =
        isExplicitConfig ? builders[i].mAdditionalOverlayPlanes : 0;
    for (int j = 0; j < 2 + additionalOverlays; ++j) {
      CreateResource(DrmResource::kPlane, mLatestPlaneId);
      // For each connector, create at least 2 planes, a primary and a cursor
      // PLUS any additional overlay planes
      PlaneType type;
      switch (j) {
        case 0:
          type = PlaneType::kCursor;
          break;
        case 1:
          type = PlaneType::kPrimary;
          break;
        default:
          type = PlaneType::kOverlay;
          break;
      }
      SetPlaneType(mLatestPlaneId, type);
      SetPlaneFormat(mLatestPlaneId, type);
      LinkToCrtc(DrmResource::kPlane, mLatestPlaneId, i);

      mLatestPlaneId++;
    }

    ALOGI("Successfully set up display %i", i);
  }

  return true;
}

// static
bool VkmsTester::ToggleVkms(bool enable) {
  std::filesystem::path path = std::filesystem::path(kVkmsBaseDir) / "enabled";
  std::string value = enable ? "1" : "0";
  if (!android::base::WriteStringToFile(value, path.string())) {
    ALOGE("Failed to toggle VKMS: %s", strerror(errno));
    return false;
  }

  ALOGI("Successfully toggled VKMS at %s", path.string().c_str());
  return true;
}

// static
bool VkmsTester::ToggleHwc3(bool enable) {
  const char* serviceName = "vendor.hwcomposer-3";
  const char* propertyName = "ctl.start";
  const char* propertyStopName = "ctl.stop";

  if (property_set(enable ? propertyName : propertyStopName, serviceName) !=
      0) {
    ALOGE("Failed to set property %s to %s",
          enable ? propertyName : propertyStopName, serviceName);
    return false;
  }

  ALOGI("Successfully set property %s to %s",
        enable ? propertyName : propertyStopName, serviceName);
  return true;
}

bool VkmsTester::CreateResource(DrmResource resource, int index) {
  std::string resourceBase = std::string(kDrmResourceBase.at(resource));
  std::filesystem::path resourceDir = std::filesystem::path(kVkmsBaseDir) /
                                      (resourceBase + std::to_string(index));
  std::error_code ec;
  std::filesystem::create_directories(resourceDir, ec);
  if (ec) {
    ALOGE("Failed to create directory %s: %s", resourceDir.string().c_str(),
          ec.message().c_str());
    return false;
  }

  return true;
}

bool VkmsTester::SetCrtcWriteback(int crtcIndex, bool enable) {
  bool success =
      WriteResourceConfig(kDrmResourceBase.at(DrmResource::kCrtc), crtcIndex,
                          "writeback", enable ? "1" : "0");
  if (success) {
    ALOGI("Successfully toggled writeback for CRTC %i: %s", crtcIndex,
          enable ? "enabled" : "disabled");
  }
  return success;
}

bool VkmsTester::SetConnectorStatus(int index, bool enable) {
  ConnectorStatus status =
      enable ? ConnectorStatus::kConnected : ConnectorStatus::kDisconnected;
  bool success =
      WriteResourceConfig(kDrmResourceBase.at(DrmResource::kConnector), index,
                          "status", std::to_string(static_cast<int>(status)));
  if (success) {
    ALOGI("Successfully toggled connector %i: %s", index,
          enable ? "connected" : "disconnected");
  }
  return success;
}

bool VkmsTester::SetConnectorType(int index, ConnectorType type) {
  bool success =
      WriteResourceConfig(kDrmResourceBase.at(DrmResource::kConnector), index,
                          "type", std::to_string(static_cast<int>(type)));
  if (success) {
    ALOGI("Successfully set connector %i type to %i", index,
          static_cast<int>(type));
  }
  return success;
}

bool VkmsTester::SetConnectorEdid(int index, MonitorName monitorName) {
  std::vector<uint8_t> edidData = getBinaryEdidForMonitor(monitorName);
  if (edidData.empty()) {
    ALOGE("Failed to get EDID data for monitor");
    return false;
  }

  std::filesystem::path connectorDir =
      std::filesystem::path(kVkmsBaseDir) /
      (std::string(kDrmResourceBase.at(DrmResource::kConnector)) +
       std::to_string(index));
  std::filesystem::path edidPath = connectorDir / "edid";

  android::base::unique_fd fd(
      open(edidPath.c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0644));
  if (fd.get() == -1) {
    ALOGE("Failed to open EDID file for writing: %s", strerror(errno));
    return false;
  }

  bool success =
      android::base::WriteFully(fd.get(), edidData.data(), edidData.size());

  if (success) {
    ALOGI("Successfully wrote EDID data with size %" PRIu64 " to connector %i",
          static_cast<uint64_t>(edidData.size()), index);
  } else {
    ALOGE("Failed to write complete EDID data: %s", strerror(errno));
  }

  return success;
}

bool VkmsTester::SetPlaneType(int index, PlaneType type) {
  bool success =
      WriteResourceConfig(kDrmResourceBase.at(DrmResource::kPlane), index,
                          "type", std::to_string(static_cast<int>(type)));
  if (success) {
    ALOGI("Successfully set plane %i type to %i", index,
          static_cast<int>(type));
  }
  return success;
}

bool VkmsTester::SetPlaneFormat(int index, PlaneType type) {
  std::string formats;
  if (type == PlaneType::kCursor) {
    formats = "ARGB8888";
  } else {
    formats = "AR24,AB24,XR24,XB24,RG16";
  }
  bool success = WriteResourceConfig(kDrmResourceBase.at(DrmResource::kPlane),
                                     index, "supported_formats", formats);
  if (success) {
    ALOGI("Successfully set plane %i format", index);
  }
  return success;
}

bool VkmsTester::LinkToCrtc(DrmResource resource, int resourceIdx,
                            int crtcIdx) {
  bool success =
      LinkResources(kDrmResourceBase.at(resource), resourceIdx,
                    kDrmResourceBase.at(DrmResource::kCrtc), crtcIdx);
  if (success) {
    ALOGI("Successfully linked resource %i to CRTC %i", resourceIdx, crtcIdx);
  }
  return success;
}

bool VkmsTester::LinkConnectorToEncoder(int connectorIdx, int encoderIdx) {
  bool success =
      LinkResources(kDrmResourceBase.at(DrmResource::kConnector), connectorIdx,
                    kDrmResourceBase.at(DrmResource::kEncoder), encoderIdx);
  if (success) {
    ALOGI("Successfully linked connector %i to encoder %i", connectorIdx,
          encoderIdx);
  }
  return success;
}

// static
// ConfigFS has special rules about deletion, so we need to clean up manually
// every layer.
void VkmsTester::ShutdownAndCleanUpVkms() {
  ToggleVkms(false);
  // Give the kernel a longer time to release resources
  std::this_thread::sleep_for(std::chrono::milliseconds(500));

  // Clean up manually created relationships first under
  // possible_(crtcs/encoders). This is required before we started cleaning up
  // the directories.
  FindAndCleanupPossibleLinks(kVkmsBaseDir);
  CleanUpDirAndChildren(kVkmsBaseDir);

  ToggleVkmsAsDisplayDriver(false);
}

// static
void VkmsTester::FindAndCleanupPossibleLinks(const std::string& dirPath) {
  std::error_code ec;
  if (!std::filesystem::exists(dirPath, ec) ||
      !std::filesystem::is_directory(dirPath, ec)) {
    return;
  }

  for (const auto& entry : std::filesystem::directory_iterator(dirPath, ec)) {
    if (entry.is_directory(ec)) {
      std::string dirname = entry.path().filename().string();
      // If this is a "possible_*" directory, process it specially
      if (dirname.find("possible_") == 0) {
        for (const auto& subEntry :
             std::filesystem::directory_iterator(entry.path(), ec)) {
          std::filesystem::remove(subEntry.path(), ec);
        }
        std::filesystem::remove(entry.path(), ec);
      } else {
        FindAndCleanupPossibleLinks(entry.path().string());
      }
    }
  }
}

// static
void VkmsTester::CleanUpDirAndChildren(const std::string& dirPath) {
  std::error_code ec;
  if (!std::filesystem::exists(dirPath, ec) ||
      !std::filesystem::is_directory(dirPath, ec)) {
    return;
  }

  for (const auto& entry : std::filesystem::directory_iterator(dirPath, ec)) {
    if (entry.is_directory(ec)) {
      CleanUpDirAndChildren(entry.path().string());
    }
  }
  std::filesystem::remove(dirPath, ec);
}

}  // namespace vkms_controller
}  // namespace cuttlefish

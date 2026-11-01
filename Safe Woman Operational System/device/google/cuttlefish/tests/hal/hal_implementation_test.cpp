/*
 * Copyright (C) 2019 The Android Open Source Project
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

#include <aidl/metadata.h>
#include <android-base/logging.h>
#include <android-base/properties.h>
#include <android-base/strings.h>
#include <android/content/pm/IPackageManagerNative.h>
#include <binder/IServiceManager.h>
#include <gtest/gtest.h>
#include <vintf/VintfObject.h>

#include <algorithm>
#include <cstddef>
#include <map>
#include <mutex>
#include <set>
#include <string>
#include <vector>

#ifdef AIDL_USE_UNFROZEN
constexpr bool kAidlUseUnfrozen = true;
#else
constexpr bool kAidlUseUnfrozen = false;
#endif

using namespace android;

struct VersionedAidlPackage {
  std::string name;
  size_t version;
  int bugNum;
  std::string instance;
  bool operator<(const VersionedAidlPackage& rhs) const {
    return (name < rhs.name || (name == rhs.name && version < rhs.version));
  }
};

static const std::set<std::string> kPhoneOnlyAidl = {
    "android.hardware.camera.provider",
};

static const std::set<std::string> kAutomotiveOnlyAidl = {
    /**
     * These types are only used in Android Automotive, so don't expect them
     * on phones.
     */
    "android.automotive.watchdog",
    "android.frameworks.automotive.display",
    "android.frameworks.automotive.power",
    "android.frameworks.automotive.powerpolicy",
    "android.frameworks.automotive.powerpolicy.internal",
    "android.frameworks.automotive.telemetry",
    "android.hardware.automotive.audiocontrol",
    "android.hardware.automotive.can",
    "android.hardware.automotive.evs",
    "android.hardware.broadcastradio",
    "android.hardware.automotive.occupant_awareness",
    "android.hardware.automotive.remoteaccess",
    "android.hardware.automotive.vehicle",
    "android.hardware.automotive.ivn",
    "android.hardware.macsec",
};

static const std::set<std::string> kTvOnlyAidl = {
    /**
     * These types are only used in Android TV, so don't expect them on other
     * devices.
     * TODO(b/266868403) This test should run on TV devices to enforce the same
     * requirements
     */
    "android.hardware.tv.hdmi.cec",        "android.hardware.tv.hdmi.earc",
    "android.hardware.tv.hdmi.connection", "android.hardware.tv.tuner",
    "android.hardware.tv.input",           "android.hardware.tv.mediaquality",
};

static const std::set<std::string> kRadioOnlyAidl = {
    // Not all devices have radio capabilities
    "android.hardware.radio.config",    "android.hardware.radio.data",
    "android.hardware.radio.messaging", "android.hardware.radio.modem",
    "android.hardware.radio.network",   "android.hardware.radio.sap",
    "android.hardware.radio.sim",       "android.hardware.radio.voice",
    "android.hardware.radio.ims",       "android.hardware.radio.ims.media",
    "android.hardware.radio.satellite",

};

/*
 * Always missing AIDL packages that are not served on Cuttlefish.
 * These are typically types-only packages.
 */
static const std::set<std::string> kAlwaysMissingAidl = {
    // types-only packages, which never expect a default implementation
    "android.frameworks.cameraservice.common",
    "android.frameworks.cameraservice.device",
    "android.hardware.audio.common",
    "android.hardware.audio.core.sounddose",
    "android.hardware.biometrics.common",
    "android.hardware.camera.common",
    "android.hardware.camera.device",
    "android.hardware.camera.metadata",
    "android.hardware.common",
    "android.hardware.common.fmq",
    "android.hardware.drm.common",
    "android.hardware.graphics.common",
    "android.hardware.input.common",
    "android.media.audio.common.types",
    "android.media.audio.eraser.types",
    "android.hardware.radio",
    "android.hardware.uwb.fira_android",
    "android.hardware.wifi.common",
    "android.hardware.keymaster",
    "android.hardware.automotive.vehicle.property",
    // not on Cuttlefish since it's needed only on systems using HIDL audio HAL
    "android.hardware.audio.sounddose",

    // android.hardware.media.bufferpool2 is a HAL-less interface.
    // It could be used for buffer recycling and caching by using the interface.
    "android.hardware.media.bufferpool2",

    /**
     * No implementation on cuttlefish for fastboot AIDL hal because it doesn't
     * run during normal boot, only in recovery/fastboot mode.
     */
    "android.hardware.fastboot",
    /**
     * No implementation for usb gadget HAL because cuttlefish doesn't
     * support usb gadget configfs, and currently there is no
     * plan to add this support.
     * Context: (b/130076572, g/android-idl-discuss/c/0SaiY0p-vJw/)
     */
    "android.hardware.usb.gadget",
    // Currently this HAL only implements a feature for protected VMs, and the
    // reference implementation of this HAL only works with pKVM hypervisor.
    // TODO(b/360102915): remove this after implementing no-op version of HAL
    //  for cuttlefish.
    "android.hardware.virtualization.capabilities.capabilities_service",
    // Removed in b/409403322.
    "android.hardware.health.storage",
};

/*
 * These packages should have implementations but currently do not.
 * These must be accompanied by a bug and expected to be here temporarily.
 */
static const std::vector<VersionedAidlPackage> kKnownMissingAidl = {
    // Cuttlefish Identity Credential HAL implementation is currently
    // stuck at version 3 while RKP support is being added. Will be
    // updated soon.
    {"android.hardware.identity.", 4, 266869317},
    {"android.hardware.identity.", 5, 266869317},

    {"android.se.omapi.", 2, 266870904},
    {"android.hardware.soundtrigger3.", 4, 266941225},
    {"android.media.soundtrigger.", 4, 266941225},
    {"android.hardware.weaver.", 3, 262418065},

    {"android.automotive.computepipe.registry.", 2, 273549907},
    {"android.automotive.computepipe.runner.", 2, 273549907},
    {"android.hardware.security.see.authmgr.", 1, 379940224},
    // TODO(b/466983803): Remove this after implementing Trusted Hals on CF.
    {"android.hardware.security.see.devicestate.", 1, 466983803},
    {"android.hardware.security.see.storage.", 1, 379940224},
    {"android.hardware.security.see.hwcrypto.", 1, 379940224},
    {"android.hardware.security.see.hdcp.", 1, 379940224},

    // TODO(b/477790952): Temporary to unblock presubmits
    {"android.hardware.security.timestamp.", 1, 455591722},
};

// android.hardware.foo.IFoo -> android.hardware.foo.
std::string getAidlPackage(const std::string& aidlType) {
  size_t lastDot = aidlType.rfind('.');
  CHECK(lastDot != std::string::npos);
  return aidlType.substr(0, lastDot + 1);
}

static bool isAospAidlInterface(const std::string& name) {
  return base::StartsWith(name, "android.") &&
         !base::StartsWith(name, "android.system.desktop.security.") &&
         !base::StartsWith(name, "android.desktop.security.") &&
         !base::StartsWith(name, "android.hardware.tests.") &&
         !base::StartsWith(name, "android.aidl.tests");
}

enum class DeviceType {
  UNKNOWN,
  AUTOMOTIVE,
  TV,
  WATCH,
  PHONE,
};

// As a part of AAOS-SDV, some of the services are available in cuttlefish
// target, but they are exposing services with non-standard names.
// TODO: b/493219046 - Remove this after the AIDLs for SDV has the package name
// with android prefix.
static const std::string kAaosSdvAidlPrefix = "google.sdv.";

static bool isSdvInterfaceOnAutomotive(const std::string& name,
                                       DeviceType deviceType) {
  return base::StartsWith(name, kAaosSdvAidlPrefix) &&
         deviceType == DeviceType::AUTOMOTIVE;
}

static DeviceType getDeviceType() {
  static DeviceType type = DeviceType::UNKNOWN;
  if (type != DeviceType::UNKNOWN) {
    return type;
  }

  sp<IBinder> binder =
      defaultServiceManager()->waitForService(String16("package_native"));
  sp<content::pm::IPackageManagerNative> packageManager =
      interface_cast<content::pm::IPackageManagerNative>(binder);
  CHECK(packageManager != nullptr);

  bool hasFeature = false;
  // PackageManager.FEATURE_AUTOMOTIVE
  CHECK(packageManager
            ->hasSystemFeature(String16("android.hardware.type.automotive"), 0,
                               &hasFeature)
            .isOk());
  if (hasFeature) {
    return DeviceType::AUTOMOTIVE;
  }

  // PackageManager.FEATURE_LEANBACK
  CHECK(packageManager
            ->hasSystemFeature(String16("android.software.leanback"), 0,
                               &hasFeature)
            .isOk());
  if (hasFeature) {
    return DeviceType::TV;
  }

  // PackageManager.FEATURE_WATCH
  CHECK(packageManager
            ->hasSystemFeature(String16("android.hardware.type.watch"), 0,
                               &hasFeature)
            .isOk());
  if (hasFeature) {
    return DeviceType::WATCH;
  }

  return DeviceType::PHONE;
}

static bool isMissingAidl(const std::string& packageName) {
  static std::once_flag unionFlag;
  static std::set<std::string> missingAidl = kAlwaysMissingAidl;

  std::call_once(unionFlag, [&]() {
    const DeviceType type = getDeviceType();
    switch (type) {
      case DeviceType::AUTOMOTIVE:
        missingAidl.insert(kPhoneOnlyAidl.begin(), kPhoneOnlyAidl.end());
        missingAidl.insert(kTvOnlyAidl.begin(), kTvOnlyAidl.end());
        break;
      case DeviceType::TV:
        missingAidl.insert(kAutomotiveOnlyAidl.begin(),
                           kAutomotiveOnlyAidl.end());
        missingAidl.insert(kRadioOnlyAidl.begin(), kRadioOnlyAidl.end());
        break;
      case DeviceType::WATCH:
        missingAidl.insert(kAutomotiveOnlyAidl.begin(),
                           kAutomotiveOnlyAidl.end());
        missingAidl.insert(kPhoneOnlyAidl.begin(), kPhoneOnlyAidl.end());
        missingAidl.insert(kTvOnlyAidl.begin(), kTvOnlyAidl.end());
        break;
      case DeviceType::PHONE:
        missingAidl.insert(kAutomotiveOnlyAidl.begin(),
                           kAutomotiveOnlyAidl.end());
        missingAidl.insert(kTvOnlyAidl.begin(), kTvOnlyAidl.end());
        break;
      case DeviceType::UNKNOWN:
        CHECK(false) << "getDeviceType return UNKNOWN type.";
        break;
    }
  });

  return missingAidl.find(packageName) != missingAidl.end();
}

static std::vector<VersionedAidlPackage> allAidlManifestInterfaces() {
  std::vector<VersionedAidlPackage> ret;
  auto setInserter = [&](const vintf::ManifestInstance& i) -> bool {
    if (i.format() != vintf::HalFormat::AIDL) {
      return true;  // continue
    }
    ret.push_back({i.package() + "." + i.interface(), i.version().minorVer, 0,
                   i.instance()});
    return true;  // continue
  };
  vintf::VintfObject::GetDeviceHalManifest()->forEachInstance(setInserter);
  vintf::VintfObject::GetFrameworkHalManifest()->forEachInstance(setInserter);
  return ret;
}

TEST(Hal, AllAidlInterfacesAreInAosp) {
  if (!kAidlUseUnfrozen) {
    GTEST_SKIP() << "Not valid in 'next' configuration";
  }
  if (getDeviceType() != DeviceType::PHONE &&
      getDeviceType() != DeviceType::AUTOMOTIVE) {
    GTEST_SKIP() << "Test only supports phones and automotive right now";
  }
  for (const auto& package : allAidlManifestInterfaces()) {
    // TODO: b/493219046 - Remove the check for isSdvInterfaceOnAutomotive when
    // SDV is fully upstreamed.
    EXPECT_TRUE(isAospAidlInterface(package.name) ||
                isSdvInterfaceOnAutomotive(package.name, getDeviceType()))
        << "This device should only have AOSP interfaces, not: "
        << package.name;
  }
}

TEST(Hal, NoExtensionsOnAospInterfaces) {
  if (!kAidlUseUnfrozen) {
    GTEST_SKIP() << "Not valid in 'next' configuration";
  }
  if (getDeviceType() != DeviceType::PHONE &&
      getDeviceType() != DeviceType::AUTOMOTIVE) {
    GTEST_SKIP() << "Test only supports phones and automotive right now";
  }
  for (const auto& package : allAidlManifestInterfaces()) {
    if (isAospAidlInterface(package.name)) {
      std::string instance = package.name + "/" + package.instance;
      sp<IBinder> binder =
          defaultServiceManager()->waitForService(String16(instance.c_str()));
      EXPECT_NE(binder, nullptr)
          << "Failed to find " << instance << " even though it is declared. "
          << "Check for crashes or misconficuration of the service";
      if (binder) {
        sp<IBinder> extension = nullptr;
        auto status = binder->getExtension(&extension);
        EXPECT_EQ(status, OK) << "Failed to getExtension on " << instance
                              << " status: " << statusToString(status);
        EXPECT_EQ(extension, nullptr)
            << "Found an extension interface on " << instance
            << ". This is not allowed on Cuttlefish";
      }
    }
  }
}

struct AidlPackageCheck {
  bool hasRegistration;
  bool knownMissing;
};

TEST(Hal, AidlInterfacesImplemented) {
  if (!kAidlUseUnfrozen) {
    GTEST_SKIP() << "Not valid in 'next' configuration";
  }
  if (getDeviceType() != DeviceType::PHONE) {
    GTEST_SKIP() << "Test only supports phones right now";
  }
  std::vector<VersionedAidlPackage> manifest = allAidlManifestInterfaces();
  std::vector<VersionedAidlPackage> thoughtMissing = kKnownMissingAidl;

  for (const auto& treePackage : AidlInterfaceMetadata::all()) {
    ASSERT_FALSE(treePackage.types.empty()) << treePackage.name;
    if (std::none_of(treePackage.types.begin(), treePackage.types.end(),
                     isAospAidlInterface) ||
        isMissingAidl(treePackage.name)) {
      continue;
    }
    if (treePackage.stability != "vintf") {
      continue;
    }

    // expect versions from 1 to latest version. If the package has development
    // the latest version is the latest known version + 1. Each of these need
    // to be checked for registration and knownMissing.
    std::map<size_t, AidlPackageCheck> expectedVersions;
    for (const auto version : treePackage.versions) {
      expectedVersions[version] = {false, false};
    }
    if (treePackage.has_development) {
      size_t version =
          treePackage.versions.empty() ? 1 : *treePackage.versions.rbegin() + 1;
      expectedVersions[version] = {false, false};
    }

    // Check all types and versions defined by the package for registration.
    // The package version is considered registered if any of those types are
    // present in the manifest with the same version.
    // The package version is considered known missing if it is found in
    // thoughtMissing.
    bool latestRegistered = false;
    for (const std::string& type : treePackage.types) {
      for (auto& [version, check] : expectedVersions) {
        auto it = std::remove_if(
            manifest.begin(), manifest.end(),
            [&type, &ver = version](const VersionedAidlPackage& package) {
              return package.name == type && package.version == ver;
            });
        if (it != manifest.end()) {
          manifest.erase(it, manifest.end());
          if (version == expectedVersions.rbegin()->first) {
            latestRegistered = true;
          }
          check.hasRegistration = true;
        }
        it = std::remove_if(
            thoughtMissing.begin(), thoughtMissing.end(),
            [&type, &ver = version](const VersionedAidlPackage& package) {
              return package.name == getAidlPackage(type) &&
                     package.version == ver;
            });
        if (it != thoughtMissing.end()) {
          thoughtMissing.erase(it, thoughtMissing.end());
          check.knownMissing = true;
        }
      }
    }

    if (!latestRegistered && !expectedVersions.rbegin()->second.knownMissing) {
      ADD_FAILURE() << "The latest version ("
                    << expectedVersions.rbegin()->first
                    << ") of the module is not implemented: "
                    << treePackage.name
                    << " which declares the following types:\n    "
                    << base::Join(treePackage.types, "\n    ");
    }

    for (const auto& [version, check] : expectedVersions) {
      if (check.knownMissing) {
        if (check.hasRegistration) {
          ADD_FAILURE() << "Package in missing list, but available: "
                        << treePackage.name << " V" << version
                        << " which declares the following types:\n    "
                        << base::Join(treePackage.types, "\n    ");
        }

        continue;
      }
    }
  }

  for (const auto& package : thoughtMissing) {
    ADD_FAILURE() << "Interface in missing list and cannot find it anywhere: "
                  << package.name << " V" << package.version;
  }

  for (const auto& package : manifest) {
    ADD_FAILURE() << "Can't find manifest entry in tree: " << package.name
                  << " version: " << package.version;
  }
}

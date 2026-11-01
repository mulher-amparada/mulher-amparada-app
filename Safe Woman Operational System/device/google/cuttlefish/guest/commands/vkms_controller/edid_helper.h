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

#pragma once

#include <cstdint>
#include <string>
#include <vector>

namespace cuttlefish {
namespace vkms_controller {

#define EDP_MONITOR_LIST(X) X(REDRIX)

#define DP_MONITOR_LIST(X)   \
  X(ACI_9713_ASUS_VE258_DP)  \
  X(DEL_61463_DELL_U2410_DP) \
  X(HP_Spectre32_4K_DP)      \
  X(HWP_12446_HP_Z24i_DP)

#define HDMI_MONITOR_LIST(X)   \
  X(ACI_9155_ASUS_VH238_HDMI)  \
  X(DEL_61462_DELL_U2410_HDMI) \
  X(HP_Spectre32_4K_HDMI)      \
  X(HWP_12447_HP_Z24i_HDMI)

enum class EdpMonitorName {
#define X(monitor) monitor,
  EDP_MONITOR_LIST(X)
#undef X
};

enum class DpMonitorName {
#define X(monitor) monitor,
  DP_MONITOR_LIST(X)
#undef X
};

enum class HdmiMonitorName {
#define X(monitor) monitor,
  HDMI_MONITOR_LIST(X)
#undef X
};

// Unified type to use for function arguments to handle both DP and HDMI.
struct MonitorName {
  enum class Type { UNSET, EDP, DP, HDMI } type;
  union {
    EdpMonitorName edp;
    DpMonitorName dp;
    HdmiMonitorName hdmi;
  };
  MonitorName() : type(Type::UNSET) {}
  explicit MonitorName(EdpMonitorName name) : type(Type::EDP), edp(name) {}
  explicit MonitorName(DpMonitorName name) : type(Type::DP), dp(name) {}
  explicit MonitorName(HdmiMonitorName name) : type(Type::HDMI), hdmi(name) {}
};

std::vector<uint8_t> getBinaryEdidForMonitor(const MonitorName& monitorName);

MonitorName StringToMonitorName(const std::string& name);
std::string GetConnectorTypeFromName(const std::string& name);

}  // namespace vkms_controller
}  // namespace cuttlefish

/*
 * Copyright (C) 2026 The Android Open Source Project
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

#include <filesystem>  // NOLINT(build/c++17)
#include <string_view>

namespace cuttlefish {
namespace vkms_controller {

// Commands
inline constexpr std::string_view kCommandSetup = "setup";
inline constexpr std::string_view kCommandHotplug = "hotplug";
inline constexpr std::string_view kCommandReset = "reset";
inline constexpr std::string_view kCommandListPresets = "list-presets";
inline constexpr std::string_view kCommandListDisplays = "list-displays";
inline constexpr std::string_view kCommandHelp = "help";

// Connector Types
inline constexpr std::string_view kTypeEdp = "eDP";
inline constexpr std::string_view kTypeDp = "DP";
inline constexpr std::string_view kTypeHdmi = "HDMIA";

// Connection Status
inline constexpr std::string_view kStatusConnected = "connected";
inline constexpr std::string_view kStatusDisconnected = "disconnected";

// Filesystem Paths
inline const std::filesystem::path kStateDir = "/data/vendor/vkms";
inline const std::filesystem::path kStateFilePath = kStateDir / "state.json";

}  // namespace vkms_controller
}  // namespace cuttlefish

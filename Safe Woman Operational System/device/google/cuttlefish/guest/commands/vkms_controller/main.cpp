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

/**
 * vkms_controller: A guest-side utility for Virtual Kernel Mode Setting (VKMS)
 *
 * WHY WE HAVE IT:
 * Previously, VKMS configuration logic was fragmented.
 * By uniting this logic into a single, comprehensive guest binary:
 * 1. The Host CLI acts simply as a stateless proxy (`adb shell
 * vkms_controller...`).
 * 2. Testing frameworks (Tradefed, etc.) share a single, robust mechanism for
 * setup.
 * 3. The guest natively handles the complex work of correlating virtual
 * hardware (ConfigFS indices) to Android's actual surface outputs
 * (SurfaceFlinger IDs).
 */

#include <android-base/file.h>
#include <android-base/logging.h>
#include <android-base/parseint.h>
#include <android-base/properties.h>
#include <android-base/strings.h>
#include <cutils/properties.h>
#include <getopt.h>
#include <json/json.h>
#include <sys/stat.h>
#include <unistd.h>

#include <algorithm>
#include <cstdio>
#include <filesystem>  // NOLINT(build/c++17)
#include <fstream>
#include <iostream>
#include <memory>
#include <regex>
#include <string>
#include <string_view>
#include <unordered_map>
#include <vector>

#include "common/libs/utils/subprocess.h"
#include "edid_helper.h"     // NOLINT(build/include_subdir)
#include "vkms_constants.h"  // NOLINT(build/include_subdir)
#include "vkms_tester.h"     // NOLINT(build/include_subdir)

namespace cuttlefish {
namespace vkms_controller {

constexpr std::string_view kUsage = R"(
Usage: vkms_controller <command> [options]

A guest-side utility for Virtual Kernel Mode Setting (VKMS).
It manages virtual displays by interacting directly with the kernel's VKMS ConfigFS interface.

Commands:
  setup
  hotplug
  reset
  list-presets
  list-displays

Run 'vkms_controller help <command>' or 'vkms_controller <command> help' for more information on a specific command.
)";

constexpr std::string_view kSetupUsage = R"(
Set up displays with specific EDIDs and planes:
  vkms_controller setup --screen=name=REDRIX,planes=0,enabled=true
)";

constexpr std::string_view kHotplugUsage = R"(
Hotplug (connect/disconnect) an existing display dynamically:
  vkms_controller hotplug <id> <connected|disconnected>
)";

constexpr std::string_view kResetUsage = R"(
Reset all displays and clean up the environment:
  vkms_controller reset
)";

constexpr std::string_view kListPresetsUsage = R"(
List all available hardware presets (monitor EDIDs) baked into the guest:
  vkms_controller list-presets
)";

constexpr std::string_view kListDisplaysUsage = R"(
List all configured displays, resolving SurfaceFlinger IDs and connection state:
  vkms_controller list-displays [--json]
)";

struct ConnectorState {
  int id;
  std::string type;
  int planes;
  std::string monitor;
  bool connected;
};

bool SaveState(const std::vector<ConnectorState>& states) {
  std::error_code ec;
  std::filesystem::create_directories(kStateDir, ec);
  if (ec) {
    LOG(ERROR) << "Failed to create directory " << kStateDir.string() << ": "
               << ec.message();
    return false;
  }

  Json::Value root;
  for (const auto& s : states) {
    Json::Value val;
    val["id"] = s.id;
    val["type"] = s.type;
    val["planes"] = s.planes;
    val["monitor"] = s.monitor;
    val["connected"] = s.connected;
    root["connectors"].append(val);
  }

  Json::StreamWriterBuilder builder;
  std::string json_str = Json::writeString(builder, root);

  if (!android::base::WriteStringToFile(json_str, kStateFilePath.string())) {
    PLOG(ERROR) << "Failed to write state file: " << kStateFilePath.string();
    return false;
  }
  return true;
}

bool LoadState(std::vector<ConnectorState>* states) {
  std::string json_str;
  if (!android::base::ReadFileToString(kStateFilePath.string(), &json_str)) {
    return false;
  }

  Json::Value root;
  Json::CharReaderBuilder builder;
  std::unique_ptr<Json::CharReader> reader(builder.newCharReader());
  std::string errs;
  if (!reader->parse(json_str.data(), json_str.data() + json_str.size(), &root,
                     &errs)) {
    LOG(ERROR) << "Failed to parse state file: " << errs;
    return false;
  }

  for (const auto& val : root["connectors"]) {
    states->push_back({val["id"].asInt(), val["type"].asString(),
                       val["planes"].asInt(), val["monitor"].asString(),
                       val["connected"].asBool()});
  }
  return true;
}

int DoSetup(const std::vector<std::string>& args) {
  std::vector<VkmsTester::VkmsConnectorBuilder> builders;
  std::vector<ConnectorState> states;

  int id = 0;
  std::vector<std::string> screens;

  bool parsed_screens = false;
  for (size_t i = 1; i < args.size(); ++i) {
    if (android::base::StartsWith(args[i], "--screen=")) {
      screens.push_back(args[i].substr(9));
      parsed_screens = true;
    }
  }

  if (parsed_screens) {
    for (const auto& screen : screens) {
      std::vector<std::string> parts = android::base::Split(screen, ",");
      std::string name;
      int planes = 0;
      bool enabled = true;

      for (const auto& part : parts) {
        std::vector<std::string> kv = android::base::Split(part, "=");
        if (kv.size() == 1 && name.empty()) {
          name = kv[0];
          continue;
        }
        if (kv.size() != 2) {
          continue;
        }
        if (kv[0] == "name") {
          name = kv[1];
        } else if (kv[0] == "planes") {
          if (!android::base::ParseInt(kv[1], &planes)) {
            LOG(ERROR) << "Failed to parse planes: " << kv[1];
            return 1;
          }
        } else if (kv[0] == "enabled") {
          enabled = (kv[1] == "true" || kv[1] == "1");
        }
      }

      std::string type = GetConnectorTypeFromName(name);

      auto builder = VkmsTester::VkmsConnectorBuilder::create()
                         .withType(type)
                         .withAdditionalOverlayPlanes(planes)
                         .enabledAtStart(enabled);
      if (!name.empty()) {
        builder.withMonitor(StringToMonitorName(name));
      }
      builders.push_back(builder);
      states.push_back({id++, type, planes, name, enabled});
    }
  } else {
    int num_generic = 0;
    if (args.size() > 1) {
      if (!android::base::ParseInt(args[1], &num_generic)) {
        LOG(ERROR) << "Failed to parse number of generic displays: " << args[1];
        return 1;
      }
    }
    for (int i = 0; i < num_generic; ++i) {
      std::string type = std::string(i == 0 ? kTypeEdp : kTypeDp);
      std::string monitor =
          std::string(i == 0 ? "REDRIX" : "HP_Spectre32_4K_DP");
      auto builder = VkmsTester::VkmsConnectorBuilder::create()
                         .withType(type)
                         .withMonitor(StringToMonitorName(monitor))
                         .enabledAtStart(true);
      builders.push_back(builder);
      states.push_back({i, type, 0, monitor, true});
    }
  }

  auto tester = VkmsTester::CreateWithBuilders(builders);
  if (!tester) {
    return 1;
  }
  if (parsed_screens) {
    SaveState(states);
    std::cout << "VKMS setup successful with " << states.size()
              << " connectors.\n";
    return 0;
  } else {
    return SaveState(states) ? 0 : 1;
  }
}

int DoHotplug(const std::vector<std::string>& args) {
  if (args.size() < 3) {
    LOG(ERROR) << "Usage: hotplug <id> <connected|disconnected>";
    return 1;
  }
  int id;
  if (!android::base::ParseInt(args[1], &id)) {
    LOG(ERROR) << "Invalid ID";
    return 1;
  }
  std::string state_str = args[2];
  if (state_str != kStatusConnected && state_str != kStatusDisconnected) {
    LOG(ERROR) << "Invalid hotplug state: " << state_str;
    return 1;
  }
  bool connected = (state_str == kStatusConnected);

  // Hotplugging requires persistent state tracking because the user provides a
  // simple logical Connector ID (e.g., 0, 1) from the CLI, but the underlying
  // VKMS configfs system manages connectors by their creation index.
  // We load the persisted state mapping to translate the logical ID to the
  // actual configfs index, perform the hotplug via ToggleConnector, and then
  // save the updated connection state back to disk.
  std::vector<ConnectorState> states;
  if (!LoadState(&states)) {
    LOG(ERROR) << "Failed to load state. Is VKMS setup?";
    return 1;
  }
  int index = -1;
  for (size_t i = 0; i < states.size(); ++i) {
    if (states[i].id == id) {
      index = i;
      break;
    }
  }

  if (index == -1) {
    LOG(ERROR) << "Connector ID " << id << " not found in state.";
    return 1;
  }

  if (!VkmsTester::ToggleConnector(index, connected)) {
    LOG(ERROR) << "Failed to toggle connector " << id;
    return 1;
  }

  states[index].connected = connected;
  SaveState(states);

  std::cout << "Connector " << id << " "
            << (connected ? kStatusConnected : kStatusDisconnected) << ".\n";
  return 0;
}

int DoReset(const std::vector<std::string>& args) {
  (void)args;
  VkmsTester::ForceDeleteVkmsDir();
  unlink(kStateFilePath.c_str());
  std::cout << "VKMS reset complete.\n";
  return 0;
}

int DoListPresets(const std::vector<std::string>& args) {
  (void)args;
  std::cout << "Supported Screen Presets (you can add an unlimited number of "
               "screens to this list):\n";
  std::cout << "  eDP (Internal):\n";
#define X(monitor) std::cout << "    " << #monitor << "\n";
  EDP_MONITOR_LIST(X)
#undef X
  std::cout << "\n  DisplayPort (DP):\n";
#define X(monitor) std::cout << "    " << #monitor << "\n";
  DP_MONITOR_LIST(X)
#undef X
  std::cout << "\n  HDMI:\n";
#define X(monitor) std::cout << "    " << #monitor << "\n";
  HDMI_MONITOR_LIST(X)
#undef X
  std::cout << std::endl;
  return 0;
}

std::string RunCommandAndCapture(const std::vector<std::string>& commands) {
  if (commands.empty()) {
    return "";
  }
  Command cmd(commands[0]);
  for (size_t i = 1; i < commands.size(); ++i) {
    cmd.AddParameter(commands[i]);
  }

  std::string stdout_str;
  int exit_code =
      RunWithManagedStdio(std::move(cmd), nullptr, &stdout_str, nullptr);

  if (exit_code != 0) {
    LOG(WARNING) << "Command failed with code " << exit_code;
  }
  return stdout_str;
}

int DoListDisplays(const std::vector<std::string>& args) {
  bool json_output = false;
  for (size_t i = 1; i < args.size(); ++i) {
    if (args[i] == "--json") {
      json_output = true;
    }
  }

  std::string dumpsys_out =
      RunCommandAndCapture({"dumpsys", "SurfaceFlinger", "--displays"});

  std::vector<std::string> display_ids;
  std::istringstream iss(dumpsys_out);
  std::string line;
  std::regex display_id_regex(R"(Display\s+(\d+))");
  std::smatch match;

  while (std::getline(iss, line)) {
    if (std::regex_search(line, match, display_id_regex)) {
      display_ids.push_back(match[1].str());
    }
  }

  if (json_output) {
    Json::Value root;
    Json::Value state;
    bool has_names = false;

    std::ifstream state_file(kStateFilePath.string());
    if (state_file.is_open()) {
      Json::CharReaderBuilder reader;
      std::string errs;
      if (Json::parseFromStream(reader, state_file, &state, &errs)) {
        has_names =
            state.isMember("connectors") && state["connectors"].isArray();
      }
    }

    if (has_names) {
      for (size_t i = 0; i < state["connectors"].size(); ++i) {
        Json::Value displayObj;
        std::string name = "Generic " + std::to_string(i);
        std::string status = "Connected";

        if (state["connectors"][static_cast<int>(i)].isMember("monitor")) {
          name = state["connectors"][static_cast<int>(i)]["monitor"].asString();
        }
        if (state["connectors"][static_cast<int>(i)].isMember("connected")) {
          status =
              state["connectors"][static_cast<int>(i)]["connected"].asBool()
                  ? "Connected"
                  : "Disconnected";
        }

        displayObj["display_name"] = name;
        displayObj["sf_id"] = i < display_ids.size() ? display_ids[i] : "";
        displayObj["status"] = status;
        root["displays"][std::to_string(i)] = displayObj;
      }
    } else {
      for (size_t i = 0; i < display_ids.size(); ++i) {
        Json::Value displayObj;
        displayObj["display_name"] = "Generic " + std::to_string(i);
        displayObj["sf_id"] = display_ids[i];
        displayObj["status"] = "Connected";
        root["displays"][std::to_string(i)] = displayObj;
      }
    }
    Json::StreamWriterBuilder builder;
    std::cout << Json::writeString(builder, root) << "\n";
  } else {
    std::cout << dumpsys_out << "\n";
  }
  return 0;
}

using CommandFn = int (*)(const std::vector<std::string>&);
struct Command {
  std::string_view help;
  CommandFn func;
};

static const std::unordered_map<std::string_view, Command> kCommands = {
    {kCommandSetup, {.help = kSetupUsage, .func = DoSetup}},
    {kCommandHotplug, {.help = kHotplugUsage, .func = DoHotplug}},
    {kCommandReset, {.help = kResetUsage, .func = DoReset}},
    {kCommandListPresets, {.help = kListPresetsUsage, .func = DoListPresets}},
    {kCommandListDisplays,
     {.help = kListDisplaysUsage, .func = DoListDisplays}},
};

}  // namespace vkms_controller
}  // namespace cuttlefish

int main(int argc, char** argv) {
  std::vector<std::string> args;
  for (int i = 1; i < argc; ++i) {
    args.push_back(argv[i]);
  }

  if (args.empty()) {
    std::cout << cuttlefish::vkms_controller::kUsage << std::endl;
    return 1;
  }

  if (args[0] == cuttlefish::vkms_controller::kCommandHelp ||
      (args.size() > 1 &&
       args[1] == cuttlefish::vkms_controller::kCommandHelp)) {
    std::string target;
    if (args[0] == cuttlefish::vkms_controller::kCommandHelp) {
      target = (args.size() > 1) ? args[1] : "";
    } else {
      target = args[0];
    }

    if (!target.empty() &&
        cuttlefish::vkms_controller::kCommands.count(target)) {
      std::cout << cuttlefish::vkms_controller::kCommands.at(target).help
                << std::endl;
    } else {
      std::cout << cuttlefish::vkms_controller::kUsage << std::endl;
    }
    return 0;
  }

  std::string hwc = android::base::GetProperty("ro.hardware.hwcomposer", "");
  if (hwc != "drm_hwcomposer") {
    std::cerr << "Error: Current HWC is '" << hwc
              << "'. VKMS requires drm_hwcomposer." << std::endl;
    std::cerr << "Please restart Cuttlefish with: cvd start "
                 "-hwcomposer=drm_hwcomposer"
              << std::endl;
    std::cerr << "Or use the desktop lunch target (e.g., "
                 "cf_x86_64_desktop-trunk_staging-userdebug)."
              << std::endl;
    return 1;
  }

  if (cuttlefish::vkms_controller::kCommands.count(args[0])) {
    return cuttlefish::vkms_controller::kCommands.at(args[0]).func(args);
  } else {
    std::cout << cuttlefish::vkms_controller::kUsage << std::endl;
    return 1;
  }
}

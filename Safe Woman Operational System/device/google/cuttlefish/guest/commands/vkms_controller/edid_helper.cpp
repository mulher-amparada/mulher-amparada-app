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

#include "edid_helper.h"  // NOLINT(build/include_subdir)

#include <android-base/parseint.h>
#include <android-base/strings.h>
#include <string>
#include <unordered_map>
#include <vector>

namespace cuttlefish {
namespace vkms_controller {

namespace {

std::vector<uint8_t> hexStringToBinary(const std::string& hexStr) {
  std::vector<uint8_t> binary;
  binary.reserve(hexStr.length() / 2);
  for (size_t i = 0; i < hexStr.length(); i += 2) {
    uint8_t byte;
    if (android::base::ParseUint(hexStr.substr(i, 2), &byte)) {
      binary.push_back(byte);
    }
  }
  return binary;
}

const std::unordered_map<EdpMonitorName, std::string> edpMonitorNameToEdidHex =
    {
        {EdpMonitorName::REDRIX,
         "00ffffffffffff0009e5760a00000000"
         "191f0104a51c137803ee95a3544c9926"
         "0f505400000001010101010101010101"
         "010101010101125cd01881e02d503020"
         "36001dbe1000001a623dd01881e02d50"
         "302036001dbe1000001a000000000000"
         "00000000000000000000000000000002"
         "000d28ff0a3cc80f0b23c800000000cc"},
};

const std::unordered_map<DpMonitorName, std::string> dpMonitorNameToEdidHex = {
    {DpMonitorName::ACI_9713_ASUS_VE258_DP,
     "00ffffffffffff000469f125f3c60100"
     "1d150104a5371f783a7695a5544ba226"
     "115054bfef00714f81c0814081809500"
     "950fb300d1c0023a801871382d40582c"
     "450029372100001e000000ff0042374c"
     "4d54463131363436370a000000fd0032"
     "4b185311041100f0f838f03c000000fc"
     "00415355532056453235380a202001b7"
     "020322714f0102031112130414051f90"
     "0e0f1d1e2309170783010000656e0c00"
     "10008c0ad08a20e02d10103e96002937"
     "21000018011d007251d01e206e285500"
     "29372100001e011d00bc52d01e20b828"
     "554029372100001e8c0ad09020403120"
     "0c405500293721000018000000000000"
     "000000000000000000000000000000aa"},

    {DpMonitorName::DEL_61463_DELL_U2410_DP,
     "00ffffffffffff0010ac17f04c334a31"
     "08150104b53420783a1ec5ae4f34b126"
     "0e5054a54b008180a940d100714f0101"
     "010101010101283c80a070b023403020"
     "360006442100001a000000ff00463532"
     "354d313247314a334c0a000000fc0044"
     "454c4c2055323431300a2020000000fd"
     "00384c1e5111000a20202020202001ff"
     "02031df15090050403020716011f1213"
     "14201511062309070783010000023a80"
     "1871382d40582c450006442100001e01"
     "1d8018711c1620582c25000644210000"
     "9e011d007251d01e206e285500064421"
     "00001e8c0ad08a20e02d10103e960006"
     "44210000180000000000000000000000"
     "00000000000000000000000000000021"},

    {DpMonitorName::HP_Spectre32_4K_DP,
     "00FFFFFFFFFFFF0022F01A3200000000"
     "2E180104B54728783A87D5A8554D9F25"
     "0E5054210800D1C0A9C081C0D100B300"
     "9500A94081804DD000A0F0703E803020"
     "3500C48F2100001A000000FD00183C1E"
     "873C000A202020202020000000FC0048"
     "502053706563747265203332000000FF"
     "00434E43393430303030310A2020018F"
     "020318F14B101F041303120211010514"
     "2309070783010000A36600A0F0701F80"
     "30203500C48F2100001A565E00A0A0A0"
     "295030203500C48F2100001AEF5100A0"
     "F070198030203500C48F2100001AB339"
     "00A080381F4030203A00C48F2100001A"
     "283C80A070B0234030203600C48F2100"
     "001A00000000000000000000000000C4"},

    {DpMonitorName::HWP_12446_HP_Z24i_DP,
     "00ffffffffffff0022f09e3000000000"
     "15180104a5342078264ca5a7554da226"
     "105054a10800b30095008100a9408180"
     "d1c081c00101283c80a070b023403020"
     "360006442100001a000000fd00324c18"
     "5e11000a202020202020000000fc0048"
     "50205a3234690a2020202020000000ff"
     "00434e343432313050334b0a2020006f"},
};

const std::unordered_map<HdmiMonitorName, std::string>
    hdmiMonitorNameToEdidHex = {
        {HdmiMonitorName::ACI_9155_ASUS_VH238_HDMI,
         "00ffffffffffff000469c323fccc0000"
         "2017010380331d782add45a3554fa027"
         "125054bfef00714f814081809500b300"
         "d1c001010101023a801871382d40582c"
         "4500fd1e1100001e000000ff0044384c"
         "4d54463035323437360a000000fd0032"
         "4b1e5011000a202020202020000000fc"
         "00415355532056483233380a202000be"},

        {HdmiMonitorName::DEL_61462_DELL_U2410_HDMI,
         "00ffffffffffff0010ac16f04c4e4332"
         "1f13010380342078ea1ec5ae4f34b126"
         "0e5054a54b008180a940d100714f0101"
         "010101010101283c80a070b023403020"
         "360006442100001a000000ff00463532"
         "354d39375332434e4c0a000000fc0044"
         "454c4c2055323431300a2020000000fd"
         "00384c1e5111000a202020202020012e"
         "020329f15090050403020716011f1213"
         "14201511062309070767030c00100038"
         "2d83010000e3050301023a801871382d"
         "40582c450006442100001e011d801871"
         "1c1620582c250006442100009e011d00"
         "7251d01e206e28550006442100001e8c"
         "0ad08a20e02d10103e96000644210000"
         "1800000000000000000000000000003e"},

        {HdmiMonitorName::HP_Spectre32_4K_HDMI,
         "00ffffffffffff0022f01c3201010101"
         "04190103804728782a87d5a8554d9f25"
         "0e5054210800d1c0a9c081c0d100b300"
         "9500a94081804dd000a0f0703e803020"
         "3500c48f2100001a000000fd00183c1b"
         "873c000a202020202020000000fc0048"
         "702053706563747265203332000000ff"
         "00434e43393430303030310a202001fb"
         "02033df15361605f5d101f0413031202"
         "11010514070616152309070783010000"
         "6c030c001000383c200040010367d85d"
         "c401788000e40f030000e2002b047400"
         "30f2705a80b0588a00c48f2100001a56"
         "5e00a0a0a0295030203500c48f210000"
         "1eef5100a0f070198030203500c48f21"
         "00001e000000000000000000000000a8"},

        {HdmiMonitorName::HWP_12447_HP_Z24i_HDMI,
         "00ffffffffffff0022f09f3001010101"
         "1a180103803420782e3c50a7544da226"
         "105054a1080081009500b3008180a940"
         "81c0d1c00101283c80a070b023403020"
         "360006442100001a000000fd00324c18"
         "5e11000a202020202020000000fc0048"
         "50205a3234690a2020202020000000ff"
         "00434e4b343236304c47320a202000d6"},
};

std::string getEdidHexForMonitor(const MonitorName& monitorName) {
  switch (monitorName.type) {
    case MonitorName::Type::EDP:
      return edpMonitorNameToEdidHex.at(monitorName.edp);
    case MonitorName::Type::DP:
      return dpMonitorNameToEdidHex.at(monitorName.dp);
    case MonitorName::Type::HDMI:
      return hdmiMonitorNameToEdidHex.at(monitorName.hdmi);
    default:
      return "";
  }
}

}  // namespace

std::vector<uint8_t> getBinaryEdidForMonitor(const MonitorName& monitorName) {
  std::string edidHex = getEdidHexForMonitor(monitorName);
  return hexStringToBinary(edidHex);
}

MonitorName StringToMonitorName(const std::string& name) {
#define X(monitor)                                       \
  if (android::base::EqualsIgnoreCase(name, #monitor)) { \
    return MonitorName(EdpMonitorName::monitor);         \
  }
  EDP_MONITOR_LIST(X)
#undef X

#define X(monitor)                                       \
  if (android::base::EqualsIgnoreCase(name, #monitor)) { \
    return MonitorName(DpMonitorName::monitor);          \
  }
  DP_MONITOR_LIST(X)
#undef X

#define X(monitor)                                       \
  if (android::base::EqualsIgnoreCase(name, #monitor)) { \
    return MonitorName(HdmiMonitorName::monitor);        \
  }
  HDMI_MONITOR_LIST(X)
#undef X

  return MonitorName();
}

std::string GetConnectorTypeFromName(const std::string& name) {
#define X(monitor)                                       \
  if (android::base::EqualsIgnoreCase(name, #monitor)) { \
    return "eDP";                                        \
  }
  EDP_MONITOR_LIST(X)
#undef X

#define X(monitor)                                       \
  if (android::base::EqualsIgnoreCase(name, #monitor)) { \
    return "DP";                                         \
  }
  DP_MONITOR_LIST(X)
#undef X

#define X(monitor)                                       \
  if (android::base::EqualsIgnoreCase(name, #monitor)) { \
    return "HDMIA";                                      \
  }
  HDMI_MONITOR_LIST(X)
#undef X
  return "DP";
}

}  // namespace vkms_controller
}  // namespace cuttlefish

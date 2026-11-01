# Copyright (C) 2024 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

$(call inherit-product, $(LOCAL_PATH)/common.mk)

# See http://go/al-bt-config for how we expect to configure binary translation for AL.
# Use ndk-translation unless already set by the board makefile.
AL_BINARY_TRANSLATION_MODE ?= houdini_primary_ndk_translation_secondary

ifeq ($(AL_BINARY_TRANSLATION_MODE), ndk_translation_only)

$(call inherit-product-if-exists, vendor/unbundled_google/libs/ndk_translation/x86_64_ndk_translation_support.mk)

else ifeq ($(AL_BINARY_TRANSLATION_MODE), houdini_only)

$(call inherit-product-if-exists, vendor/google_devices/desktop_ibt/houdini64.mk)

else ifeq ($(AL_BINARY_TRANSLATION_MODE), houdini_primary_ndk_translation_secondary)

$(call inherit-product-if-exists, vendor/google_devices/desktop_ibt/houdini64.mk)

NDK_TRANSLATION_AS_2ND_NATIVE_BRIDGE=true
$(call inherit-product-if-exists, vendor/unbundled_google/libs/ndk_translation/x86_64_ndk_translation_support.mk)

else

$(error Unknown AL_BINARY_TRANSLATION_MODE=$(AL_BINARY_TRANSLATION_MODE))

endif

# By default, use the x86 common variant of the layout
ifeq ($(call soong_config_get,desktop_partition_layout,disk_layout_json),)
$(call soong_config_set,desktop_partition_layout,disk_layout_json,x86)
endif

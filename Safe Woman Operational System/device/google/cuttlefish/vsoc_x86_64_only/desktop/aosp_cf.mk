#
# Copyright (C) 2025 The Android Open Source Project
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
#
#
# All components inherited here go to vendor image
#
$(call inherit-product, device/google/cuttlefish/shared/desktop/common_x86.mk)
$(call inherit-product, device/google/cuttlefish/shared/desktop/aosp_device_vendor.mk)
#
# All components inherited here go to product image (same as GSI product)
#
$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_product.mk)

PRODUCT_FSTAB_PATH := device/google/desktop/common/shared/fstab
# Ika uses ndk-translation only like http://go/al-bt-config describes.
AL_BINARY_TRANSLATION_MODE := ndk_translation_only

#
# Enable NDK Translation
#
# TODO(b/363016680): some branches don't include vendor/unbundled_google
#
$(call inherit-product-if-exists, vendor/unbundled_google/libs/ndk_translation/x86_64_ndk_translation_support.mk)

#
# Special settings for the target
#
$(call inherit-product, device/google/cuttlefish/vsoc_x86_64/bootloader.mk)

PRODUCT_NAME := aosp_cf_x86_64_desktop
PRODUCT_DEVICE := vsoc_x86_64_only
PRODUCT_MANUFACTURER := Google
PRODUCT_MODEL := Cuttlefish AOSP x86_64 Desktop

PRODUCT_VENDOR_PROPERTIES += \
    ro.soc.manufacturer=$(PRODUCT_MANUFACTURER) \
    ro.soc.model=$(PRODUCT_DEVICE)

#
# All components inherited here go to vendor image
#

# Skip installing the default cuttlefish init_dev_config package
# and setting 'ro.vendor.init_dev_config'.path vendor property.
LOCAL_ENABLE_INIT_DEV_CONFIG := false

# ARC/Auto/Desktop - don't use compressed apks.
UNCOMPRESS_CHROME_WEBVIEW = true

# Include the`launch_cvd --config al` option.
#$(call soong_config_append,cvd,launch_configs,cvd_config_desktop.json)

# Add arch-independent information.
# Arch-specific file can be added in cf_*_desktop.mk.
TARGET_BOARD_INFO_FILES += vendor/google/products/cuttlefish/desktop/android-info-common.txt

PRODUCT_COPY_FILES += \
    vendor/google/products/cuttlefish/desktop/services/wifi/desktop-virtwifi-setup.sh:$(TARGET_COPY_OUT_VENDOR)/bin/desktop-virtwifi-setup.sh \
    vendor/google/products/cuttlefish/desktop/services/wifi/desktop-virtwifi-setup.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/desktop-virtwifi-setup.rc


# Soong-only configuration for aosp_cf_x86_64_desktop
ifeq ($(TARGET_PRODUCT),aosp_cf_x86_64_desktop)
PRODUCT_SOONG_ONLY := $(RELEASE_SOONG_ONLY_CUTTLEFISH)
endif

PRODUCT_ENABLE_UFFD_GC := true


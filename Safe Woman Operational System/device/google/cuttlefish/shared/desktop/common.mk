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

# HW-independent rules (applicable to virtual devices) go here.
# HW-dependent rules (such as HAL) go to device_common.mk

# First enable HSUM, since it can affect subsequent mk behaviour.
$(call inherit-product, build/make/target/product/hsu_as_login.mk)

# TODO(b/489783760#comment12): Explicitly setting the variable is required,
# rather than just inheriting it from hsu_as_login.mk; ideally, fix this.
PRODUCT_USE_HSUM := true

$(call inherit-product, $(SRC_TARGET_DIR)/product/generic_system.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit_only.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/generic_ramdisk.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/generic_no_telephony.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_minimal.mk)

# Default AOSP sounds
$(call inherit-product-if-exists, frameworks/base/data/sounds/AllAudio.mk)

# Dynamic partitions support
PRODUCT_USE_DYNAMIC_PARTITIONS := true

$(call inherit-product, $(SRC_TARGET_DIR)/product/virtual_ab_ota/android_t_baseline.mk)
# Compression for virtual AB partitions
PRODUCT_VIRTUAL_AB_COMPRESSION_METHOD := lz4

# init_dev_config service to initialize device configuration and APEX
# selection properties
PRODUCT_PACKAGES += desktop_init_dev_config
PRODUCT_VENDOR_PROPERTIES += \
    ro.vendor.init_dev_config.path=/vendor/bin/desktop_init_dev_config

# Security related flags
USE_VERITY_AND_ENCRYPTION ?= true
PRODUCT_ENFORCE_SELINUX_TREBLE_LABELING := true
ifeq ($(USE_VERITY_AND_ENCRYPTION),true)
    # TODO(b/372670649): Investigate what exactly is required for recovery
    TARGET_RECOVERY_FSTAB := $(PRODUCT_FSTAB_PATH)/fstab-verity-encryption
    # Vendor ramdisk
    PRODUCT_COPY_FILES += \
        $(PRODUCT_FSTAB_PATH)/fstab-verity-encryption:$(TARGET_COPY_OUT_VENDOR_RAMDISK)/first_stage_ramdisk/fstab.android-desktop \

    # Vendor image
    PRODUCT_COPY_FILES += \
        $(PRODUCT_FSTAB_PATH)/fstab-verity-encryption:$(TARGET_COPY_OUT_VENDOR)/etc/fstab.android-desktop
else
    # TODO(b/372670649): Investigate what exactly is required for recovery
    TARGET_RECOVERY_FSTAB := $(PRODUCT_FSTAB_PATH)/fstab
    # Vendor ramdisk
    PRODUCT_COPY_FILES += \
        $(PRODUCT_FSTAB_PATH)/fstab:$(TARGET_COPY_OUT_VENDOR_RAMDISK)/first_stage_ramdisk/fstab.android-desktop \

    # Vendor image
    PRODUCT_COPY_FILES += \
        $(PRODUCT_FSTAB_PATH)/fstab:$(TARGET_COPY_OUT_VENDOR)/etc/fstab.android-desktop
endif

PRODUCT_COPY_FILES += \
        $(LOCAL_PATH)/init.common.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.common.rc \
        $(LOCAL_PATH)/fstab.zram:$(TARGET_COPY_OUT_VENDOR)/etc/fstab.zram

# Set default logd buffer size
PRODUCT_PROPERTY_OVERRIDES += \
    ro.logd.size.main=8M \
    ro.logd.size.system=4M
# Use erofs as the APEX payload filesystem instead of using compressed APEXes
# containing uncompressed ext4 payload.
OVERRIDE_PRODUCT_COMPRESSED_APEX := false
PRODUCT_DEFAULT_APEX_PAYLOAD_TYPE := erofs
# Include com.android.virt APEX
$(call inherit-product, packages/modules/Virtualization/apex/product_packages.mk)

# Large screen config. e.g. Enables dual-pane Settings UI.
$(call inherit-product, $(SRC_TARGET_DIR)/product/large_screen_common.mk)

# Enable project quotas and casefolding for emulated storage without sdcardfs
$(call inherit-product, $(SRC_TARGET_DIR)/product/emulated_storage.mk)

# Set product definition variables.
# "desktop" is used to identify a desktop device in contrast to phone/tablet/watch etc.
# The type(s) below also activate the respective UI string variants.
PRODUCT_CHARACTERISTICS := desktop

# BOARD_USES_GENERIC_KERNEL_IMAGE disables creating OTA files by
# default. Set PRODUCT_BUILD_GENERIC_OTA_PACKAGE to enable it again.
PRODUCT_BUILD_GENERIC_OTA_PACKAGE := true

# Exclude Non-Desktop packages
PRODUCT_PACKAGES += \
    ExcludeNonDesktopApps

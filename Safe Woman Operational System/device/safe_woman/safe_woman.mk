#
# Safe Woman Operational System
#

$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)

# Base system
$(call inherit-product, $(SRC_TARGET_DIR)/product/generic_system.mk)

# Aplicativo Mulher Amparada
PRODUCT_PACKAGES += \
    MulherAmparada

PRODUCT_NAME := safe_woman
PRODUCT_DEVICE := safe_woman
PRODUCT_BRAND := SafeWoman
PRODUCT_MODEL := Safe Woman Operational System
PRODUCT_MANUFACTURER := SafeWoman

PRODUCT_SYSTEM_NAME := safe_woman

PRODUCT_CHARACTERISTICS := nosdcard
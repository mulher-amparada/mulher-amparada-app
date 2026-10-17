#
# Safe Woman Operational System
# Cuttlefish x86_64
#

$(call inherit-product, device/google/cuttlefish/vsoc_x86_64/phone/aosp_cf.mk)

# Identidade do sistema
PRODUCT_NAME := safe_woman
PRODUCT_DEVICE := safe_woman
PRODUCT_BRAND := SafeWoman
PRODUCT_MODEL := Safe Woman Operational System
PRODUCT_MANUFACTURER := SafeWoman

# Mulher Amparada integrado ao sistema
PRODUCT_PACKAGES += \
    MulherAmparada

# Allowlist de permissões privilegiadas
PRODUCT_COPY_FILES += \
    device/safe_woman/privapp-permissions-mulher-amparada.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/permissions/privapp-permissions-mulher-amparada.xml
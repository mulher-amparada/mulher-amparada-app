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

# Aplicativos integrados ao sistema
PRODUCT_PACKAGES += \
    MulherAmparada \
    MulherAmparadaProfileAdmin

# Arquivos de permissões, inicialização e provisionamento do perfil gerenciado
PRODUCT_COPY_FILES += \
    device/safe_woman/privapp-permissions-mulher-amparada.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/permissions/privapp-permissions-mulher-amparada.xml \
    device/safe_woman/init.safe_woman.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.safe_woman.rc \
    device/safe_woman/provision-mulher-amparada-profile.sh:$(TARGET_COPY_OUT_PRODUCT)/bin/provision-mulher-amparada-profile.sh

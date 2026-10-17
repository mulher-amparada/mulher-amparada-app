#
# Safe Woman Operational System
# Produto base
#

PRODUCT_NAME := safe_woman
PRODUCT_DEVICE := safe_woman
PRODUCT_BRAND := SafeWoman
PRODUCT_MODEL := Safe Woman Operational System
PRODUCT_MANUFACTURER := Safe Woman

# Aplicativos incluídos na imagem do sistema
PRODUCT_PACKAGES += \
    MulherAmparada

# Permissões privilegiadas
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/../etc/permissions/privapp-permissions-mulher-amparada.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/permissions/privapp-permissions-mulher-amparada.xml
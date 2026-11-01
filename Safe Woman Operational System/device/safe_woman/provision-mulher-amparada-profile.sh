#!/system/bin/sh

PROFILE_NAME="Mulher Amparada"
PROFILE_PACKAGE="com.mulheres.profileadmin"
PROFILE_OWNER="$PROFILE_PACKAGE/.MulherAmparadaProfileAdminReceiver"
PROFILE_SETUP_ACTIVITY="$PROFILE_PACKAGE/.ProfileProvisioningActivity"
APP_PACKAGE="com.mulheres"
TAG="SafeWomanProfile"

log -t "$TAG" "Iniciando provisionamento do perfil gerenciado."

# Reutiliza o perfil se ele já existir; nunca cria duplicatas.
PROFILE_ID="$(pm list users | sed -n 's/.*UserInfo{\([0-9][0-9]*\):Mulher Amparada:.*/\1/p' | head -n 1)"

if [ -z "$PROFILE_ID" ]; then
    CREATE_OUTPUT="$(pm create-user --profileOf 0 --managed "$PROFILE_NAME" 2>&1)"
    log -t "$TAG" "$CREATE_OUTPUT"
    PROFILE_ID="$(printf '%s\n' "$CREATE_OUTPUT" | sed -n 's/.*user id \([0-9][0-9]*\).*/\1/p' | tail -n 1)"
fi

if [ -z "$PROFILE_ID" ]; then
    log -t "$TAG" "ERRO: não foi possível criar ou localizar o perfil gerenciado."
    exit 1
fi

# Inicia o perfil antes de instalar e ativar o administrador.
am start-user "$PROFILE_ID" >/dev/null 2>&1 || true

# Disponibiliza os aplicativos de sistema no perfil recém-criado.
pm install-existing --user "$PROFILE_ID" "$PROFILE_PACKAGE" >/dev/null 2>&1 || true
pm install-existing --user "$PROFILE_ID" "$APP_PACKAGE" >/dev/null 2>&1 || true

# Registra o Profile Owner somente na primeira configuração.
PROFILE_OWNER_SET=0
if ! dumpsys device_policy | grep -F "$PROFILE_OWNER" >/dev/null 2>&1; then
    if dpm set-profile-owner --user "$PROFILE_ID" "$PROFILE_OWNER"; then
        PROFILE_OWNER_SET=1
        log -t "$TAG" "Profile Owner configurado para o usuário $PROFILE_ID."
    else
        log -t "$TAG" "ERRO: não foi possível definir o Profile Owner."
        exit 1
    fi
fi

# O aplicativo administrador habilita o perfil após a configuração.
if [ "$PROFILE_OWNER_SET" = "1" ]; then
    am start --user "$PROFILE_ID" -n "$PROFILE_PACKAGE/$PROFILE_SETUP_ACTIVITY" >/dev/null 2>&1
fi

log -t "$TAG" "Perfil gerenciado do Mulher Amparada provisionado. ID=$PROFILE_ID"
exit 0

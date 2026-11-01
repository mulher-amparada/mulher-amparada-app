#!/system/bin/sh

PROFILE_NAME="Mulher Amparada"
PROFILE_PACKAGE="com.mulheres.profileadmin"
PROFILE_OWNER="$PROFILE_PACKAGE/.MulherAmparadaProfileAdminReceiver"
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

# Garante que os pacotes pré-instalados estejam disponíveis no novo usuário.
pm install-existing --user "$PROFILE_ID" "$PROFILE_PACKAGE" >/dev/null 2>&1 || true
pm install-existing --user "$PROFILE_ID" "$APP_PACKAGE" >/dev/null 2>&1 || true

# Define o administrador como Profile Owner. Se já estiver configurado,
# confirma a associação antes de considerar o provisionamento concluído.
if dpm set-profile-owner --user "$PROFILE_ID" "$PROFILE_OWNER"; then
    log -t "$TAG" "Profile Owner configurado para o usuário $PROFILE_ID."
else
    OWNER_STATE="$(dpm list-owners 2>&1)"
    if ! printf '%s\n' "$OWNER_STATE" | grep -F "$PROFILE_OWNER" >/dev/null; then
        log -t "$TAG" "ERRO: não foi possível definir o Profile Owner."
        log -t "$TAG" "$OWNER_STATE"
        exit 1
    fi
fi

log -t "$TAG" "Perfil gerenciado do Mulher Amparada provisionado. ID=$PROFILE_ID"
exit 0

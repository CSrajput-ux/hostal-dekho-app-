#!/usr/bin/env bash
set -e

echo "================================================================"
echo "  HostelDekho — Production Android Keystore Generator          "
echo "================================================================"

KEYSTORE_FILE="hosteldekho-release.jks"
KEY_ALIAS="hosteldekho"
VALIDITY_DAYS="10000"

if [ -f "$KEYSTORE_FILE" ]; then
    echo "[WARNING] Keystore '$KEYSTORE_FILE' already exists!"
    exit 0
fi

echo "Generating 2048-bit RSA production keystore..."
keytool -genkey -v -keystore "$KEYSTORE_FILE" -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 -validity "$VALIDITY_DAYS"

echo ""
echo "================================================================"
echo "[SUCCESS] Keystore generated: $KEYSTORE_FILE"
echo ""
echo "Add the following to your gradle.properties or environment:"
echo ""
echo "RELEASE_KEYSTORE_PATH=$(pwd)/$KEYSTORE_FILE"
echo "RELEASE_KEYSTORE_PASSWORD=YOUR_CHOSEN_PASSWORD"
echo "RELEASE_KEY_ALIAS=$KEY_ALIAS"
echo "RELEASE_KEY_PASSWORD=YOUR_CHOSEN_PASSWORD"
echo "================================================================"

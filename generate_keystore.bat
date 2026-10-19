@echo off
echo ================================================================
echo   HostelDekho — Production Android Keystore Generator
echo ================================================================
echo.

set KEYSTORE_FILE=hosteldekho-release.jks
set KEY_ALIAS=hosteldekho
set VALIDITY_DAYS=10000

if exist "%KEYSTORE_FILE%" (
    echo [WARNING] Keystore '%KEYSTORE_FILE%' already exists!
    echo To regenerate, delete the existing file first.
    exit /b 0
)

echo Generating 2048-bit RSA production keystore...
keytool -genkey -v -keystore %KEYSTORE_FILE% -alias %KEY_ALIAS% -keyalg RSA -keysize 2048 -validity %VALIDITY_DAYS%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================================
    echo [SUCCESS] Keystore generated: %KEYSTORE_FILE%
    echo.
    echo Add the following to your gradle.properties or environment:
    echo.
    echo RELEASE_KEYSTORE_PATH=%cd%\%KEYSTORE_FILE%
    echo RELEASE_KEYSTORE_PASSWORD=YOUR_CHOSEN_PASSWORD
    echo RELEASE_KEY_ALIAS=%KEY_ALIAS%
    echo RELEASE_KEY_PASSWORD=YOUR_CHOSEN_PASSWORD
    echo.
    echo Keep this file secure and BACK IT UP! You will need it for all future Play Store updates.
    echo ================================================================
) else (
    echo [ERROR] Failed to generate keystore. Make sure 'keytool' is in your PATH.
)

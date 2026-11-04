@echo off
chcp 65001 >nul
title Legyen Ön is Milliomos - EXE és Telepítő Generáló

:: jpackage felderítése
set "JPACKAGE_CMD=jpackage"
if exist "%JAVA_HOME%\bin\jpackage.exe" (
    set "JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe"
) else if exist "C:\Program Files\BellSoft\LibericaJDK-25-Full\bin\jpackage.exe" (
    set "JPACKAGE_CMD=C:\Program Files\BellSoft\LibericaJDK-25-Full\bin\jpackage.exe"
) else if exist "C:\Users\%USERNAME%\.jdks\openjdk-25.0.1\bin\jpackage.exe" (
    set "JPACKAGE_CMD=C:\Users\%USERNAME%\.jdks\openjdk-25.0.1\bin\jpackage.exe"
) else if exist "C:\Users\csaba\.jdks\openjdk-25.0.1\bin\jpackage.exe" (
    set "JPACKAGE_CMD=C:\Users\csaba\.jdks\openjdk-25.0.1\bin\jpackage.exe"
)

:: Maven parancs felderítése
set "MVN_CMD=mvn"
where mvn >nul 2>&1
if errorlevel 1 (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
    ) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" (
        set "MVN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd"
    )
)

echo ========================================================
echo 1. LÉPÉS: Fat JAR ellenőrzése a target mappában...
echo ========================================================
if not exist "target\milliomos-jatek-1.0.0-desktop.jar" (
    echo A JAR még nincs lefordítva, Maven package indítása...
    call "%MVN_CMD%" clean package -DskipTests
)

if not exist "target\milliomos-jatek-1.0.0-desktop.jar" (
    echo [HIBA] Nem található a target\milliomos-jatek-1.0.0-desktop.jar!
    echo Kérlek ellenőrizd a Maven fordítást!
    pause
    exit /b 1
)

echo.
echo ========================================================
echo 2. LÉPÉS: Tiszta bemeneti mappa előkészítése...
echo ========================================================
if exist "jpackage-input" rmdir /s /q "jpackage-input"
if exist "dist" rmdir /s /q "dist"
mkdir "jpackage-input"

copy "target\milliomos-jatek-1.0.0-desktop.jar" "jpackage-input\" >nul
if exist "questions.json" copy "questions.json" "jpackage-input\" >nul

echo.
echo ========================================================
echo 3. LÉPÉS: Hordozható Windows .EXE generálása (app-image)
echo ========================================================
"%JPACKAGE_CMD%" ^
  --type app-image ^
  --name "MilliomosJatek" ^
  --app-version "1.0.0" ^
  --vendor "Debreceni Egyetem - Szoftverfejlesztes" ^
  --input "jpackage-input" ^
  --dest "dist" ^
  --main-jar "milliomos-jatek-1.0.0-desktop.jar" ^
  --main-class "hu.milliomos.Launcher" ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "--enable-native-access=ALL-UNNAMED"

if exist "questions.json" (
    copy "questions.json" "dist\MilliomosJatek\" >nul
)

echo [SIKER] Az önállóan futtatható .exe elkészült itt:
echo         dist\MilliomosJatek\MilliomosJatek.exe

echo.
echo ========================================================
echo 4. LÉPÉS: Windows Telepítő (.exe installer) kísérlete...
echo ========================================================
"%JPACKAGE_CMD%" ^
  --type exe ^
  --name "MilliomosJatek-Telepito" ^
  --app-version "1.0.0" ^
  --vendor "Debreceni Egyetem - Szoftverfejlesztes" ^
  --input "jpackage-input" ^
  --dest "dist" ^
  --main-jar "milliomos-jatek-1.0.0-desktop.jar" ^
  --main-class "hu.milliomos.Launcher" ^
  --win-shortcut ^
  --win-menu ^
  --win-dir-chooser ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "--enable-native-access=ALL-UNNAMED"

if errorlevel 1 (
    echo.
    echo [MEGJEGYZÉS] A telepítős (Setup .exe) változathoz a WiX Toolset szükséges,
    echo de a hordozható dist\MilliomosJatek\MilliomosJatek.exe már KÉSZ és azonnal indítható!
) else (
    echo [SIKER] A telepítő (.exe) is elkészült a dist\ mappában!
)

rmdir /s /q "jpackage-input"
echo.
pause
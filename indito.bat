@echo off
chcp 65001 >nul
title Legyen Ön is Milliomos - Indító

set "JAR_FILE=target\milliomos-jatek-1.0.0-desktop.jar"
set "JVM_ARGS=--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow"

:: Java futtató felderítése
set "JAVAW_CMD=javaw"
if exist "%JAVA_HOME%\bin\javaw.exe" (
    set "JAVAW_CMD=%JAVA_HOME%\bin\javaw.exe"
) else if exist "C:\Program Files\BellSoft\LibericaJDK-25-Full\bin\javaw.exe" (
    set "JAVAW_CMD=C:\Program Files\BellSoft\LibericaJDK-25-Full\bin\javaw.exe"
) else if exist "C:\Users\%USERNAME%\.jdks\openjdk-25.0.1\bin\javaw.exe" (
    set "JAVAW_CMD=C:\Users\%USERNAME%\.jdks\openjdk-25.0.1\bin\javaw.exe"
) else if exist "C:\Users\csaba\.jdks\openjdk-25.0.1\bin\javaw.exe" (
    set "JAVAW_CMD=C:\Users\csaba\.jdks\openjdk-25.0.1\bin\javaw.exe"
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
:: Hibakód törlése
(call )

:: Ha még nem létezik a lefordított Fat JAR, lefordítjuk Maven-nel
if not exist "%JAR_FILE%" (
    echo [INFO] A futtatható JAR fájl még nem létezik. Fordítás folyamatban...
    call "%MVN_CMD%" clean package -DskipTests
    if errorlevel 1 (
        echo [HIBA] A Maven fordítás sikertelen! Ellenőrizd, hogy a Maven elérhető-e!
        pause
        exit /b 1
    )
)

echo [INFO] Legyen Ön is Milliomos indítása (%JAVAW_CMD%)...
start "" "%JAVAW_CMD%" %JVM_ARGS% -Dfile.encoding=UTF-8 -jar "%JAR_FILE%"

exit /b 0
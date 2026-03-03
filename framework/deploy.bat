@echo off
set FRAMEWORK_DIR=d:\ITU\S5\M.Naina\BackOf-taxi\framework
set SERVLET_JAR=%FRAMEWORK_DIR%\jakarta.servlet-api_5.0.0.jar
set BUILD_DIR=build
set DEST_DIR="d:\ITU\S5\M.Naina\BackOf-taxi\modules"

echo Compilation du framework depuis %FRAMEWORK_DIR%...
echo.

REM Nettoyage
if exist "%BUILD_DIR%\classes" rmdir /s /q "%BUILD_DIR%\classes"
if exist "%BUILD_DIR%\framework.jar" del "%BUILD_DIR%\framework.jar"

REM Création des répertoires
if not exist "%BUILD_DIR%\classes" mkdir "%BUILD_DIR%\classes"

REM 1. Compilation des annotations
echo [1/4] Compilation des annotations...
javac -d "%BUILD_DIR%\classes" framework\annotation\*.java

REM 2. Compilation des utilitaires
echo [2/4] Compilation des utilitaires...
javac -classpath "%SERVLET_JAR%;%BUILD_DIR%\classes" -d "%BUILD_DIR%\classes" framework\utilitaire\*.java

REM 3. Compilation des servlets
echo [3/4] Compilation des servlets...
javac -classpath "%SERVLET_JAR%;%BUILD_DIR%\classes" -d "%BUILD_DIR%\classes" framework\servlet\*.java

REM 4. Création du JAR
echo [4/4] Creation du JAR...
cd %BUILD_DIR%
jar cvf framework.jar -C classes .
cd ..

if errorlevel 1 (
    echo.
    echo ERREUR: Echec de la compilation !
    pause
    exit /b 1
)

echo.
echo ✅ Compilation terminee avec succes !
echo Le JAR est disponible dans : %BUILD_DIR%\framework.jar

REM 5. Déploiement
echo [5/5] Deploiement vers le projet BackOf-taxi...
if not exist %DEST_DIR% mkdir %DEST_DIR%
copy "%BUILD_DIR%\framework.jar" %DEST_DIR%
copy "%FRAMEWORK_DIR%\MANUEL_UTILISATION.md" %DEST_DIR%

echo.
echo ✅ Deploiement termine avec succes !
echo.
pause

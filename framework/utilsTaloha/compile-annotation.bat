@echo off
chcp 65001 >nul
echo ===============================
echo  Compilation du projet Java
echo ===============================
echo.

REM === Définition des chemins ===
set "BUILD_DIR=build\classes"
set "SERVLET_JAR=jakarta.servlet-api_5.0.0.jar"
set "TEST_DIR=D:\xampp\tomcat\webapps\testFramework"
set "MAIN_CLASS=testFramework.AnnotationTestRunner"

REM === Création du répertoire de build s’il n’existe pas ===
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

REM === Vérifier l’existence du fichier JAR ===
if not exist "%SERVLET_JAR%" (
    echo [ERREUR] Le fichier %SERVLET_JAR% est introuvable dans %CD%.
    echo Veuillez placer le JAR dans ce répertoire ou spécifier son chemin complet.
    pause
    exit /b 1
)

REM === Nettoyage du répertoire de build ===
echo Nettoyage du répertoire de build...
if exist "%BUILD_DIR%" rmdir /S /Q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"

REM === Compilation des annotations ===
echo Compilation des annotations...
javac -d "%BUILD_DIR%" framework\annotation\*.java
if errorlevel 1 (
    echo [ERREUR] Échec de la compilation des annotations !
    pause
    exit /b 1
)

REM === Compilation des servlets ===
echo Compilation des servlets...
javac -classpath "%SERVLET_JAR%;%BUILD_DIR%" -d "%BUILD_DIR%" framework\servlet\*.java
if errorlevel 1 (
    echo [ERREUR] Échec de la compilation des servlets !
    pause
    exit /b 1
)

REM === Compilation du test principal ===
echo Compilation du test (AnnotationTestRunner)...
if not exist "%TEST_DIR%\AnnotationTestRunner.java" (
    echo [ERREUR] Le fichier AnnotationTestRunner.java est introuvable dans %TEST_DIR%.
    pause
    exit /b 1
)
javac -classpath "%BUILD_DIR%;%SERVLET_JAR%" -d "%BUILD_DIR%" "%TEST_DIR%\AnnotationTestRunner.java"
if errorlevel 1 (
    echo [ERREUR] Échec de la compilation du test !
    pause
    exit /b 1
)

echo.
echo ✅ Compilation réussie !
echo -----------------------------------------------
echo Pour exécuter le programme, lancez la commande :
echo java -cp "%BUILD_DIR%;%SERVLET_JAR%" %MAIN_CLASS%
echo -----------------------------------------------
echo.
pause

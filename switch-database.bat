@echo off
echo ========================================
echo LU-MEMORY Database Launcher
echo ========================================
echo.
echo Iniciando aplicación con SQLite...
echo La base de datos se creará en: C:\Apps\sqlite-tools-win-x64\lu-memory.db
echo.
mvn spring-boot:run
pause

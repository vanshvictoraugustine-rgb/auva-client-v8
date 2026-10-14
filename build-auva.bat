@echo off
setlocal
where java >nul 2>nul || (echo Install Java 21 first.& exit /b 1)
where gradle >nul 2>nul || (echo Gradle 9.5.1 is needed, or use GitHub Actions.& exit /b 1)
call gradle build -x test
if errorlevel 1 (echo Build failed.& pause& exit /b 1)
echo Build succeeded: build\libs\auva-client-0.4.0.jar
pause

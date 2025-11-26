@echo off
setlocal

cd /d %~dp0

echo.
echo === Building Docker image for backend (javazadanie-backend) ===
docker build -t javazadanie-backend .
if errorlevel 1 (
    echo Docker build failed. Check errors above.
    pause
    exit /b 1
)

echo.
echo === Stopping old backend container (if exists) ===
docker rm -f javazadanie-backend >nul 2>&1

echo.
echo === Starting backend in Docker on port 8080 with existing DB ===
docker run -d --name javazadanie-backend ^
  -p 8080:8080 ^
  -v "%~dp0javaZadanie.db:/app/javaZadanie.db" ^
  javazadanie-backend
if errorlevel 1 (
    echo Failed to start backend container.
    pause
    exit /b 1
)

echo.
echo === Waiting 7 seconds for backend to start... ===
timeout /t 7 /nobreak >nul

echo.
echo === Starting JavaFX client (frontend) ===
start "" cmd /k "cd /d %~dp0 && mvnw.cmd javafx:run"

echo.
echo All done. Backend is running in Docker, frontend in a new window.
endlocal

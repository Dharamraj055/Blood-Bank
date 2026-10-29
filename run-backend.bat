@echo off
cd /d "%~dp0backend"
set "PATH=C:\Users\dhara\.tools\node-v20.18.0-win-x64;%PATH%"
echo Starting BloodRadar Backend Server...
npm run dev
pause


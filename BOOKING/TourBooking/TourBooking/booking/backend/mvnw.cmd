@echo off
setlocal

@REM Minimal, robust Maven wrapper for Windows PowerShell.
@SET "__MVNW_PS__=C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe"
@IF NOT EXIST "%__MVNW_PS__%" (SET "__MVNW_PS__=powershell.exe")

@SET "__MVNW_CMD__="
@FOR /F "tokens=1* delims==" %%A IN ('""%__MVNW_PS__%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0mvnw.ps1""') DO @(
  IF "%%A"=="MVN_CMD" (SET "__MVNW_CMD__=%%B") ELSE IF NOT "%%B"=="" (ECHO %%A=%%B) ELSE (ECHO %%A)
)

@IF NOT "%__MVNW_CMD__%"=="" ("%__MVNW_CMD__%" %*)
@echo Cannot start maven from wrapper >&2
@exit /b 1

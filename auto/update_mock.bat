@echo off
python Update_mock.py

rem Capture the exit code from the Python script
set EXIT_CODE=%errorlevel%

rem Check the exit code and exit the batch file accordingly
if %EXIT_CODE% neq 0 (
    echo Python script exited with an error. Exiting batch file with error code: %EXIT_CODE% > error.txt
    exit /b %EXIT_CODE%
) else (
    echo Python script completed successfully. Exiting batch file with code 0. > error.txt
    exit /b 0
)
@echo off
set SERVER_JAR=paper-1.21.10-117.jar
:RESTART
cls
echo Starting Minecraft server...
java -Xms2G -Xmx2G -jar %SERVER_JAR% --nogui

echo.
echo The server has stopped.
set /p USER_INPUT=Press any key to restart, or type q to quit: 

if /I "%USER_INPUT%"=="q" goto END
goto RESTART

:END
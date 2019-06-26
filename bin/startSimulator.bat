@echo off
set HOME_DIR=C:\Users\jl80929\Documents\GitHub\SmartSimulator
set LIB_DIR=%HOME_DIR%\target\lib
set CONF_DIR=%HOME_DIR%\src\main\resources
set JAVA_HOME=C:\applications\jdk1.8.0_71
setlocal enabledelayedexpansion
set CLASSPATH=
for %%i in (%LIB_DIR%\*.jar) do set CLASSPATH=!CLASSPATH!;%%i
set CLASSPATH=!CLASSPATH!;%HOME_DIR%\target\SmartSimulator-1.0.1.jar
set CLASSPATH=!CLASSPATH!;%CONF_DIR%
set CLASSPATH=!CLASSPATH!;%CONF_DIR%\log4j.properties

cd %HOME_DIR%
%JAVA_HOME%\bin\java -classpath %CLASSPATH% -Dlog4j.configuration=file:///%CONF_DIR%/log4j.properties -DScript_Home=%HOME_DIR%/scripts io.github.lujian213.simulator.SimulatorService scripts



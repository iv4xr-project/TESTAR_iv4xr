:: Indicate the number of distributed dockers to execute in the batch file
:: run_distributed_labrecruits.bat 5

rem We are executing this batch file in the cloned https://github.com/iv4xr-project/TESTAR_iv4xr directory

:: Compile TESTAR to check everything is OK
call gradlew distTar

:: Clean and prepare a new OrientDB server
:: https://github.com/iv4xr-project/TESTAR_iv4xr/releases/download/v3.3/orientdb-3.0.34_testar_iv4xr.zip
rmdir /s /q "testar\target\orientdb-3.0.34"

if exist "testar\target\orientdb-3.0.34_testar_iv4xr.zip" (echo "orientdb-3.0.34_testar_iv4xr.zip exists"
) else (curl -L "https://github.com/iv4xr-project/TESTAR_iv4xr/releases/download/v3.3/orientdb-3.0.34_testar_iv4xr.zip" -o "testar\target\orientdb-3.0.34_testar_iv4xr.zip"
)

tar -xf "testar\target\orientdb-3.0.34_testar_iv4xr.zip" -C "testar\target"

START "orientdb" /D "testar\target\orientdb-3.0.34\bin" server.bat

timeout /t 10

:: Then build the docker image
docker build -t iv4xr/testar:latest .

rem docker container rm $(docker container ls -aq)
@echo Removing old containers
FOR /f "tokens=*" %%i IN ('docker ps -aq') DO docker rm %%i

@echo Building new containers based on the docker image
@echo Started Execution: %date% %time%

FOR /L %%A IN (1,1,%1) DO (
  docker run -d --add-host=host.docker.internal:host-gateway --shm-size=512m iv4xr/testar:latest
)

:wait
timeout /t 5
FOR /f "tokens=*" %%i IN ('docker ps -a -q --filter "status=running"') DO GOTO wait

@echo End Execution: %date% %time%

timeout /t 10

@echo Stop the OrientDB server process
START "orientdb" /D "testar\target\orientdb-3.0.34\bin" shutdown.bat -u root -p testar

timeout /t 10
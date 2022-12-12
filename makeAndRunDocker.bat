call gradlew distTar

docker build -t iv4xr/testar:latest .

@rem docker run -d --shm-size=512m --mount type=bind,source="C:\testardock\settings",target=/testar/bin/settings --mount type=bind,source="C:\Users\testar\Desktop\TESTAR_dev",target=/mnt --mount type=bind,source="c:\testardock\output",target=/testar/bin/output iv4xr/testar:latest

docker run -d --shm-size=512m --mount type=bind,source="c:\testardock\output",target=/testar/bin/output iv4xr/testar:latest
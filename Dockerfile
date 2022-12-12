## This image should be created with an Ubuntu based,
## but it is easy to use the selenium chromedriver because it already contains a Xvfb server
FROM selenium/standalone-chrome

USER root
RUN apt-get -o Acquire::Check-Valid-Until=false -o Acquire::Check-Date=false update && apt-get install -y openjdk-16-jdk libxkbcommon-x11-0 unzip

ADD testar/target/distributions/testar.tar .

RUN wget https://github.com/iv4xr-project/TESTAR_iv4xr/releases/download/v3.0/linux_labrecruits_2.2.1.zip -P /testar/bin/suts
RUN unzip /testar/bin/suts/linux_labrecruits_2.2.1.zip -d /testar/bin/suts
RUN chmod -R 777 /testar/bin/suts/labrecruits_linux

ENV JAVA_HOME "/usr/lib/jvm/java-16-openjdk-amd64"
ENV DISPLAY=":99.0"

COPY runLabRecruitsImage /runLabRecruitsImage
COPY README.Docker /README.Docker
RUN chmod 777 /runLabRecruitsImage

CMD [ "sh", "/runLabRecruitsImage"]


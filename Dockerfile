FROM wpilib/ubuntu-base:18.04

RUN <ls -a .>
COPY . /

ENTRYPOINT ["/gradlew run"]

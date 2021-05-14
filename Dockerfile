FROM wpilib/ubuntu-base:18.04

COPY . /

ENTRYPOINT ["/gradlew run"]

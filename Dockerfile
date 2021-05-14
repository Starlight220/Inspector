FROM wpilib/ubuntu-base:18.04


RUN chmod +x ./gradlew
COPY . /
ENTRYPOINT ["/gradlew run"]

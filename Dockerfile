FROM wpilib/ubuntu-base:18.04


RUN chmod +x entrypoint.sh
COPY . /
ENTRYPOINT ["/gradlew run"]

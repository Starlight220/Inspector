FROM wpilib/ubuntu-base

RUN chmod +x entrypoint.sh
COPY . /
ENTRYPOINT ["/gradlew run"]

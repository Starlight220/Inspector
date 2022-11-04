FROM wpilib/ubuntu-base:22.04

RUN chmod +x /inspector/gradlew
RUN ./gradlew jar

COPY . /inspector/
RUN chmod +x /inspector/run.sh

ENTRYPOINT ["/inspector/run.sh"]

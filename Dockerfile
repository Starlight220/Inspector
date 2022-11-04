FROM wpilib/ubuntu-base:22.04

COPY . /inspector/

RUN chmod +x /inspector/gradlew
RUN cd /inspector/ && ./gradlew jar

RUN chmod +x /inspector/run.sh

ENTRYPOINT ["/inspector/run.sh"]

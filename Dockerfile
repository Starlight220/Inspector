FROM wpilib/ubuntu-base:22.04

# Release
COPY ./run.sh  /inspector/run.sh
ADD https://github.com/Starlight220/Inspector/releases/download/v1.8/Inspector.jar /inspector/build/libs/Inspector.jar

## Development
#COPY . /inspector/
#RUN chmod +x /inspector/gradlew
#RUN cd /inspector/ && ./gradlew jar


RUN chmod +x /inspector/run.sh

ENTRYPOINT ["/inspector/run.sh"]

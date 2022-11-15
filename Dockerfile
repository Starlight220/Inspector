FROM wpilib/ubuntu-base:22.04

COPY ./run.sh  /inspector/run.sh
ADD https://github.com/Starlight220/Inspector/releases/download/v1.5/Inspector.jar /inspector/build/libs/Inspector.jar

RUN chmod +x /inspector/run.sh

ENTRYPOINT ["/inspector/run.sh"]

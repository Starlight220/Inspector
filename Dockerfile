FROM wpilib/ubuntu-base:22.04 as builder

COPY . /inspector/
RUN cd /inspector/ && ./gradlew --no-daemon jar


FROM wpilib/ubuntu-base:22.04

COPY --from=builder /inspector/build/libs/Inspector.jar /inspector/build/libs/Inspector.jar

COPY ./run.sh  /inspector/run.sh

ENTRYPOINT ["/inspector/run.sh"]

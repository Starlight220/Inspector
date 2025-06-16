FROM alpine/java:17 as builder

COPY . /inspector/
RUN cd /inspector/ && ./gradlew --no-daemon jar


FROM alpine/java:17

RUN apk --no-cache add git

COPY --from=builder /inspector/build/libs/Inspector.jar /inspector/build/libs/Inspector.jar

COPY ./run.sh  /inspector/run.sh

ENTRYPOINT ["/inspector/run.sh"]

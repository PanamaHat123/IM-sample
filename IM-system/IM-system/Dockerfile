FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER Lal


RUN mkdir -p /imTest/server
RUN mkdir -p /imTest/server/logs

WORKDIR /imTest/server


ADD ./im-tcp/target/im-tcp.jar ./tcp.jar
ADD ./im-service/target/im-service.jar ./service.jar
ADD ./im-message-store/target/im-message-store.jar ./messageStore.jar

COPY start.sh ./start.sh

RUN chmod 777 ./start.sh

EXPOSE 8001
CMD ["sh","start.sh"]
#ENTRYPOINT ["java", "-jar","-Duser.timezone=GMT+08", "tcp.jar","--spring.profiles.active=prod"]

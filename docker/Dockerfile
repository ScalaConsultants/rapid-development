# Run sbt dist before building Docker image

FROM  debian:9

# As defined https://github.com/zulu-openjdk/zulu-openjdk/blob/master/debian/8u131-8.21.0.1/Dockerfile Java 8u131
RUN apt-get -yqq update && \
    apt-get -yqq install apt-transport-https && \
    echo "deb http://repos.azulsystems.com/debian stable  main" >> /etc/apt/sources.list.d/zulu.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0x219BD9C9 && \
    apt-get -yqq update && \
    apt-get -yqq install zulu-8=8.21.0.1 && \
    java -version && \
    echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 && \
    apt-get -yqq update && \
    apt-get -yqq install mc \
    sbt && \
    sbt sbtVersion && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ARG APP_NAME
ENV APP_NAME ${APP_NAME:-rapid-development}

COPY ./scripts/docker-entrypoint.sh /docker-entrypoint.sh

RUN mkdir -p -m 755 /app

COPY ./target/universal/$APP_NAME.zip /app

RUN cd /app && \
    unzip -u $APP_NAME.zip && \
    rm -f $APP_NAME.zip

# Not used to force docker run to specifically needed ports
#EXPOSE 9000

ENTRYPOINT ["/docker-entrypoint.sh"]

CMD ["bash", "-c", "./app/${APP_NAME}/bin/${APP_NAME}"]

#This file is only used if there is an app.yaml with custom runtime

FROM gcr.io/google-appengine/jetty

COPY gae-dump-webapp-0.0.1-SNAPSHOT.war $APP_DESTINATION_WAR

RUN unzip $APP_DESTINATION_WAR -d $APP_DESTINATION_EXPLODED_WAR 

RUN /scripts/jetty/generate-jetty-start.sh

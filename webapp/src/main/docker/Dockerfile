#This file is only used if there is an app.yaml with custom runtime

FROM gcr.io/google-appengine/jetty
#FROM jetty9-compat:latest
#FROM gcr.io/jetty9-work/jetty9-compat:release-20190429

ADD gae-dump-1.0-SNAPSHOT.war $JETTY_BASE/webapps/root.war
RUN unzip $JETTY_BASE/webapps/root.war -d $JETTY_BASE/webapps/root \
 && chown -R jetty.jetty $JETTY_BASE/webapps/root

WORKDIR $JETTY_BASE
RUN java -jar $JETTY_HOME/start.jar --add-to-startd=gzip-inflate \
 && $JETTY_BASE/generate_jetty_cmd.sh 


FROM jetty9-compat:latest
#FROM gcr.io/jetty9-work/jetty9-compat:jetty-9.3.26-1f79e8231c0026d8b1bfb4d67bd3b64c789c159c
#FROM gcr.io/jetty9-work/jetty:9.4-2019-04-03-07-37
#FROM gcr.io/google-appengine/jetty

ADD gae-dump-1.0-SNAPSHOT.war $JETTY_BASE/webapps/root.war
RUN unzip $JETTY_BASE/webapps/root.war -d $JETTY_BASE/webapps/root \
 && chown -R jetty.jetty $JETTY_BASE/webapps/root

WORKDIR $JETTY_BASE
RUN java -jar $JETTY_HOME/start.jar --add-to-startd=gzip-inflate \
 && $JETTY_BASE/generate_jetty_cmd.sh 


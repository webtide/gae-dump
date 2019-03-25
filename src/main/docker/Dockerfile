FROM gcr.io/google-appengine/jetty
# FROM gcr.io/jetty9-work/jetty/traceid243-7
ADD logging.properties /var/lib/jetty/etc/java-util-logging.properties
ADD gae-dump-1.0-SNAPSHOT.war $JETTY_BASE/webapps/root.war

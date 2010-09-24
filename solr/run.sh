#!/bin/bash

java \
  -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=3000 \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dslave=disabled \
  -Xms512M \
  -Xmx1024M \
  -server \
  -Dsolr.solr.home=. \
  -Djava.util.logging.config.file=./logging.properties \
  -Djetty.home=./jetty-6.1.25 \
  -Djetty.logs=./logs \
  -Djetty.port=8983 \
  -DSTOP.PORT=9983 \
  -DSTOP.KEY=changeme \
  -jar ./jetty-6.1.25/start.jar

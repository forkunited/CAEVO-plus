#!/bin/bash
# FIXME Change url wget micro-util-0.0.7-jar-with-dependencies.jar
mkdir repo
mvn deploy:deploy-file -Durl=file://repo -Dfile=micro-util-0.0.7-jar-with-dependencies.jar -DgroupId=edu.cmu.ml.rtw -DartifactId=micro-util -Dpackaging=jar -Dversion=0.0.7
# FIXME Add this back rm micro-util-0.0.7-jar-with-dependencies.jar

cp src/main/resources/event.properties event.properties

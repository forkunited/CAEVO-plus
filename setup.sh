#!/bin/bash
mkdir repo
mvn deploy:deploy-file -Durl=file://repo -Dfile=micro-util-0.0.7-jar-with-dependencies.jar -DgroupId=edu.cmu.ml.rtw -DartifactId=micro-util -Dpackaging=jar -Dversion=0.0.7
cp src/main/resources/event.properties event.properties

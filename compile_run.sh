#!bin/bash

# this script only works if you have your dependencies set up the same way
# as below. If this is not the case, please consult ./README.txt

echo Compiling
javac -cp "/selenium/selenium-server-standalone-3.4.0.jar;/selenium/sel3.4/client-combined-3.4.0-nodeps.jar;/JUNIT/junit-4.12.jar;/JUNIT/hamcrest-core-1.3.jar;.;" *.java -d bin -Xlint:deprecation 

echo Packaging Jar
cd bin
jar cfm flashscores.jar Manifest.txt *.class

echo Running
java -jar ./flashscores.jar
#cd bin
#sh run.sh

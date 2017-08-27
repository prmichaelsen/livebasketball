#!bin/bash
rm -rf bin
mkdir bin

lib_path="./lib/"
# mark java dependencies here:
require=()
require+=("commons-lang3-3.5/*.jar") 
require+=("commons-lang3-3.5/*.jar")
require+=("*")
require+=("jaxrs-ri/ext/*")
require+=("jaxrs-ri/api/*")
require+=("jaxrs-ri/lib/*")
require+=("jetty-distribution-9.4.3.v20170317/lib/*")
require+=("jetty-distribution-9.4.3.v20170317/modules/sessions/*.jar")
require+=("gson-2.8.1.jar")

# determine os
platform=-1
linux=0
windows=1
mac=2
unamestr=`uname`
echo "Detected OS is $unamestr"
if [[ "$unamestr" == 'Linux' ]]; then
   platform=$linux
elif [[ "$unamestr" == 'MINGW32_NT-10.0-WOW' ]]; then
   platform=$windows
else
	echo "Unsupported OS"
	exit
fi

# build class_path
path_seperator=( ":" ";" ":" ) 
class_path="."
for req in "${require[@]}"
do
	class_path="$class_path${path_seperator[$platform]}$lib_path$req"
done

# create Manifest.txt 
for req in "${require[@]}"
do
	manifest_path="$manifest_path .$lib_path$req"
done
manifest="./bin/Manifest"
echo "Main-Class: com.parm.server.Main" > $manifest
printf "Class-Path:" >> $manifest
printf "$manifest_path" >> $manifest
echo >> $manifest 

echo Compiling
find -name "*.java" > java
javac -cp $class_path -d bin @java -Xlint:deprecation 
rm -f java

echo Packaging Jar
cd bin 
find -name "*.class" > class
jar cfm main.jar Manifest @class

echo Running 
cd ..
bash run.sh 

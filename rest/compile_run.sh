#!bin/bash

# get java dependencies
require=()
while IFS=  read -r -d $'\0'; do
    require+=("$REPLY")
done < <(find ./lib/ -name *.jar -print0) 

# mark package name here
package="com.parm.server"

mkdir -p bin lib

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
elif [[ "$unamestr" == 'MINGW64_NT-10.0' ]]; then
   platform=$windows 
else
   echo "Unsupported OS"
   exit
fi

# build class_path
path_seperator=( ":" ";" ":" ) 
class_path=""
for req in "${require[@]}"
do
	class_path="$class_path${path_seperator[$platform]}$req"
done

# create Manifest.txt 
manifest="./bin/Manifest"
#echo "Main-Class: com.parm.server.Main" > $manifest
#echo "Class-Path:" >> $manifest
#for req in "${require[@]}"
#do
	#echo " $req" >> $manifest
#done

cd bin
rm -f *.jar
rm -f *.class

echo Compiling
cd ..
find -name "*.java" > sources
javac -cp $class_path -d bin @sources -Xlint:deprecation -Xlint:unchecked
rm -f sources

echo Packaging Jar
cd bin 
find -name "*.class" > class
jar cmf Manifest main.jar .

#echo Running
#java -jar ./main.jar 

echo Running 
cd ..
bash run.sh 

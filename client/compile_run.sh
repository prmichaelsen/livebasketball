#!bin/bash

# get java dependencies
require=()
while IFS=  read -r -d $'\0'; do
    require+=("$REPLY")
done < <(find ./lib/ -name *.jar -print0) 

# mark package name here
package="com.patrickmichaelsen.livebasketball"

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
for req in "${require[@]}"
do
	manifest_path="$manifest_path .$lib_path$req"
done
manifest="./bin/Manifest"
echo "Main-Class: $package.Main" > $manifest
printf "Class-Path:" >> $manifest
printf "$manifest_path" >> $manifest
echo >> $manifest 

cd bin
rm -f *.jar
rm -f *.class

echo Compiling
cd ..
# get source files
find -name "*.java" > sources
javac -cp "$class_path" @sources -d bin -Xlint:deprecation -Xlint:unchecked
rm -f sources

echo Packaging Jar
cd bin
jar cmf Manifest main.jar .

echo Running
cd ../bin
java -jar ./main.jar 

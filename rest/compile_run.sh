#!bin/bash

# mark package name here
package="com.patrickmichaelsen.livebasketball"
manifest="Manifest.mf"

# carefully remove previous builds
mkdir -p bin lib
cd bin
find . -type f -name "*.class" -delete
rm -f *.jar
rm -f "$manifest"
find . -type d -empty -delete

# get java dependencies
require=()
while IFS=  read -r -d $'\0'; do
    require+=("$REPLY")
done < <(find ../lib -name *.jar -print0) 

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
class_path="."
for req in "${require[@]}"
do
	class_path="$class_path${path_seperator[$platform]}$req"
done

# create Manifest
echo "Main-Class: $package.Main" > $manifest
echo "Class-Path: ." >> $manifest
for req in "${require[@]}"
do
 echo "  $req" >> $manifest
done

echo Compiling
# get source files
find .. -name "*.java" > sources
javac -cp "$class_path" @sources -d . -Xlint:deprecation -Xlint:unchecked
rm -f sources

echo Packaging Jar
jar cmf $manifest main.jar .

echo Running
java -jar ./main.jar 
cd ..
bash run.sh 

#!bin/bash

# get java dependencies
require=()
while IFS=  read -r -d $'\0'; do
    require+=("$REPLY")
done < <(find ./lib/ -name *.jar -print0) 

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
	echo $req
	class_path="$class_path${path_seperator[$platform]}$req"
done

# create Manifest.txt 
for req in "${require[@]}"
do
	manifest_path="$manifest_path .$lib_path$req"
done
manifest="./bin/Manifest"
echo "Main-Class: Main" > $manifest
printf "Class-Path:" >> $manifest
printf "$manifest_path" >> $manifest
echo >> $manifest 

cd bin
rm -f *.jar
rm -f *.class

echo Compiling
cd ..
javac -cp "$class_path" *.java -d bin -Xlint:deprecation 

echo Packaging Jar
cd bin
jar cfm main.jar Manifest *.class 

echo Running
cd ../bin
java -jar ./main.jar 

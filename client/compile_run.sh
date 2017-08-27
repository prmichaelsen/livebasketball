#!bin/bash

# program defaults
STANDALONE=false
LIBPATH="lib"
MANIFEST="Manifest.mf"
HELP=false
RUN=false

# get options
while [[ $# -gt 0 ]]
do
	key="$1"

	case $key in
		-t|--target)
			TARGET=$2
			shift # past argument
			;;
		-s|--standalone)
			STANDALONE=true
			;;
		-p|--package)
			PACKAGE="$2"
			shift # past argument
			;;
		-l|--lib)
			LIBPATH="$2"
			shift # past argument
			;;
		-n|--name)
			NAME="$2"
			shift # past argument
			;;
		-r|--run)
			RUN=true
			;;
		-h|--help)
			HELP=true
			;;
		*)
			# unknown option
			;;
esac
shift # past argument or value
done 

if ! [[ $TARGET ]]; then
	echo "Error: No target main class found"
	echo 
	HELP=true
fi

if [[ $HELP == true ]]; then 
	echo "This script will neatly package your java project."
	echo "Recommended Project Structure"
	echo ".. compile_run.sh bin lib src"
	echo
	echo "Options:"
	echo "-t|--target [string] the fully qualified name of the main class"
	echo "-s|--standalone [flag] compile all dependencies into the final jar"
	echo "-l|--lib [string] path to all libraries, defaults to lib"
	echo "-r|--run [flag] run the program upon compilation"
	echo "-h|--help"
	exit
fi 

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

# carefully remove previous builds
mkdir -p bin lib resources
cd bin
find . -type f -name "*.class" -delete
rm -f *.jar
rm -f "$manifest"
find . -type d -empty -delete

# get java dependencies
require=()
while IFS=  read -r -d $'\0'; do
    require+=("$REPLY")
done < <(find "../$LIBPATH" -name *.jar -print0) 

# optional:
# pack all libraries into this jra
# to create a completely standalone
# jar
if [[ $STANDALONE == true ]]; then
	for req in "${require[@]}"
	do
		# naively unpack the entire jar
		echo "Unpacking $req..."
		jar xf "$req" 
	done
	# delete any non-class files 
	# if you need external files, 
	# they belong in the resources folder
	echo "Cleaning up..."
	find . -type f -not -name "*.class" -delete 
	# remove any ghost directories
	find . -type d -empty -delete
fi 

# build class_path
path_seperator=( ":" ";" ":" ) 
class_path="."
for req in "${require[@]}"
do
	class_path="$class_path${path_seperator[$platform]}$req"
done

# create Manifest
echo "Main-Class: $TARGET" > $MANIFEST
echo "Class-Path: ." >> $MANIFEST
for req in "${require[@]}"
do
 echo "  $req" >> $MANIFEST
done

echo Compiling
# get source files
find .. -name "*.java" > sources
javac -cp "$class_path" @sources -d . -Xlint:deprecation -Xlint:unchecked
rm -f sources

echo Packaging Jar
jar cmf $MANIFEST main.jar . ../resources 

if [[ $RUN == true ]]; then
	echo Running
	java -jar ./main.jar 
else
	echo Done
fi

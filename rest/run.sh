#!/bin/bash
#java -cp "./lib/commons-lang3-3.5/*.jar;./lib/*;./lib/*;./lib/jaxrs-ri/ext/*;./lib/jaxrs-ri/api/*;../lib/jaxrs-ri/lib/*;./lib/jetty-distribution-9.4.3.v20170317/lib/*;./bin/;./lib/jetty-distribution-9.4.3.v20170317/modules/sessions/*.jar" com.parm.server.Main

#!bin/bash
lib_path="../lib/"
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

#cd "./bin/com/parm/server/" 
cd bin
java -cp "$class_path" com.parm.server.Main

#!bin/bash

HELPTEXT=`cat <<EOF 
	This script will neatly package your java project.
	Recommended Project Structure
	.. compile_run.sh bin lib src
	
	Options:
	-s|--standalone [flag] compile all dependencies into the final jar
	-l|--lib [string] path to all libraries, defaults to lib
	-r|--run [flag] run the program upon compilation
	-d|--dev [flag] compile the program for development environment
	-h|--help
EOF`

# program defaults
STANDALONE=false
LIBPATH="lib"
MANIFEST="Manifest.mf"
HELP=false
RUN=false 
DEV=false
OPTIONS="" 

# get options
while [[ $# -gt 0 ]]
do
	key="$1"

	case $key in
		-s|--standalone)
			STANDALONE=true
			OPTIONS="$OPTIONS -s"
			;;
		-l|--lib)
			LIBPATH="$2"
			OPTIONS="$OPTIONS -l $LIBPATH"
			shift # past argument
			;;
		-n|--name)
			NAME="$2"
			OPTIONS="$OPTIONS -n $NAME"
			shift # past argument
			;;
		-r|--run)
			OPTIONS="$OPTIONS -r"
			RUN=true
			;;
		-d|--dev)
			DEV=true
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

# set target & environment variables
TARGET=com.patrickmichaelsen.livebasketball.Main
if [[ $DEV == true ]]; then 
	export URI="http://localhost:8081"
	export HOST=localhost
	export PORT=6789
else
	export URI="http://ec2-35-167-51-118.us-west-2.compute.amazonaws.com"
	export HOST="ec2-35-167-51-118.us-west-2.compute.amazonaws.com"
	export PORT=6789
fi

if [[ $HELP == true ]]; then 
	echo "$HELPTEXT"
	exit
fi 

bash compile_run.sh $OPTIONS -t $TARGET

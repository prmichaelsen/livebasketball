#!/bin/bash

# program defaults
BUILD=false
DEPLOY=false
RUN=false

# get options
while [[ $# -gt 0 ]]
do
	key="$1"

	case $key in
		-b|--build)
			BUILD=true
			;;
		-d|--deploy)
			DEPLOY=true
			;;
		-r|--run)
			RUN=true
			;;
		*)
			# unknown option
			;;
esac
shift # past argument or value
done 

if [[ $BUILD == true ]]; then 
	bash build.sh
fi 
mkdir -p build
rm -rf build
mkdir -p build
cp ./bin/*.jar ./build
commit=`git rev-parse --short HEAD`
mv ./build/* ./build/server_$commit.jar
touch post-deploy.sh
chmod aog+x post-deploy.sh 
PM2=/home/ubuntu/.npm-global/bin/pm2
echo "cd bin && $PM2 start --name server_$commit java -- -jar server_$commit.jar && $PM2 save" > post-deploy.sh
ssh livebasketball 'mkdir -p $HOME/server/bin'
if [[ $DEPLOY == true ]]; then 
	sftp -b batch_deploy_jar livebasketball
fi 
if [[ $RUN == true ]]; then 
	sftp -b batch_deploy_post_script livebasketball
	ssh livebasketball 'cd $HOME/server/ && chmod aog+x post-deploy.sh && bash post-deploy.sh'
fi 

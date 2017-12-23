#!/bin/sh

pid=`ps -ef | grep check | grep -v grep | awk '{print $2}'`
if [ ! "$pid" == "" ];
then
	kill -9 $pid
fi

case "`uname`" in
    Linux)
		bin_abs_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_abs_path=`cd $(dirname $0); pwd`
		;;
esac
base=${bin_abs_path}

appName=OPPOGateway

while [ 1 = 1 ]
do
	tmp=`ps -ef | grep java | grep ${appName}`
	if [[ "$tmp" = *${appName}* ]];
	then
		echo "${appName} is exists"
	else
		sh $base/start.sh
		echo "${appName} is start"
	fi
	sleep 300
done


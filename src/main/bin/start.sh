#!/bin/sh

case "`uname`" in
    Linux)
		bin_abs_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_abs_path=`cd $(dirname $0); pwd`
		;;
esac
base=${bin_abs_path}/..

appName=IMServer

get_pid() {
        STR=$1
        PID=$2
        if [ ! -z "$PID" ]; then
                JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep "$PID"|grep -v grep|awk '{print $2}'`
            else 
                JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep -v grep|awk '{print $2}'`
        fi
    echo $JAVA_PID;
}

pid=`get_pid "appName=${appName}"`
if [ ! "$pid" = "" ]; then
	echo "${appName} is running."
	exit -1;
fi

if [ "$1" = "debug" ]; then
	DEBUG_PORT=$2
	DEBUG_SUSPEND="n"
	JAVA_DEBUG_OPT="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
fi

if [ "$1" = "jmx" ]; then
        JMX_PORT=$2
        JAVA_JMX_OPT="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
fi


JAVA_OPTS="-server -Djava.io.tmpdir=$base/tmp -DappName=${appName} -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"

MEM_OPTS="-Xms4g -Xmx4g -XX:NewRatio=2 -XX:PermSize=64m -XX:MaxPermSize=128m"

OOM_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$base/../gateway.hprof -XX:OnOutOfMemoryError=$base/bin/restart.sh"

GC_OPTS="-XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75"

cd $base
java $JAVA_OPTS $MEM_OPTS $OOM_OPTS $GC_OPTS $JAVA_DEBUG_OPT $JAVA_JMX_OPT -classpath '.:lib/*:conf' com.oppo.gateway.GatewayMain 1>>logs/server.log 2>&1 &

echo $! > $base/server.pid

echo OK!`cat $base/server.pid`


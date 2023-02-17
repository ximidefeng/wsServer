#!/bin/sh
#定义程序名 及jar包的名
PROJECT_NAME=websocket-server-1.0.0.jar
LOG_DIR=./logs
## 编写判断程序是否正在运行的方法
isExist(){
	## 首先查找进程号
    pid=$(ps -ef | grep ${PROJECT_NAME} | grep -v "grep" | awk '{print $2}')
	echo $(pid)
    ## 如果进程号不存在，则返回0 否则返回1
    if [ -z "${pid}" ]; then
    	return 0
    else
    	return 1
    fi
}
## 编写启动程序方法
start(){
	## 调用 判断程序是否正在运行的方法
    isExist
    ## 判断方法返回值是否等于0 ，等于则不存在
    if [ $? -eq "0" ]; then
	#日志文件夹不存在，则创建
        if [ ! -d "${LOG_DIR}" ]; then
           mkdir "${LOG_DIR}"
        fi
    	echo "${PROJECT_NAME} is starting ......"
    	nohup java -Xms1024m -Xmx2048m -Dloader.path=./lib  -jar ${PROJECT_NAME} >"${LOG_DIR}"/catalina.out 2>&1 &
    	echo "${PROJECT_NAME} startup success"
    else
    	echo "${PROJECT_NAME} is running, pid=${pid} "
    fi
}
## 编写停止程序的方法
stop(){
    ## 调用 判断程序是否正在运行
    isExist
    ## 判断是否存在，返回值0不存在
    if [ $? -eq "0" ]; then
    	echo "${PROJECT_NAME} is not running ......"
    else
    	echo "${PROJECT_NAME} is running, pid=${pid}, prepare kill it "
    	kill -9 ${pid}
    	echo "${PROJECT_NAME} has been successfully killed ......"
    fi
}
## 编写重启方法
restart(){
    ## 先停止再启动
    stop
    start
}
 
 
## 程序最开始执行的
## 根据用户输入，判断执行方法
case "$1" in
	"start")
		start
		;;
	"stop")
		stop
		;;
	"restart")
		restart
		;;
	*)
		echo "please enter the correct commands: "
		echo "such as : sh startup.sh [ start | stop | restart ]"
		;;
esac
 

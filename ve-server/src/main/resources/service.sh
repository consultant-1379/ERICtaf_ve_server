#!/bin/bash


# VE server version
version=${project.version}


# Finds location of settings file and sets variable identifying this file. 
function setSettingsFileLocation() {
  local file=""
  if [ $1 ]; then
    file=$1
  else
    local pwd="`pwd`"
    file=${pwd}"/settings.yml"
  fi
  if [ -e $file ] && [ -r $file ] ; then
    settingsFileLocation="$file"
  else
    echo "Settings file does not exist or is not readable"
    exit 1;
  fi
  return 0;
}

# Sets variables for all required properties for service. Further, if the 
# properties file referred to by the environment variable $VE_SERVER_PROPERTIES 
# contains new values for any of the following:
# $javaHome $serviceUser $serviceGroup $applDir $serviceLogFile $pidFile
# these variables are updated with the values from the properties file. 
function readAndSetProperties () {
  serviceNameLo="ve-server"                       # service name with the first letter in lowercase
  serviceName="ve-server"                         # service name
  javaHome="/usr/java/latest"                     # Absolute path to Java 7 installation (location of `bin` dir)
  serviceUser="root"                              # OS user name for the service
  serviceGroup="root"                             # OS group name for the service
  applDir="/opt/$serviceNameLo"                   # home directory of the service application
  serviceLogFile="/var/log/$serviceNameLo.log"    # log file for StdOut/StdErr
  pidFile="/var/run/$serviceNameLo.pid"           # name of PID file (PID = process ID number)
  maxShutdownTime=15                              # maximum number of seconds to wait for the daemon to terminate normally
  javaCommand="java"                              # name of the Java launcher without the path
  javaCommandLineKeyword="ve-server.jar"          # a keyword that occurs on the commandline, used 
                                                  # to detect an already running service process and 
                                                  # to distinguish it from others
  if [ -e $VE_SERVER_PROPERTIES ] && [ -r $VE_SERVER_PROPERTIES ] ; then
    # Read properties and place them into $javaHome $serviceUser $serviceGroup $applDir $serviceLogFile $pidFile
    . $VE_SERVER_PROPERTIES
  else
    echo 'Properties file does not exist or is not readable'
    echo '(The environment variable \$VE_SERVER_PROPERTIES must be set to the absolut path to a properties file)'
    exit 1;
  fi
    javaExe="$javaHome/bin/$javaCommand"              # file name of the Java application launcher executable
    javaArgs="-Xms256m -Xmx512m -server -jar $applDir/lib/$serviceNameLo-$version.jar"     # arguments for Java launcher
  return 0;
}


# Makes the file $1 writable by the group $serviceGroup.
function makeFileWritable () {
  local filename="$1"
  touch $filename || return 1
  chgrp $serviceGroup $filename || return 1
  chmod g+w $filename || return 1
  return 0;
}

# Returns 0 if the process with PID $1 is running.
function checkProcessIsRunning {
  local pid="$1"
  if [ -z "$pid" -o "$pid" == " " ]; then return 1; fi
  if [ ! -e /proc/$pid ]; then return 1; fi
  return 0; 
}

# Returns 0 if the process with PID $1 is our Java service process.
function checkProcessIsOurService {
  local pid="$1"
  if [ "$(ps -p $pid --no-headers -o comm)" != "$javaCommand" ]; then return 1; fi
  grep -q --binary -F "$javaCommandLineKeyword" /proc/$pid/cmdline
  if [ $? -ne 0 ]; then return 1; fi
  return 0; 
}

#: Returns 0 when the service is running and sets the variable $pid to the PID.
function getServicePID {
  if [ ! -f $pidFile ]; then return 1; fi
  pid="$(<$pidFile)"
  checkProcessIsRunning $pid || return 1
  checkProcessIsOurService $pid || return 1
  return 0; 
}

function startServiceProcess {
  cd $applDir || return 1
  rm -f $pidFile
  makeFileWritable $pidFile || return 1
  makeFileWritable $serviceLogFile || return 1
  javaCommandLine="$javaExe $javaArgs $settingsFileLocation" # command line to start the Java service application
  cmd="nohup $javaCommandLine >>$serviceLogFile 2>&1 & echo \$! >$pidFile"
  su -m $serviceUser -s $SHELL -c "$cmd" || return 1
  sleep 5
  pid="$(<$pidFile)"
  if checkProcessIsRunning $pid; then :; else
    echo -ne "\n$serviceName start failed, see logfile."
    return 1
  fi
  return 0; 
}

function stopServiceProcess {
  kill $pid || return 1
  for ((i=0; i<maxShutdownTime*10; i++)); do
    checkProcessIsRunning $pid
    if [ $? -ne 0 ]; then
       rm -f $pidFile
       return 0
    fi
    sleep 0.1
  done
  echo -e "\n$serviceName did not terminate within $maxShutdownTime seconds, sending SIGKILL..."
  kill -s KILL $pid || return 1
  local killWaitTime=15
  for ((i=0; i<killWaitTime*10; i++)); do
    checkProcessIsRunning $pid
    if [ $? -ne 0 ]; then
       rm -f $pidFile
       return 0
    fi
    sleep 0.1
  done
  echo "Error: $serviceName could not be stopped within $maxShutdownTime+$killWaitTime seconds!"
  return 1; 
}

function startService() {
  getServicePID
  if [ $? -eq 0 ]; then echo -n "$serviceName is already running"; RETVAL=0; return 0; fi
  echo -n "Starting $serviceName   "
  startServiceProcess $1 
  if [ $? -ne 0 ]; then RETVAL=1; echo "failed"; return 1; fi
  echo "started PID=$pid"
  RETVAL=0
  return 0; 
}

function stopService {
  getServicePID
  if [ $? -ne 0 ]; then echo -n "$serviceName is not running"; RETVAL=0; echo ""; return 0; fi
  echo -n "Stopping $serviceName   "
  stopServiceProcess
  if [ $? -ne 0 ]; then RETVAL=1; echo "failed"; return 1; fi
  echo "stopped PID=$pid"
  RETVAL=0
  return 0; 
}

function checkServiceStatus {
  echo -n "Checking for $serviceName:   "
  if getServicePID; then
    echo "running PID=$pid"
    RETVAL=0
  else
    echo "stopped"
    RETVAL=3
  fi
  return 0; 
}


function usage() {
  echo "Usage: $1 {start [config_file]|stop|restart [config_file]|status}"
  echo "To start server a settings file must be provided. It there is no" 
  echo "settings file given as an argument, and there is a file named"
  echo "'settings.yml' in the current directory, this file will be used"
  echo "as a settings file. Also, set '\$VE_SERVER_PROPERTIES' to the properties file"
  RETVAL=1
  return 0;
}



function main {
  RETVAL=0
  case "$1" in
    start)                                               # starts the Java program as a Linux service
      readAndSetProperties
      setSettingsFileLocation $2
      startService
      ;;
    stop)                                                # stops the Java program service
      stopService
      ;;
    restart)                                             # stops and restarts the service
      readAndSetProperties
      setSettingsFileLocation $2
      stopService && startService
      ;;
    status)                                              # displays the service status
      checkServiceStatus
      ;;
    *)
    usage $0 
    ;;
  esac
  exit $RETVAL
}

main $1 $2

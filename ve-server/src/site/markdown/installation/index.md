# Eiffel Visualization Engine (VE) Server

## Installation

Download the (VE) Server from the [Eiffel Nexus](https://eiffel.lmera.ericsson.se/nexus/content/repositories/releases/com/ericsson/duraci/ve-server/). Select version and download the `ve-server-<VERSION>-package.zip` file.

Unzip file to get a directory: `lib` which contains `ve-server-<VERSION>.jar` and other jars. The distributed zip file also includes a control script `service.sh`, which may be used to start, restart, stop and check the status of the server. 

### Settings File

The server requires the path to a valid configuration file, which must either be given as an argument (with an absolute path), or if this is not the case, a file named `settings.yml` must be present in the currrent working directory, and will then be used as the configuration file for the server. How the configuration file for the VE server should be setup is described in the section [Eiffel Visualization Engine (VE) Server Configuration ](../configuration/index.html).

### Properties File

In addition to the settings file, the VE server requires a properties file. This file should be stored outside of the VE server installation area, to maintain the same configuration when upgrading. Set the environment variable `$VE_SERVER_PROPERTIES` to the absolut path to the properties file. 

The following may work as a template of a properties file:


    javaHome=/usr/java/latest
    serviceUser=<USER>
    serviceGroup=<GROUP>
    applDir=<USER_HOME_DIR>/eiffel/VE/ve-server
    serviceLogFile=$applDir/ve-server.log
    pidFile=$applDir/ve-server.pid 


As seen above, if a value has been specified previously in the properties file, (such as `$applDir`) this value may be used later as variable. Useful if for instance, the log file and pid file should be placed below the same directory.

The values which must be specified are the following:
<table cellpadding="5px" border="0">
  <tr>
    <td><code>javaHome</code></td>    
    <td>Absolute path to Java 7 installation, where `bin` dir is located</td>
  </tr>
  <tr>
    <td><code>serviceUser</code></td>
    <td>Service will be run under this username</td>
  </tr>
  <tr>
    <td><code>serviceGroup</code></td>
    <td>Service will be run under this group</td>
  </tr>
  <tr>
    <td><code>applDir</code></td>
    <td>'Home directory' of service. This directory must contain the directory namned `lib' which was packed into the zipfile.</td>
  </tr>
  <tr>
    <td><code>serviceLogFile</code></td>
    <td>Log file. <code>chmod</code> to ensure user running server has write permissions in directory</td>
  </tr>
  <tr>
    <td><code>pidFile</code></td>
    <td>File where PID (process ID) is stored. <code>chmod</code> to ensure user running server has write permissions in directory where pidFile should reside.</td>
  </tr>
</table>

### Notes
Here are som small pointers if difficulties are encountered during usage.

** If the error message `Ambiguous output redirect` is received during startup; try setting the environmental variable $SHELL to `/bin/bash`. 

** If the script fails due to write, for instance the pid file, ensure that the user owning the server process has write permissions in the directory that will contain the file. 
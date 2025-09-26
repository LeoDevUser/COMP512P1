#!/bin/bash
# Usage: ./run_server.sh [<rmi_name>]
./run_rmi.sh > /dev/null 2>&1

# Get the current directory and URL-encode spaces
CODEBASE_PATH=$(pwd | sed 's/ /%20/g')
java -Djava.rmi.server.codebase="file:${CODEBASE_PATH}/" -cp "." Server.RMI.RMIResourceManager $1

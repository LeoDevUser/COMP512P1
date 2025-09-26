#!/bin/bash
# Usage: ./run_server.sh [<rmi_name>]
./run_rmi.sh > /dev/null 2>&1

# Get the current directory and URL-encode spaces
java -Djava.rmi.server.codebase="file://$(pwd | sed 's/ /%20/g')/" -cp "." Server.RMI.RMIResourceManager $1

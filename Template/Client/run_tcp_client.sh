#!/bin/bash
# Usage: ./run_tcp_client.sh [<server_hostname> [<server_port>]]

# Simple classpath - just what we need
java -cp "../Server/RMIInterface.jar:../Server:." Client.TCPClient $1 $2 $3

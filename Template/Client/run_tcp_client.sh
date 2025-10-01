#!/bin/bash
# Usage: ./run_tcp_client.sh [<server_host> [<server_port>] [<server_name>]]

java -cp "../Server/RMIInterface.jar:../Server:." Client.TCPClient $1 $2 $3

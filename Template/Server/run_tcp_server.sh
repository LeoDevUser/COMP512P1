#!/bin/bash
# Usage: ./run_tcp_server.sh [<server_name> [<port>]]

# Default values
DEFAULT_SERVER_NAME="Server"
DEFAULT_PORT="4014"

# Get server name and port from arguments
SERVER_NAME=${1:-$DEFAULT_SERVER_NAME}
PORT=${2:-$DEFAULT_PORT}

java -cp "." Server.TCP.TCPResourceManager $SERVER_NAME $PORT

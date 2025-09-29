#!/bin/bash
# Usage: ./run_tcp_middleware.sh [<server_name> [<port>] [<flight_host>] [<flight_port>] [<car_host>] [<car_port>] [<room_host>] [<room_port>]]

# Default values
DEFAULT_SERVER_NAME="Middleware"
DEFAULT_PORT="3014"
DEFAULT_FLIGHT_HOST="localhost"
DEFAULT_FLIGHT_PORT="4014"
DEFAULT_CAR_HOST="localhost"
DEFAULT_CAR_PORT="5014"
DEFAULT_ROOM_HOST="localhost"
DEFAULT_ROOM_PORT="6014"

# Get server name and port from arguments
SERVER_NAME=${1:-$DEFAULT_SERVER_NAME}
PORT=${2:-$DEFAULT_PORT}
FLIGHT_HOST=${3:-DEFAULT_FLIGHT_HOST}
FLIGHT_PORT=${4:-DEFAULT_FLIGHT_PORT}
CAR_HOST=${5:-DEFAULT_CAR_HOST}
CAR_PORT=${6:-DEFAULT_CAR_PORT}
ROOM_HOST=${7:-DEFAULT_ROOM_HOST}
ROOM_PORT=${8:-DEFAULT_ROOM_PORT}

java -cp "." Server.TCP.TCPMiddleware $SERVER_NAME $PORT $FLIGHT_HOST $FLIGHT_PORT $CAR_HOST $CAR_PORT $ROOM_HOST $ROOM_PORT
# Names for flight, car, and room servers are Flights, Cars, Rooms respectively
#!/bin/bash
# Usage: ./run_tcp_servers.sh [machine1] [machine2] [machine3] [machine4]
# If no machines specified, defaults to localhost for all

# Default to localhost for all machines
DEFAULT_MACHINE="localhost"

# Get machines from arguments or use defaults
MACHINE1="${1:-$DEFAULT_MACHINE}"
MACHINE2="${2:-$DEFAULT_MACHINE}"
MACHINE3="${3:-$DEFAULT_MACHINE}"
MACHINE4="${4:-$DEFAULT_MACHINE}"

# Get current directory
CURRENT_DIR="$(pwd)"

# Port configuration
FLIGHT_PORT=4014
CAR_PORT=5014
ROOM_PORT=6014
MIDDLEWARE_PORT=3014

echo "Starting TCP servers on:"
echo "  Flights:    $MACHINE1:$FLIGHT_PORT"
echo "  Cars:       $MACHINE2:$CAR_PORT"
echo "  Rooms:      $MACHINE3:$ROOM_PORT"
echo "  Middleware: $MACHINE4:$MIDDLEWARE_PORT"

# Helper function to create command based on whether it's localhost or remote
create_command() {
    local machine=$1
    local command=$2
    
    if [[ "$machine" == "localhost" || "$machine" == "127.0.0.1" ]]; then
        # Local execution - no SSH needed
        echo "$command"
    else
        # Remote execution - use SSH
        echo "ssh -t \"${machine}\" 'cd \"${CURRENT_DIR}\" > /dev/null 2>&1; echo -n \"Connected to \"; hostname; $command'"
    fi
}

# Create commands for each server
CMD1=$(create_command "$MACHINE1" "./run_tcp_server.sh Flights ${FLIGHT_PORT}")
CMD2=$(create_command "$MACHINE2" "./run_tcp_server.sh Cars ${CAR_PORT}")
CMD3=$(create_command "$MACHINE3" "./run_tcp_server.sh Rooms ${ROOM_PORT}")
CMD4=$(create_command "$MACHINE4" "sleep 0.5s; ./run_tcp_middleware.sh Middleware ${MIDDLEWARE_PORT} ${MACHINE1} ${FLIGHT_PORT} ${MACHINE2} ${CAR_PORT} ${MACHINE3} ${ROOM_PORT}")

tmux new-session \; \
	split-window -h \; \
	split-window -v \; \
	split-window -v \; \
	select-layout main-vertical \; \
	select-pane -t 1 \; \
	send-keys "$CMD1" C-m \; \
	select-pane -t 2 \; \
	send-keys "$CMD2" C-m \; \
	select-pane -t 3 \; \
	send-keys "$CMD3" C-m \; \
	select-pane -t 0 \; \
	send-keys "$CMD4" C-m \;

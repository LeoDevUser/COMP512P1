./run_rmi.sh > /dev/null

echo "Edit file run_middleware.sh to include instructions for launching the middleware"
echo '  $1 - name of this server'
echo '  $2 - hostname of Flights'
echo '  $3 - hostname of Cars'
echo '  $4 - hostname of Rooms'

java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware $1 $2 $3 $4

# Please note that the flight, car, and room servers must be named Flights, Cars, Rooms respectively

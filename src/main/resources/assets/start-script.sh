#Auto generated script, by KiloEssentials
#Initializes the startup script
run() {
  echo "Maximum memory is set to %MAXIMUM_RAM%"
  %STARTUP_CODE%
}
start() {
  echo "Starting the %SERVER_NAME%..."
  run
}
restart() {
  echo "Restarting the server..."
  run
}

#Starts the server
start
#Checks if the 'RESTARTME" File exists to re-start the server
while [ -e RESTARTME ]; do
  rm RESTARTME
  restart
done
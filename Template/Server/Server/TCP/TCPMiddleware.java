package Server.TCP;

import Server.Common.Middleware;
import Server.Common.RMItem;
import Server.Common.ResourceManager;

import java.io.*;
import java.net.*;
import java.util.*;

import Server.TCP.TCPResourceManagerProxy;

public class TCPMiddleware extends Middleware {
    private static int port = 3014;
    private static String s_serverName = "server";
    private ServerSocket serverSocket;

    private static String s_flightServerName = "Flights";
    private static String s_flightServerHost = "localhost";
    private static int s_flightPort = 5014;
    private static String s_carServerName = "Cars";
    private static String s_carServerHost = "localhost";
    private static int s_carPort = 6014;
    private static String s_roomServerName = "Rooms";
    private static String s_roomServerHost = "localhost";
    private static int s_roomPort = 7014;

	public static void main(String args[]) {
		if (args.length > 0) {
			s_serverName = args[0];
		}
		if (args.length > 1) {
			port = Integer.parseInt(args[1]);
		}
        if (args.length > 2) {
            s_flightServerHost = args[2];
        }
        if (args.length > 3) {
            s_flightPort = Integer.parseInt(args[3]);
        }
        if (args.length > 4) {
            s_carServerHost = args[4];
        }
        if (args.length > 5) {
            s_carPort = Integer.parseInt(args[5]);
        }
        if (args.length > 6) {
            s_roomServerHost = args[6];
        }
        if (args.length > 7) {
            s_roomPort = Integer.parseInt(args[7]);
        }
        if (args.length > 8) {
            System.err.println("Usage: java Server.TCP.TCPMiddleware [server_name [server_port] [flights_host] [flight_port] [cars_host] [car_port] [rooms_host] [room_port]]");
            System.exit(1);
        }

		try {
			TCPMiddleware middleware = new TCPMiddleware(s_serverName);
            middleware.connectServer();
			middleware.start();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

    public void connectServer() {
        connectServer(port, s_flightServerHost, s_carServerHost, s_roomServerHost);
    }
    
    public void connectServer(int port, String flightHost, String carHost, String roomHost) {
        try {
            boolean first = true;
            while (true) {
                try {
                    flightResourceManager = new TCPResourceManagerProxy(flightHost, s_flightPort);
                    System.out.println("Connected to '" + s_flightServerName + "' server [" + flightHost + ":" + s_flightPort + "]");
                    break;
                } catch (IOException e) {
                    if (first) {
                        System.out.println("Waiting for '" + s_flightServerName + "' server [" + flightHost + ":" + s_flightPort + "]");
                        first = false;
                    }
                }
                Thread.sleep(500);
            }

            first = true;
            while (true) {
                try {
                    carResourceManager = new TCPResourceManagerProxy(carHost, s_carPort);
                    System.out.println("Connected to '" + s_carServerName + "' server [" + carHost + ":" + s_carPort + "]");
                    break;
                } catch (IOException e) {
                    if (first) {
                        System.out.println("Waiting for '" + s_carServerName + "' server [" + carHost + ":" + s_carPort + "]");
                        first = false;
                    }
                }
                Thread.sleep(500);
            }

            first = true;
            while (true) {
                try {
                    roomResourceManager = new TCPResourceManagerProxy(roomHost, s_roomPort);
                    System.out.println("Connected to '" + s_roomServerName + "' server [" + roomHost + ":" + s_roomPort + "]");
                    break;
                } catch (IOException e) {
                    if (first) {
                        System.out.println("Waiting for '" + s_roomServerName + "' server [" + roomHost + ":" + s_roomPort + "]");
                        first = false;
                    }
                }
                Thread.sleep(500);
            }

        } catch (Exception e) {
            System.err.println("Server exception: Uncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() throws IOException {
		serverSocket = new ServerSocket(port);
		System.out.println("'" + m_name + "' TCP server started on port " + port);
		
		//add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run(){
				try {
					if(serverSocket != null && !serverSocket.isClosed()){
						serverSocket.close();
						System.out.println("'" + m_name + "' server shutdown");
					}
				} catch (IOException e) {
					System.err.println("Error during shutdown: " + e.getMessage());
				} 
			}
		});


		while(true) {
			try {
				Socket clientSocket = serverSocket.accept();
				new Thread(new ClientHandler(clientSocket)).start();
			} catch (IOException e) {
				if(!serverSocket.isClosed()) {
					System.err.println("Error accepting client connection: " + e.getMessage());
				}
				break;
			}
		}
	}

    private class ClientHandler implements Runnable {
		private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;


		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		@Override
		public void run() {
			try {
				out = new ObjectOutputStream(clientSocket.getOutputStream());
				in = new ObjectInputStream(clientSocket.getInputStream());

				while(true) {
					try {
						//Read method and parameters
						String methodName = (String) in.readObject();
						Object[] params = (Object[]) in.readObject();

						Object result = executeMethod(methodName,params);
						out.writeObject(result);
						out.flush();
					} catch (EOFException e) {
						//client has disconnected
						break;
					} catch (Exception e) {
						// Send exception back to client
                        out.writeObject(new RuntimeException(e.getMessage()));
                        out.flush();
					}
				}
			} catch (IOException e) {
				System.err.println("Client handler error: " + e.getMessage());
            } finally {
				//close our resources
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client connection: " + e.getMessage());
                }
			}
		}
	}

	private Object executeMethod(String methodName, Object[] params) throws Exception {
		switch(methodName){
			case "newCustomer":
				if (params.length == 0) {
					return newCustomer();
				} else {
					return newCustomer((Integer) params[0]);
				}
			case "deleteCustomer":
				return deleteCustomer((Integer) params[0]);
			case "addFlight":
				return addFlight((Integer) params[0],(Integer) params[1],(Integer) params[2]);
			case "addCars":
				return addCars((String) params[0], (Integer) params[1], (Integer) params[2]);
			case "addRooms":
				return addRooms((String) params[0], (Integer) params[1], (Integer) params[2]);
			case "deleteFlight":
				return deleteFlight((Integer) params[0]);
			case "deleteCars":
				return deleteCars((String) params[0]);
			case "deleteRooms":
				return deleteRooms((String) params[0]);
			case "queryFlight":
				return queryFlight((Integer) params[0]);
			case "queryCars":
				return queryCars((String) params[0]);
			case "queryRooms":
				return queryRooms((String) params[0]);
			case "queryFlightPrice":
				return queryFlightPrice((Integer) params[0]);
			case "queryCarsPrice":
				return queryCarsPrice((String) params[0]);
			case "queryRoomsPrice":
				return queryRoomsPrice((String) params[0]);
			case "queryCustomerInfo":
				return queryCustomerInfo((Integer) params[0]);
			case "reserveFlight":
				return reserveFlight((Integer) params[0], (Integer) params[1]);
			case "reserveCar":
				return reserveCar((Integer) params[0], (String) params[1]);
			case "reserveRoom":
				return reserveRoom((Integer) params[0], (String) params[1]);
			case "bundle":
				return bundle((Integer) params[0], (Vector<String>) params[1], (String) params[2], (boolean) params[3], (boolean) params[4]);
			case "getName":
				return getName();
			case "readData":
				return readData((String) params[0]);
			case "writeData":
				writeData((String) params[0], (RMItem) params[1]);
				return null;
			case "removeData":
				removeData((String) params[0]);
				return null;
			default:
				throw new UnsupportedOperationException("Unknown method: " + methodName);
		}
		
	}

    public TCPMiddleware(String name) {super(name);}
}

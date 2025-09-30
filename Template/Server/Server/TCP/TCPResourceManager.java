package Server.TCP;

import Server.Interface.*;
import Server.Common.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPResourceManager extends ResourceManager {
	private static int port = 3014; 
	private static String s_serverName = "Server";
	private ServerSocket serverSocket;

	public static void main(String args[]) {
		if (args.length > 0) {
			s_serverName = args[0];
		}
		if (args.length > 1) {
			port = Integer.parseInt(args[1]);
		}

		try {
			TCPResourceManager server = new TCPResourceManager(s_serverName);
			server.start();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TCPResourceManager(String name){
		super(name);
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
}

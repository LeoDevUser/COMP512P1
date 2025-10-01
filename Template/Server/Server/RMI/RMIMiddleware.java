package Server.RMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Server.Common.Middleware;
import Server.Interface.IResourceManager;

public class RMIMiddleware extends Middleware {

    private static int s_serverPort = 3014;
    private static String s_serverName = "Middleware";
    private static String s_flightServerName = "Flights";
	private static String s_flightHost = "localhost";
    private static String s_carServerName = "Cars";
	private static String s_carHost = "localhost";
    private static String s_roomServerName = "Rooms";
	private static String s_roomHost = "localhost";
    private static String s_rmiPrefix = "group_14_";

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            s_serverName = args[0];
        }
        if (args.length > 1)
        {
            s_flightHost = args[1];
        }
        if (args.length > 2)
        {
            s_carHost = args[2];
        }
        if (args.length > 3)
        {
            s_roomHost = args[3];
        }
        if (args.length > 4)
        {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
            System.exit(1);
        }

        // Get a reference to the RMIRegister
        try {
            RMIMiddleware middleware = new RMIMiddleware("Middleware");
            middleware.connectServer();

            // Dynamically generate the stub (client proxy)
			IResourceManager resourceManager = (IResourceManager)UnicastRemoteObject.exportObject(middleware, 0);

			// Bind the remote object's stub in the registry; adjust port if appropriate
			Registry l_registry;
			try {
				l_registry = LocateRegistry.createRegistry(3014);
			} catch (RemoteException e) {
				l_registry = LocateRegistry.getRegistry(3014);
			}
			final Registry registry = l_registry;
			registry.rebind(s_rmiPrefix + s_serverName, resourceManager);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						registry.unbind(s_rmiPrefix + s_serverName);
						System.out.println("'" + s_serverName + "' resource manager unbound");
					}
					catch(Exception e) {
						System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
						e.printStackTrace();
					}
				}
			});                                       
			System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_rmiPrefix + s_serverName + "'");
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
        }
    }

    public void connectServer() {
        connectServer(s_serverPort, s_flightHost, s_carHost, s_roomHost);
    }

    public void connectServer(int port, String flightHost, String carHost, String roomHost) {
        try {
			boolean first = true;
			while (true) {
				try {
					Registry registry = LocateRegistry.getRegistry(flightHost, port);
					flightResourceManager = (IResourceManager)registry.lookup(s_rmiPrefix + s_flightServerName);
					System.out.println("Connected to '" + s_flightServerName + "' server [" + flightHost + ":" + port + "/" + s_rmiPrefix + s_flightServerName + "]");
					break;
				}
				catch (NotBoundException|RemoteException e) {
					//System.out.println("DEBUG: Exception caught: " + e.getClass().getName() + ": " + e.getMessage());
					//e.printStackTrace();
					if (first) {
						System.out.println("Waiting for '" + s_flightServerName + "' server [" + s_flightHost + ":" + port + "/" + s_rmiPrefix + s_flightServerName + "]");
						first = false;
					}
				}
				Thread.sleep(500);
			}

            first = true;
			while (true) {
				try {
					Registry registry = LocateRegistry.getRegistry(carHost, port);
					carResourceManager = (IResourceManager)registry.lookup(s_rmiPrefix + s_carServerName);
					System.out.println("Connected to '" + s_carServerName + "' server [" + carHost + ":" + port + "/" + s_rmiPrefix + s_carServerName + "]");
					break;
				}
				catch (NotBoundException|RemoteException e) {
					//System.out.println("DEBUG: Exception caught: " + e.getClass().getName() + ": " + e.getMessage());
					//e.printStackTrace();
					if (first) {
						System.out.println("Waiting for '" + s_carServerName + "' server [" + s_carHost + ":" + port + "/" + s_rmiPrefix + s_carServerName + "]");
						first = false;
					}
				}
				Thread.sleep(500);
			}

            first = true;
			while (true) {
				try {
					Registry registry = LocateRegistry.getRegistry(roomHost, port);
					roomResourceManager = (IResourceManager)registry.lookup(s_rmiPrefix + s_roomServerName);
					System.out.println("Connected to '" + s_roomServerName + "' server [" + roomHost + ":" + port + "/" + s_rmiPrefix + s_roomServerName + "]");
					break;
				}
				catch (NotBoundException|RemoteException e) {
					//System.out.println("DEBUG: Exception caught: " + e.getClass().getName() + ": " + e.getMessage());
					//e.printStackTrace();
					if (first) {
						System.out.println("Waiting for '" + s_roomServerName + "' server [" + s_roomHost + ":" + port + "/" + s_rmiPrefix + s_roomServerName + "]");
						first = false;
					}
				}
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
    }

    public RMIMiddleware(String name) {super(name);}
}

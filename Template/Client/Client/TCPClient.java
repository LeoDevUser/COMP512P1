package Client;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;

public class TCPClient extends Client {
    private static String s_serverHost = "localhost";
    private static int s_serverPort = 3014;
    private static String s_serverName = "Middleware";
    
    public static void main(String args[]) {
        if (args.length > 0) {
            s_serverHost = args[0];
        }
        if (args.length > 1) {
            s_serverPort = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            System.err.println("Usage: java Client.TCPClient [server_hostname [server_port]]");
            System.exit(1);
        }
        
        try {
            TCPClient client = new TCPClient();
            client.connectServer();
            client.start();
        } catch (Exception e) {
            System.err.println("Client exception: Uncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public TCPClient() {
        super();
    }
    
    @Override
    public void connectServer() {
        connectServer(s_serverHost, s_serverPort, s_serverName);
    }
    
    public void connectServer(String server, int port, String name) {
        try {
            boolean first = true;
            while (true) {
                try {
                    m_resourceManager = new TCPResourceManagerProxy(server, port);
                    System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "]");
                    break;
                } catch (IOException e) {
                    if (first) {
                        System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "]");
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
}

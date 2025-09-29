package Server.TCP;

import Server.Interface.*;
import Server.Common.RMItem;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;

public class TCPResourceManagerProxy implements IResourceManager {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    public TCPResourceManagerProxy(String host, int port) throws IOException {
    this.host = host;
    this.port = port;
    connect();
}

private void connect() throws IOException {
    try {
        socket = new Socket();
        socket.setSoTimeout(5000); // 5 second read timeout
        socket.connect(new InetSocketAddress(host, port), 1000); // 1 second connect timeout
        
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    } catch (IOException e) {
        // Make sure socket is closed on failure
        if (socket != null && !socket.isClosed()) {
            try { socket.close(); } catch (IOException ignored) {}
        }
        throw e; // Re-throw the exception
    }
}
    private Object invokeRemoteMethod(String methodName, Object... params) throws RemoteException {
        try {
            // Ensure connection is alive
            if (socket.isClosed()) {
                connect();
            }
            
            // Send method name and parameters
            out.writeObject(methodName);
            out.writeObject(params);
            out.flush();
            
            // Read response
            Object result = in.readObject();
            
            // Check if it's an exception
            if (result instanceof RuntimeException) {
                throw new RemoteException(((RuntimeException) result).getMessage());
            }
            
            return result;
            
        } catch (IOException | ClassNotFoundException e) {
            throw new RemoteException("Communication error: " + e.getMessage(), e);
        }
    }
    
    public void close() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
    }
    
    // IResourceManager implementation
    @Override
    public RMItem readData(String key) throws RemoteException {
        return (RMItem) invokeRemoteMethod("readData", key);
    }
    
    @Override
    public void writeData(String key, RMItem value) throws RemoteException {
        invokeRemoteMethod("writeData", key, value);
    }
    
    @Override
    public void removeData(String key) throws RemoteException {
        invokeRemoteMethod("removeData", key);
    }
    
    @Override
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return (Boolean) invokeRemoteMethod("addFlight", flightNum, flightSeats, flightPrice);
    }
    
    @Override
    public boolean addCars(String location, int numCars, int price) throws RemoteException {
        return (Boolean) invokeRemoteMethod("addCars", location, numCars, price);
    }
    
    @Override
    public boolean addRooms(String location, int numRooms, int price) throws RemoteException {
        return (Boolean) invokeRemoteMethod("addRooms", location, numRooms, price);
    }
    
    @Override
    public int newCustomer() throws RemoteException {
        return (Integer) invokeRemoteMethod("newCustomer");
    }
    
    @Override
    public boolean newCustomer(int cid) throws RemoteException {
        return (Boolean) invokeRemoteMethod("newCustomer", cid);
    }
    
    @Override
    public boolean deleteFlight(int flightNum) throws RemoteException {
        return (Boolean) invokeRemoteMethod("deleteFlight", flightNum);
    }
    
    @Override
    public boolean deleteCars(String location) throws RemoteException {
        return (Boolean) invokeRemoteMethod("deleteCars", location);
    }
    
    @Override
    public boolean deleteRooms(String location) throws RemoteException {
        return (Boolean) invokeRemoteMethod("deleteRooms", location);
    }
    
    @Override
    public boolean deleteCustomer(int customerID) throws RemoteException {
        return (Boolean) invokeRemoteMethod("deleteCustomer", customerID);
    }
    
    @Override
    public int queryFlight(int flightNumber) throws RemoteException {
        return (Integer) invokeRemoteMethod("queryFlight", flightNumber);
    }
    
    @Override
    public int queryCars(String location) throws RemoteException {
        return (Integer) invokeRemoteMethod("queryCars", location);
    }
    
    @Override
    public int queryRooms(String location) throws RemoteException {
        return (Integer) invokeRemoteMethod("queryRooms", location);
    }
    
    @Override
    public String queryCustomerInfo(int customerID) throws RemoteException {
        return (String) invokeRemoteMethod("queryCustomerInfo", customerID);
    }
    
    @Override
    public int queryFlightPrice(int flightNumber) throws RemoteException {
        return (Integer) invokeRemoteMethod("queryFlightPrice", flightNumber);
    }
    
    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        return (Integer) invokeRemoteMethod("queryCarsPrice", location);
    }
    
    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        return (Integer) invokeRemoteMethod("queryRoomsPrice", location);
    }
    
    @Override
    public boolean reserveFlight(int customerID, int flightNumber) throws RemoteException {
        return (Boolean) invokeRemoteMethod("reserveFlight", customerID, flightNumber);
    }
    
    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        return (Boolean) invokeRemoteMethod("reserveCar", customerID, location);
    }
    
    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        return (Boolean) invokeRemoteMethod("reserveRoom", customerID, location);
    }
    
    @Override
    public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        return (Boolean) invokeRemoteMethod("bundle", customerID, flightNumbers, location, car, room);
    }
    
    @Override
    public String getName() throws RemoteException {
        return (String) invokeRemoteMethod("getName");
    }
}

package Server.Common;

import Server.Interface.*;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Vector;

public abstract class Middleware implements IResourceManager {

    protected String m_name = "";

    public IResourceManager flightResourceManager = null;
    public IResourceManager carResourceManager = null;
    public IResourceManager roomResourceManager = null;

    RMHashMap m_data = new RMHashMap();

    public Middleware(String p_name) {m_name = p_name;}

    // Reads a data item
	public RMItem readData(String key)
	{
		synchronized(m_data) {
			RMItem item = m_data.get(key);
			if (item != null) {
				return (RMItem)item.clone();
			}
			return null;
		}
	}

	// Writes a data item
	public void writeData(String key, RMItem value)
	{
		synchronized(m_data) {
			m_data.put(key, value);
		}
	}

	// Remove the item out of storage
	public void removeData(String key)
	{
		synchronized(m_data) {
			m_data.remove(key);
		}
	}
    
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightResourceManager.addFlight(flightNum, flightSeats, flightPrice);
    }

    public boolean addCars(String location, int numCars, int price) throws RemoteException {
        return carResourceManager.addCars(location, numCars, price);
    }

    public boolean addRooms(String location, int numRooms, int price) throws RemoteException {
        return roomResourceManager.addRooms(location, numRooms, price);
    }

    public int newCustomer() throws RemoteException
	{
        	Trace.info("RM::newCustomer() called");
		// Generate a globally unique ID for the new customer; if it generates duplicates for you, then adjust
		int cid = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
			String.valueOf(Math.round(Math.random() * 100 + 1)));
		Customer customer = new Customer(cid);
		writeData(customer.getKey(), customer);
		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
		return cid;
	}

	public boolean newCustomer(int customerID) throws RemoteException
	{
		Trace.info("RM::newCustomer(" + customerID + ") called");
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null)
		{
			customer = new Customer(customerID);
			writeData(customer.getKey(), customer);
			Trace.info("RM::newCustomer(" + customerID + ") created a new customer");
			return true;
		}
		else
		{
			Trace.info("INFO: RM::newCustomer(" + customerID + ") failed--customer already exists");
			return false;
		}
	}

    public boolean deleteFlight(int flightNum) throws RemoteException {
        return flightResourceManager.deleteFlight(flightNum);
    }

    public boolean deleteCars(String location) throws RemoteException {
        return carResourceManager.deleteCars(location);
    }

    public boolean deleteRooms(String location) throws RemoteException {
        return roomResourceManager.deleteRooms(location);
    }

    public boolean deleteCustomer(int customerID) throws RemoteException
	{
		Trace.info("RM::deleteCustomer(" + customerID + ") called");
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::deleteCustomer(" + customerID + ") failed--customer doesn't exist");
			return false;
		}
		else
		{            
			// Increase the reserved numbers of all reservable items which the customer reserved. 
 			RMHashMap reservations = customer.getReservations();
			for (String reservedKey : reservations.keySet())
			{        
				ReservedItem reserveditem = customer.getReservedItem(reservedKey);
				Trace.info("RM::deleteCustomer(" + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
				
                IResourceManager resource_manager = null;
                if (isFlight(reserveditem.getKey())) {resource_manager = flightResourceManager;}
                if (isCar(reserveditem.getKey())) {resource_manager = carResourceManager;}
                if (isRoom(reserveditem.getKey())) {resource_manager = roomResourceManager;}

                ReservableItem item  = (ReservableItem)resource_manager.readData(reserveditem.getKey());
				Trace.info("RM::deleteCustomer(" + customerID + ") has reserved " + reserveditem.getKey() + " which is reserved " +  item.getReserved() +  " times and is still available " + item.getCount() + " times");
				item.setReserved(item.getReserved() - reserveditem.getCount());
				item.setCount(item.getCount() + reserveditem.getCount());
				resource_manager.writeData(item.getKey(), item);
			}

			// Remove the customer from the storage
			removeData(customer.getKey());
			Trace.info("RM::deleteCustomer(" + customerID + ") succeeded");
			return true;
		}
	}

    protected static boolean isFlight(String key) {return key.charAt(0) == 'f';}

    protected static boolean isCar(String key) {return key.charAt(0) == 'c';}

    protected static boolean isRoom(String key) {return key.charAt(0) =='r';}

    public int queryFlight(int flightNumber) throws RemoteException {
        return flightResourceManager.queryFlight(flightNumber);
    }

    public int queryCars(String location) throws RemoteException {
        return carResourceManager.queryCars(location);
    }

    public int queryRooms(String location) throws RemoteException {
        return roomResourceManager.queryRooms(location);
    }

    public String queryCustomerInfo(int customerID) throws RemoteException {
        Trace.info("RM::queryCustomerInfo(" + customerID + ") called");
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::queryCustomerInfo(" + customerID + ") failed--customer doesn't exist");
			// NOTE: don't change this--WC counts on this value indicating a customer does not exist...
			return "";
		}
		else
		{
			Trace.info("RM::queryCustomerInfo(" + customerID + ")");
			System.out.println(customer.getBill());
			return customer.getBill();
		}
    }

    public int queryFlightPrice(int flightNumber) throws RemoteException {
        return flightResourceManager.queryFlightPrice(flightNumber);
    }

    public int queryCarsPrice(String location) throws RemoteException {
        return carResourceManager.queryCarsPrice(location);
    }

    public int queryRoomsPrice(String location) throws RemoteException {
        return roomResourceManager.queryRoomsPrice(location);
    }

    protected boolean reserveItem(int customerID, String key, String location) throws RemoteException {
        Trace.info("RM::reserveItem(customer=" + customerID + ", " + key + ", " + location + ") called" );        
		// Read customer object if it exists (and read lock it)
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
			return false;
		} 

		// Check if the item is available

        IResourceManager resource_manager = null;
                if (isFlight(key)) {resource_manager = flightResourceManager;}
                if (isCar(key)) {resource_manager = carResourceManager;}
                if (isRoom(key)) {resource_manager = roomResourceManager;}

		ReservableItem item = (ReservableItem) resource_manager.readData(key);
		if (item == null)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
			return false;
		}
		else if (item.getCount() == 0)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") failed--No more items");
			return false;
		}
		else
		{            
			customer.reserve(key, location, item.getPrice());        
			writeData(customer.getKey(), customer);

			// Decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			resource_manager.writeData(item.getKey(), item);

			Trace.info("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") succeeded");
			return true;
		}   
    }

    public boolean reserveFlight(int customerID, int flightNum) throws RemoteException {
        return reserveItem(customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
    }

    public boolean reserveCar(int customerID, String location) throws RemoteException {
        return reserveItem(customerID, Car.getKey(location), location);
    }

    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        return reserveItem(customerID, Room.getKey(location), location);
    }

    public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        for (String flightStr: flightNumbers) {
			int flightNum = Integer.valueOf(flightStr);
			reserveFlight(customerID, flightNum);
		}

		if (car) {reserveCar(customerID, location);}
		if (room) {reserveRoom(customerID, location);}

		return true;
    }

    public String getName() throws RemoteException {
        return m_name;
    }
}


package Server;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import Remote.ClientInterface;

public class ClientManager implements Iterable<ClientInterface> {

    private Set<ClientInterface> clientsList;

    public ClientManager(WhiteBoardServer canvasServer){
        this.clientsList = Collections.newSetFromMap(new ConcurrentHashMap<ClientInterface, Boolean>());
    }

    // add new client into white board
    public void addClient(ClientInterface client){
    	System.out.println("Adding client: " + client);
	    this.clientsList.add(client);
	    System.out.println("Current number of clients: " + this.clientsList.size());
    }

    // remove a client from the white board.
    public void deleteClient(ClientInterface client){
        this.clientsList.remove(client);
    }
    
    // Checks if the list of clients is empty
    public Boolean isEmpty(){
    	boolean users = this.clientsList.size() == 0;
	    return users;
    }

    // Returns the set of all clients
    public Set<ClientInterface> getClients(){
        return this.clientsList;
    }

    // an iterator over the set of clients
    @Override
    public Iterator<ClientInterface> iterator() {
        return clientsList.iterator();
    }

}

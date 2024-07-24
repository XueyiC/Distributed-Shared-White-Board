package Server;

import Remote.ClientInterface;
import Remote.MessageInterface;
import Remote.ServerInterface;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class WhiteBoardServer extends UnicastRemoteObject implements ServerInterface, Serializable {

    private ClientManager clientManager;

    protected WhiteBoardServer() throws RemoteException {
        this.clientManager = new ClientManager(this);
		System.out.println("ClientManager initialized: " + this.clientManager);
    }

    // client join in the server
    @Override
    public void joinIn(ClientInterface client) throws RemoteException {
		System.out.println("Attempting to join client: " + client + " | ClientManager: " + this.clientManager);
		if (client == null) {
	        System.err.println("Received a null client reference.");
	        return;
	    }
		if (this.clientManager == null) {
	        System.err.println("ClientManager is not initialized.");
	        return; 
	    }
		
		// the fist client is the manager
		if(this.clientManager.isEmpty()) {
            client.setManager();
		    client.setName("Manager: " + client.getName());
        }
//		
//		if (checkManagerAgreement(client)) {
//	        this.clientManager.addClient(client);
//	        for (ClientInterface c : this.clientManager) {
//	            c.updateClients(this.clientManager.getClients());
//	        }
//	    } else {
//	        client.setAgreement(false);
//	    }

        boolean permission = true;
        for(ClientInterface c : this.clientManager) {
            if(c.isManager()) {
                try {
                    permission = c.checkAgreement(client.getName());
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Set the client's permission.
        if(!permission) {
            try {
                client.setAgreement(permission);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        this.clientManager.addClient(client);
        for(ClientInterface c : this.clientManager) {
        	try {
                c.updateClients(this.clientManager.getClients());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to update client list for " + c.getName());
            }
        }

        System.out.println("Client " + client.getName() + " successfully joined.");
    }

    private boolean checkManagerAgreement(ClientInterface client) throws RemoteException {
	    for (ClientInterface c : this.clientManager) {
	        if (c.isManager()) {
	            return c.checkAgreement(client.getName());
	        }
	    }
	    return false;
	}
    
    // clear all drawings on the white board
    public void clearWhiteBoard() throws RemoteException {
        for (ClientInterface client : this.clientManager) {
            client.clearWhiteBoard();
        }
    }


    // the client quit the white board
    public void quit(String name) throws RemoteException {
//        for (ClientInterface c : this.clientManager) {
//            if (c.getName().equals(name)) {
//                this.clientManager.deleteClient(c);
//				System.out.println(name + "has quit this white board");
//            }
//        }
//        for (ClientInterface c : this.clientManager) {
//            c.updateClients(this.clientManager.getClients());
//        }
    	boolean clientRemoved = false;
        Iterator<ClientInterface> iterator = this.clientManager.iterator();
        
        while (iterator.hasNext()) {
            ClientInterface client = iterator.next();
            if (client.getName().equals(name)) {
                iterator.remove();
                System.out.println(name + " quit this white board");
                clientRemoved = true;
                break; // Assume client names are unique, so we can break after finding the client
            }
        }
        
        if (clientRemoved) {
            JOptionPane.showMessageDialog(null, name + " has quit the whiteboard.");
            for (ClientInterface client : this.clientManager) {
                client.updateClients(this.clientManager.getClients());
            }
        } else {
            System.err.println("Client with name " + name + " not found.");
        }
    }

    
    @Override
    // the white board manager kicks out the client
    public void removeClient(String name) throws RemoteException {
//        for (ClientInterface c : this.clientManager) {
//            if (c.getName().equals(name)) {
//                this.clientManager.deleteClient(c);
//				System.out.println(name + " is kicked out by the manager");
//	            JOptionPane.showMessageDialog(null, name + " is kicked out by the manager.");
//                c.closeWhiteBoard();
//            }
//        }
//        for (ClientInterface c : this.clientManager) {
//            c.updateClients(this.clientManager.getClients());
//        }
    	boolean clientRemoved = false;
        Iterator<ClientInterface> iterator = this.clientManager.iterator();
        
        while (iterator.hasNext()) {
            ClientInterface client = iterator.next();
            if (client.getName().equals(name)) {
                iterator.remove();
                System.out.println(name + " is kicked out by the manager");
                JOptionPane.showMessageDialog(null, name + " is kicked out by the manager.");
                try {
                    client.closeWhiteBoard();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to close whiteboard for " + name, "Error", JOptionPane.ERROR_MESSAGE);
                }
                clientRemoved = true;
                break; // Assume client names are unique, so we can break after finding the client
            }
        }
        
        if (clientRemoved) {
            for (ClientInterface client : this.clientManager) {
                try {
                    client.updateClients(this.clientManager.getClients());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update client list for " + client.getName(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Client with name " + name + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Client with name " + name + " not found.");
        }
    }


    @Override
	// the white board's manager close the board and remove all clients in the board
    public void managerQuit() throws IOException, RemoteException {
//		System.out.println("The manager closed the white board");
//        for(ClientInterface c : this.clientManager) {
//            this.clientManager.deleteClient(c);
//            c.closeWhiteBoard();
//        }
    	System.out.println("The manager closed the white board");

        Iterator<ClientInterface> iterator = this.clientManager.iterator();
        while (iterator.hasNext()) {
            ClientInterface client = iterator.next();
            try {
                client.closeWhiteBoard(); 
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to close windows for " + client.getName(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            iterator.remove(); 
        }

        JOptionPane.showMessageDialog(null, "The whiteboard has been closed by the manager.");
    	
    }


    @Override
    // client can send text message through chat
    public void chat(String text) throws RemoteException {
        for (ClientInterface client : this.clientManager) {
            try {
                client.chat(text);
            } catch (Exception e) {
				System.out.println("The server is not responding");
	            JOptionPane.showMessageDialog(null, "White board server is not responding", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    @Override
	// when a new client join in, the manager sends the white board
    public byte[] sendWhiteBoard() throws IOException, RemoteException {
        byte[] image = null;
        for (ClientInterface client : this.clientManager) {
            if (client.isManager()) {
            	try {
                    image = client.sendImage();
                } catch (Exception e) {
                    System.err.println("Failed to send whiteboard image from manager: " + client.getName());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to send whiteboard image from manager.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return image;
    }

	// the manager can open an existing white board image and share it to others
    public void shareImage(byte[] image) throws IOException, RemoteException {
        for (ClientInterface client : this.clientManager) {
            if (!client.isManager()) {
            	try {
                    client.drawImage(image);
                } catch (Exception e) {
                    System.err.println("Failed to share image with client: " + client.getName());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to share image with client: " + client.getName(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    @Override
    public Set<ClientInterface> getClients() throws RemoteException {
        return this.clientManager.getClients();
    }

    
    @Override
	// broadcast the updates to all clients
    public void broadcast(MessageInterface message) throws RemoteException {
        for (ClientInterface client : this.clientManager) {
        	try {
                client.updateWhiteBoard(message);
            } catch (Exception e) {
                System.err.println("Failed to broadcast message to client: " + client.getName());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to broadcast message to client: " + client.getName(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

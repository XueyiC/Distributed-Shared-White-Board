package Remote;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ServerInterface extends Remote {

	// Client join in the white board server
    public void joinIn(ClientInterface client) throws RemoteException;

	// client sends to server and server broadcast to other clients
    public void broadcast(MessageInterface message) throws RemoteException;
    
    // manager clear the drawings to refresh the white board
    public void clearWhiteBoard() throws RemoteException;

    // client closes his windows and quite the program
    public void quit(String name) throws RemoteException;

    // the white board manager kicks out the client
    public void removeClient(String name) throws RemoteException;

    // the white board's manager close the board and remove all clients in the board
    public void managerQuit() throws IOException,RemoteException;

    // client can send text through chat
    public void chat(String text) throws RemoteException;

	// when a new client join in, the manager sends the white board
    public byte[] sendWhiteBoard() throws IOException,RemoteException;
    
	// the manager can open an local image and share it to others
    public void shareImage(byte[] rawImage)throws IOException,RemoteException;

     // get the clients list
    public Set<ClientInterface> getClients() throws RemoteException;
}

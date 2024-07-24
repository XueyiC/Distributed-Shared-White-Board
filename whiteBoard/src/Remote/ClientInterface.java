package Remote;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ClientInterface extends Remote {
	
	// set the first joined client be the manager of the white board 
    public void setManager() throws RemoteException;

    // check if the client is the manager
    public boolean isManager() throws RemoteException;
    
	// get and set the client's name
    public String getName() throws RemoteException;
    public void setName(String string) throws RemoteException;

	// the manager allows the client to join in
    public boolean checkAgreement(String name) throws RemoteException;

    // change client's status to can join in or cannot join in
    public void setAgreement(Boolean permission) throws RemoteException;
    
    boolean getAgreement() throws RemoteException;

    // update joined client's list
    public void updateClients(Set<ClientInterface> clientsList) throws RemoteException;

    public void updateWhiteBoard(MessageInterface message) throws RemoteException;

    public void clearWhiteBoard() throws RemoteException;

    public void closeWhiteBoard() throws RemoteException;

    public void chat(String text) throws RemoteException;

    public byte[] sendImage() throws IOException;

    public void drawImage(byte[] image) throws IOException;

    public void launch(ServerInterface server) throws RemoteException;
}

package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.JOptionPane;
import Remote.ServerInterface;

public class Server {
    public static void main(String[] args) {
    	if (args.length < 1) {
            System.out.println("Usage: java Server <serverPort>");
            return;
        }
        
        try {
            int port = Integer.parseInt(args[0]);
            ServerInterface server = new WhiteBoardServer();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("WhiteBoardServer", server);
            JOptionPane.showMessageDialog(null, "Whiteboard server is running on port " + port + "!");
        } catch (Exception e) {
            System.out.println("Port Number is wrong or already in use.");  
            e.printStackTrace();
        }
    }
}


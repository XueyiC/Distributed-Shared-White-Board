package Remote;

import java.awt.Color;
import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;

import Client.WhiteBoard.BrushType;

public interface MessageInterface extends Remote {

    // Getters for drawing state.
    public String getState() throws RemoteException;

    // Getters for client name.
    public String getName() throws RemoteException;

    // Getters for drawing brush.
    @SuppressWarnings("exports")
	public BrushType getBrush() throws RemoteException;

    // Getters for drawing color.
    @SuppressWarnings("exports")
	public Color getColor() throws RemoteException;

    // Getters for drawing point.
    @SuppressWarnings("exports")
	public Point getPoint() throws RemoteException;

    // Getters for sending text.
    public String getText() throws RemoteException;

}

package Client;

import java.awt.Color;
import java.awt.Point;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import Client.WhiteBoard.BrushType;
import Remote.MessageInterface;

// The Message class is used for wrapping whiteBoard message details.
@SuppressWarnings("serial")
public class Message extends UnicastRemoteObject implements MessageInterface {

    private String state;
    private String name;
    private BrushType brush;
    private Color color;
    private Point point;
    private String text;

    // Constructor for Message to initialize canvas message details.
    protected Message(String state, String name, BrushType brush, Color color, Point point, String text) throws RemoteException {
        this.state = state;
        this.name = name;
        this.brush = brush;
        this.color = color;
        this.point = point;
        this.text = text;
    }

    // Get the drawing state.
    @Override
    public String getState() throws RemoteException {
        return state;
    }


    // Get the client name.
    @Override
    public String getName() throws RemoteException {
        return name;
    }


    // Get the brush.
    @Override
    public BrushType getBrush() throws RemoteException {
        return brush;
    }
    
    public void setBrush(BrushType brush) throws RemoteException {
        this.brush = brush;
    }

    
    // Get the text.
    @Override
    public String getText() throws RemoteException {
        return text;
    }
    

    // Get the color.
    @Override
    public Color getColor() throws RemoteException {
        return color;
    }


    // Get the point.
    @Override
    public Point getPoint() throws RemoteException {
        return point;
    }
}

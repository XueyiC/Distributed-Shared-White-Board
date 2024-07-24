package Client;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import Remote.ServerInterface;

public class WhiteBoard extends JComponent {
    private static final long serialVersionUID = 1L;
    private ServerInterface server;
    private String clientName;
    private boolean isManager;
    
    private Point startPoint, endPoint;
    ///private String brush;
    private BrushType brush;
    public enum BrushType {
    	PEN, LINE, RECT, CIRCLE, OVAL, TEXT, ERASER
    }
    private Color color;
    private String text;

    private BufferedImage image;
    private BufferedImage previousWhiteBoard;
    private Graphics2D graphics;
    
    public static final Color[] COLORS = {
            Color.black, Color.blue, Color.red, Color.green, Color.yellow,
            Color.pink, Color.orange, Color.gray, Color.cyan, Color.magenta,
            Color.darkGray, Color.lightGray, new Color(128, 0, 128), // Purple
            new Color(165, 42, 42), // Brown
            new Color(0, 255, 0), // Lime
            new Color(0, 128, 128) // Teal
    };
    
    public WhiteBoard(String name, boolean isManager, ServerInterface RemoteInterface) {
        this.server = RemoteInterface;
        this.clientName = name;
        this.isManager = isManager;

        this.brush = BrushType.PEN;
        this.color = Color.black;
        this.text = "";
        setDoubleBuffered(false);
        
	    // When listens to a mouse click, store the start location and send it to the server
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                saveWhiteBoard();
                try {
                    Message message = new Message("start", clientName, brush, color, startPoint, text);
                    server.broadcast(message);
                } catch (RemoteException ex) {
	                JOptionPane.showMessageDialog(null, "WhiteBoard server shuts down.");
                }
            }
        });

   	 // Listen to the action on the white board, draw the shape on local client, then send the shape to server
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                Shape shape = null;
                if (graphics != null) {
                	graphics.setPaint(color);
                    graphics.setStroke(new BasicStroke(1.0f));
                    if (brush == BrushType.PEN) {
                        shape = makeLine(startPoint, endPoint);
                        graphics.setPaint(color);
                        graphics.setStroke(new BasicStroke(1.0f));
                        startPoint = endPoint;
                        try {
                            server.broadcast(new Message("drawing", clientName, brush, color, endPoint, ""));
                        } catch (RemoteException ex) {
                            JOptionPane.showMessageDialog(null, "Whit Board server is not responding.");
                        }
                    } else if (brush == BrushType.ERASER) {
                        shape = makeLine(startPoint, endPoint);
                        startPoint = endPoint;
                        graphics.setPaint(Color.white);
                        graphics.setStroke(new BasicStroke(20.0f));
                        try {
                            server.broadcast(new Message("drawing", clientName, brush, Color.white, endPoint, ""));
                        } catch (RemoteException ex) {
                            JOptionPane.showMessageDialog(null, "White Board server is not responding.");
                        }
                    } else {
                        drawPreviousCanvas();
                        if(brush == BrushType.LINE) {
                            shape = makeLine(startPoint, endPoint);
                        } else if(brush == BrushType.RECT) {
                            shape = makeRect(startPoint, endPoint);
                        } else if(brush == BrushType.CIRCLE) {
                            shape = makeCircle(startPoint, endPoint);
                        } else if(brush == BrushType.OVAL) {
                            shape = makeOval(startPoint, endPoint);
                        } else if(brush == BrushType.TEXT) {
                            graphics.setFont(new Font("TimesRoman", Font.PLAIN, 20));
                            graphics.drawString("Enter text here", endPoint.x, endPoint.y);          
                            shape = makeText(startPoint);
                            graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1));
                        }
                    }
                    graphics.draw(shape);
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint(); 
                Shape shape = null;
                if (graphics != null) {
                    if (brush == BrushType.LINE) {
                        shape = makeLine(startPoint, endPoint);
                    } else if (brush == BrushType.PEN) {
                        shape = makeLine(startPoint, endPoint);
                    } else if (brush == BrushType.RECT) {
                        shape = makeRect(startPoint, endPoint);
                    } else if (brush == BrushType.CIRCLE) {
                        shape = makeCircle(startPoint, endPoint);
                    } else if (brush == BrushType.OVAL) {
                        shape = makeOval(startPoint, endPoint);
                    } else if (brush == BrushType.TEXT) {
                        String inputText = JOptionPane.showInputDialog(null, "Enter text: ");
                        if (inputText != null && !inputText.isEmpty()) {
                            drawPreviousCanvas();
                            graphics.setFont(new Font("TimesRoman", Font.PLAIN, 20));
                            graphics.drawString(inputText, endPoint.x, endPoint.y);
                            graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1));
                            shape = makeText(startPoint); 
                            text = inputText;
                        }
                    }
                    if (shape != null) {
                        graphics.draw(shape);
                    }
                    repaint(); 
                    try {
                        Message message = new Message("end", clientName, brush, color, endPoint, text);
                        server.broadcast(message);
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(null, "White Board server is not responding.");
                    }
                }
            }
        });
    }
    
    public Shape makeText(Point start) {
        int x = start.x - 5; 
        int y = start.y - 20; 
        int width = 130;     
        int height = 25;   
        return new RoundRectangle2D.Double(x, y, width, height, 15, 15);
    }

    public Shape makeLine(Point start, Point end) {
        return new Line2D.Double(start.x, start.y, end.x, end.y);
    }

    
    public Shape makeRect(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        return new Rectangle2D.Double(x, y, width, height);
    }
    
    
    public Shape makeCircle(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int diameter = Math.max(Math.abs(start.x - end.x), Math.abs(start.y - end.y));
        return new Ellipse2D.Double(x, y, diameter, diameter);
    }

    public Shape makeOval(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        return new Ellipse2D.Double(x, y, width, height);
    }

    @Override
	// The method for painting the shape on the white board.
	// Initialize the white board to synchronize with the manager's image when the client joins the shared white board.
    protected void paintComponent(Graphics g) {
        if (image == null) {
            if (isManager) {
            	image = new BufferedImage(900, 500, BufferedImage.TYPE_INT_RGB);
                graphics = (Graphics2D) image.getGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
                reset(); 
            } else {
                try {
                    byte[] rawImage = server.sendWhiteBoard();  
                    image = ImageIO.read(new ByteArrayInputStream(rawImage));
                    graphics = (Graphics2D) image.getGraphics();
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
                    graphics.setPaint(color);
                } catch (IOException e) {
                    System.err.println("Fail receiving image!"); 
                }
            }
        }
        g.drawImage(image, 0, 0, null);
    }

    public Color getCurrColor() {
        return color;
    }

    public String getCurrMode() {
        return brush.name().toLowerCase(); 
    }

    public Graphics2D getGraphic() {
        return graphics;  
    }

    public BufferedImage getWhiteBoard() {
        saveWhiteBoard();  
        return previousWhiteBoard; 
    }

    public void reset() {
        graphics.setPaint(Color.white);
        graphics.fillRect(0, 0, 900, 500);  
        graphics.setPaint(color); 
        repaint(); 
    }

    public void saveWhiteBoard() {
        ColorModel cm = image.getColorModel(); 
        WritableRaster raster = image.copyData(null); 
        previousWhiteBoard = new BufferedImage(cm, raster, false, null); 
    }
    public void drawPreviousCanvas() {
        drawImage(previousWhiteBoard);  
    }

    public void drawImage(BufferedImage img) {
        graphics.drawImage(img, 0, 0, null);
        repaint(); 
    }

    public void setColor(Color newColor) {
    	this.color = newColor;
    	graphics.setPaint(color);
    }
    
    public void setBrush(BrushType newBrush) {
        this.brush = newBrush;
    }
}

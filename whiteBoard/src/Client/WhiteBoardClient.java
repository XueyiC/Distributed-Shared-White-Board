package Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import Remote.ClientInterface;
import Remote.MessageInterface;
import Remote.ServerInterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

import Client.WhiteBoard.BrushType;

@SuppressWarnings("serial")
public class WhiteBoardClient extends UnicastRemoteObject implements ClientInterface {
    static ServerInterface server;
    private boolean isManager; // true if is manager
    private boolean agree; // permission granted by manager
    private JFrame frame;
    private DefaultListModel<String> clients;
    private DefaultListModel<String> chats;
    private JButton newButton, openButton, saveButton, saveAsButton, closeButton;
    private JButton[] colorButtons;
    private JButton[] toolButtons;
    private JScrollPane msgArea;
    private JTextArea tellColor, displayColor;
    private JList<String> chat;
    private ArrayList<JButton> buttonList;
    private WhiteBoard whiteBoard;
    private String clientName;
    private String imageName; 
    private String imagePath; 
    private Hashtable<String, Point> startPoints = new Hashtable<>();

    
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
    	if (args.length < 3) {
            System.out.println("Usage: java Client <serverIPAddress> <serverPort> <username>");
            return;
        }

        String hostName = args[0];
        String portNumber = args[1];
        String clientName = args[2];
        String serverAddress = "//" + hostName + ":" + portNumber + "/WhiteBoardServer";
        ServerInterface server = (ServerInterface) Naming.lookup(serverAddress);
        ClientInterface client = new WhiteBoardClient(server);
        
        // Validate the username  
        boolean validName = false;
        do {
		    if (clientName == null || clientName.isEmpty()) {
		        JOptionPane.showMessageDialog(null, "You didn't enter a username. Please try again.");
		        validName = false;
		        continue;
		    } else {
		        validName = true;
		        for (ClientInterface c : server.getClients()) {
		            if (clientName.equals(c.getName()) || c.getName().equals("Manager: " + clientName)) {
		                JOptionPane.showMessageDialog(null, "This username is already taken. Please choose another.");
		                validName = false;
		                break;
		            }
		        }
		    }
		} while (!validName);

		client.setName(clientName);
		try {
			if(client.getAgreement()) {
				server.joinIn(client);
				client.launch(server);
			} else {
				JOptionPane.showMessageDialog(null, "The manager did not allow you to join in.");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Faild to register the client.");
			e.printStackTrace();
		}
    }

    @SuppressWarnings("static-access")
	protected WhiteBoardClient(ServerInterface server) throws RemoteException {
        this.server = server;
        clients = new DefaultListModel<>();
        isManager = false; 
        agree = true;  
        chats = new DefaultListModel<>(); 
        buttonList = new ArrayList<>();
    }
    
    
    ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	Object source = e.getSource();
			if (source == newButton) {
				handleClear();
            } else if (source == openButton) {
                try {
                    handleOpen();
                } catch (IOException ex) {
                    System.err.println("There is an IO error.");
                    JOptionPane.showMessageDialog(null, "Failed to open the iamge.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (source == saveButton) {
                try {
                    handleSave();
                } catch (IOException ex) {
                    System.err.println("There is an IO error.");
                    JOptionPane.showMessageDialog(null, "Failed to save the whiteboard.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (source == saveAsButton) {
                try {
                    handleSaveAs();
                } catch (IOException ex) {
                    System.err.println("There is an IO error.");
                    JOptionPane.showMessageDialog(null, "Failed to save as the whiteboard.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (source == closeButton) {
            	if (JOptionPane.showConfirmDialog(frame,
                        "You are the manager, Are you sure to quit?", "Close white board?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    try {
                        server.managerQuit(); 
                    } catch (IOException e1) {
                        System.err.println("There is an IO error");
                        JOptionPane.showMessageDialog(null, "Failed to close the whiteboard.", "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        System.exit(0); 
                    } 
            	}
            }
			else if (isColorButton(source)) {
                handleColorChange(source);
            } else if (isToolButton(source)) {
                handleToolChange(source);
            }
        }
    };
    
    
    private void handleClear() {
        if (isManager) {
            int dialogResult = JOptionPane.showConfirmDialog(frame, "Are you sure to open a new white board?", "Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                try {
                    whiteBoard.reset();
                    server.clearWhiteBoard();
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(null, "WhiteBoard server is not responding, please save and exit.");
                }
            }
        }
    }

    private void handleOpen() throws IOException {
        FileDialog openFileDialog = new FileDialog(frame, "Open an white board image", FileDialog.LOAD);
        openFileDialog.setVisible(true);  
        if (openFileDialog.getFile() != null) {  
            this.imagePath = openFileDialog.getDirectory(); 
            this.imageName = openFileDialog.getFile();  

            BufferedImage image = ImageIO.read(new File(imagePath + imageName));
            whiteBoard.drawImage(image);  

            ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
            ImageIO.write(image, "png", imageArray);
            server.shareImage(imageArray.toByteArray());
        }
    }

    private void handleSaveAs() throws IOException {
        FileDialog saveAsDialog = new FileDialog(frame, "Save white board as image", FileDialog.SAVE);
        saveAsDialog.setVisible(true); 
        if (saveAsDialog.getFile() != null) { 
            this.imagePath = saveAsDialog.getDirectory();  
            this.imageName = saveAsDialog.getFile();  
            ImageIO.write(whiteBoard.getWhiteBoard(), "png", new File(imagePath + imageName));
        }
    }

    private void handleSave() throws IOException {
        if (imageName == null) {
            JOptionPane.showMessageDialog(null, "Please use saveAs to define a save path first.");
        } else {
            ImageIO.write(whiteBoard.getWhiteBoard(), "png", new File(imagePath + imageName));
        }
    }
    
    private boolean isColorButton(Object source) {
        for (JButton button : colorButtons) {
            if (source == button) {
                return true;
            }
        }
        return false;
    }

    private void handleColorChange(Object source) {
        for (int i = 0; i < colorButtons.length; i++) {
            if (source == colorButtons[i]) {
                whiteBoard.setColor(WhiteBoard.COLORS[i]);
                displayColor.setBackground(whiteBoard.getCurrColor());
                break;
            }
        }
    }

    private boolean isToolButton(Object source) {
        for (JButton button : toolButtons) {
            if (source == button) {
                return true;
            }
        }
        return false;
    }

    private void handleToolChange(Object source) {
        for (int i = 0; i < toolButtons.length; i++) {
            if (source == toolButtons[i]) {
                WhiteBoard.BrushType brushType = WhiteBoard.BrushType.values()[i];
                whiteBoard.setBrush(brushType);
                updateButtonBorders(toolButtons[i]);
                break;
            }
        }
    }

    @Override
    public void setManager() throws RemoteException {
        this.isManager = true;
    }

    @Override
    public boolean isManager() throws RemoteException {
        return this.isManager;
    }

    @Override
    public String getName() throws RemoteException {
        return this.clientName;
    }

    @Override
    public void setName(String string) throws RemoteException {
        this.clientName = string;
        return;
    }

    public void clearWhiteBoard() {
        this.whiteBoard.reset();

    }

    public byte[] sendImage() throws IOException {
        ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
        ImageIO.write(this.whiteBoard.getWhiteBoard(), "png", imageArray);
        return imageArray.toByteArray();
    }

    public void drawImage(byte[] image) throws IOException {
        BufferedImage newImage = ImageIO.read(new ByteArrayInputStream(image));
        this.whiteBoard.drawImage(newImage);
    }

    public boolean checkAgreement(String name) {
    	int response = JOptionPane.showConfirmDialog(
    	        frame,
    	        name + " wants to join in your whiteboard. Do you agree?",
    	        "Grant Agreement",
    	        JOptionPane.YES_NO_OPTION,
    	        JOptionPane.QUESTION_MESSAGE
    	    );

    	return response == JOptionPane.YES_OPTION;
    }

    @Override
    public void setAgreement(Boolean agreement) throws RemoteException {
        this.agree = agreement;

    }

    @Override
    public void updateClients(Set<ClientInterface> list) {
        this.clients.removeAllElements();
        for (ClientInterface c : list) {
            try {
                clients.addElement(c.getName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    
    private Shape makeLine(Point start, Point end) {
        return new Line2D.Double(start.x, start.y, end.x, end.y);
    }

    private Shape makeRect(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        return new Rectangle2D.Double(x, y, width, height);
    }

    private Shape makeCircle(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int diameter = Math.max(Math.abs(start.x - end.x), Math.abs(start.y - end.y));
        return new Ellipse2D.Double(x, y, diameter, diameter);
    }

    private Shape makeOval(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        return new Ellipse2D.Double(x, y, width, height);
    }

    private Shape makeText(Point start) {
        int x = start.x - 5;
        int y = start.y - 20;
        int width = 130;
        int height = 25;
        return new RoundRectangle2D.Double(x, y, width, height, 15, 15);
    }

    @Override
    public void updateWhiteBoard(MessageInterface msg) throws RemoteException {
        if (msg.getName().compareTo(clientName) == 0) {
            return;
        }
        
        Shape shape = null;
        Graphics2D graphics = whiteBoard.getGraphic();
        
        if (graphics == null) {
            System.err.println("Graphics context is not available.");
            return;
        }
        
        // Process the start of a drawing action
        if (msg.getState().equals("start")) {
            startPoints.put(msg.getName(), msg.getPoint());
            return;
        }
        // Handle the drawing action
        Point startPt = startPoints.get(msg.getName());
        if (startPt == null) {
            System.err.println("Start point for client " + msg.getName() + " is null.");
            return;
        }
        graphics.setPaint(msg.getColor());
        
        if (msg.getState().equals("drawing")) {
            if (msg.getBrush() == BrushType.ERASER) {
            	graphics.setStroke(new BasicStroke(15.0f));
            } else {
            	graphics.setStroke(new BasicStroke(1.0f));
            }
            shape = makeLine(startPt, msg.getPoint());
            startPoints.put(msg.getName(), msg.getPoint());
            graphics.draw(shape);
            whiteBoard.repaint();
            return;
        }
        
        // Finalize and refresh the canvas        
        if (msg.getState().equals("end")) {
            graphics.setStroke(new BasicStroke(1.0f));
        	switch (msg.getBrush()) {
            case PEN:
            case LINE:
                shape = makeLine(startPt, msg.getPoint());
                break;
            case ERASER:
                graphics.setStroke(new BasicStroke(15.0f));
                shape = makeLine(startPt, msg.getPoint());
                break;
            case RECT:
                shape = makeRect(startPt, msg.getPoint());
                break;
            case CIRCLE:
                shape = makeCircle(startPt, msg.getPoint());
                break;
            case OVAL:
                shape = makeOval(startPt, msg.getPoint());
                break;
            case TEXT:
                graphics.setFont(new Font("TimesRoman", Font.PLAIN, 20));
                graphics.drawString(msg.getText(), msg.getPoint().x, msg.getPoint().y);
                shape = makeText(startPt);
                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1);
                graphics.setStroke(dashed);
                break;
            default:
                System.err.println("Unknown brush type: " + msg.getBrush());
                break;
        }

        if (shape != null) {
            try {
                graphics.draw(shape);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Shape is null, not drawing.");
        }

        whiteBoard.repaint();
        // Clean up after drawing is complete
        startPoints.remove(msg.getName());
        }
    }


    @Override
    public void closeWhiteBoard() throws RemoteException {
        // If manager does not agree to join in
        if (!this.agree) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(null, "Sorry, the shared whiteBoard's manager does not allow you to join in.", "Warning", JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                }
            });
            t.start();
            return;
        }

        // If kicked out or manager quit
        Thread t = new Thread(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, "The manager has quit.\n or you have been removed.\n" + "Your application will be closed.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
        t.start();
    }
    
    private void updateButtonBorders(JButton selectedButton) {
        for (JButton button : toolButtons) {
            if (button == selectedButton) {
                button.setBorder(new LineBorder(Color.black, 2));
            } else {
                button.setBorder(new LineBorder(new Color(238, 238, 238), 2));
            }
        }
    }

    @Override
    public boolean getAgreement() throws RemoteException{
        return this.agree;
    }

    @Override
    public void chat(String text) throws RemoteException {
        this.chats.addElement(text);
    }



    @Override
    public void launch(ServerInterface server) throws RemoteException {
        frame = new JFrame(clientName + " 's WhiteBoard");
        Container content = frame.getContentPane();
        whiteBoard = new WhiteBoard(clientName, isManager, server);

        colorButtons = new JButton[WhiteBoard.COLORS.length];
        for (int i = 0; i < WhiteBoard.COLORS.length; i++) {            
            JButton button = new JButton();
            button.setBackground(WhiteBoard.COLORS[i]);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.addActionListener(actionListener);
            colorButtons[i] = button;
            buttonList.add(button);
        }

        // Create tool buttons
        String[] iconPaths = {
                "/icon/office-material.png", "/icon/diagonal-line.png", "/icon/rectangle.png",
                "/icon/dry-clean.png", "/icon/ellipse.png", "/icon/text.png", "/icon/eraser.png"
        };
        String[] toolTips = {
                "Pen draw", "Line draw", "Rectangle draw", "Circle draw",
                "Oval draw", "Text input", "Eraser"
        };

        toolButtons = new JButton[iconPaths.length];
        for (int i = 0; i < iconPaths.length; i++) {
            LineBorder border = new LineBorder(new Color(238, 238, 238), 2);
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPaths[i]));
            Image image = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            ImageIcon newIcon = new ImageIcon(image);

            JButton button = new JButton(newIcon);
            button.setToolTipText(toolTips[i]);
            button.setBorder(border);
            button.addActionListener(actionListener);
            toolButtons[i] = button;
        }

        newButton = new JButton("New Board");
        newButton.setToolTipText("Create a new board");
        newButton.addActionListener(actionListener);
        
        openButton = new JButton("Open Image");
        openButton.setToolTipText("Open an image file");
        openButton.addActionListener(actionListener);

        saveButton = new JButton("Save Image");
        saveButton.setToolTipText("Save as image file");
        saveButton.addActionListener(actionListener);

        saveAsButton = new JButton("Save as");
        saveAsButton.setToolTipText("Save image file");
        saveAsButton.addActionListener(actionListener);
        
        closeButton = new JButton("Close");
        closeButton.setToolTipText("Close the white board");
        closeButton.addActionListener(actionListener);


        tellColor = new JTextArea("The current color is:");
        tellColor.setBackground(new Color(238,238,238));

        displayColor = new JTextArea("");
        displayColor.setBackground(Color.black);
        

        // if the client is not the manager, no file buttons
	    if (!isManager) {
	        newButton.setVisible(false);
	        openButton.setVisible(false);
	        saveButton.setVisible(false);
	        saveAsButton.setVisible(false);
	        closeButton.setVisible(false);
	    }

        JList<String> list = new JList<>(clients);
        JScrollPane currUsers = new JScrollPane(list);
        Dimension size = currUsers.getViewport().getView().getPreferredSize();
        currUsers.setPreferredSize(new Dimension(size.width + 20, Math.min(size.height + 20, 150)));

        if (isManager) {
            list.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    JList<String> list = (JList<String>)evt.getSource();
                    if (evt.getClickCount() == 2) {
                        int index = list.locationToIndex(evt.getPoint());
                        String selectedName = list.getModel().getElementAt(index);
                        try {
                            if (!getName().equals(selectedName)) {
                                int dialogResult = JOptionPane.showConfirmDialog(frame, "Are you sure to remove " + selectedName + "?", "Warning", JOptionPane.YES_NO_OPTION);
                                if (dialogResult == JOptionPane.YES_OPTION) {
                                    server.removeClient(selectedName);
                                    updateClients(server.getClients());
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("There is an IO error.");
                        }
                    }
                }
            });
        }

        chat = new JList<>(chats);
        msgArea = new JScrollPane(chat);
        msgArea.setMinimumSize(new Dimension(100, 100));
        JTextField msgText = new JTextField();
        msgText.setMinimumSize(new Dimension(100, 100));
        JButton sendBtn = new JButton("Send"); 
        sendBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (!msgText.getText().equals("")) { 
                    try {
                        server.chat(clientName + ": " + msgText.getText()); 
                        SwingUtilities.invokeLater(() -> {
                            JScrollBar vertical = msgArea.getVerticalScrollBar();
                            vertical.setValue(vertical.getMaximum());
                        });
                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(null, "WhiteBoard server is down, please try again later.");
                    }
                    msgText.setText("");
                }
            }
        });        
        
        // Set up the layout
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        whiteBoard.setPreferredSize(new Dimension(900, 500));

        // Horizontal group layout
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        
        // Adding tool buttons
        GroupLayout.ParallelGroup toolGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        for (JButton button : toolButtons) {
            toolGroup.addComponent(button);
        }
        hGroup.addGroup(toolGroup);
        hGroup.addComponent(whiteBoard, 500, 500, Short.MAX_VALUE);

        // Adding user and message area
        GroupLayout.ParallelGroup msgGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        msgGroup.addComponent(currUsers);
        msgGroup.addComponent(msgArea);
        msgGroup.addGroup(layout.createSequentialGroup()
                .addComponent(msgText)
                .addComponent(sendBtn)
        );
        hGroup.addGroup(msgGroup);

        // Adding color buttons and other controls
        GroupLayout.ParallelGroup colorGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        colorGroup.addComponent(newButton);
        colorGroup.addComponent(openButton);
        colorGroup.addComponent(saveButton);
        colorGroup.addComponent(saveAsButton);
        colorGroup.addComponent(closeButton);
        colorGroup.addComponent(tellColor);
        colorGroup.addComponent(displayColor);
        for (JButton button : colorButtons) {
            colorGroup.addComponent(button);
        }
        hGroup.addGroup(colorGroup);

        layout.setHorizontalGroup(hGroup);

        // Vertical group layout
        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        
        // Adding tool buttons vertically
        GroupLayout.SequentialGroup toolVGroup = layout.createSequentialGroup();
        for (JButton button : toolButtons) {
            toolVGroup.addComponent(button);
        }
        vGroup.addGroup(toolVGroup);

        vGroup.addComponent(whiteBoard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);

        // Adding user and message area
        GroupLayout.SequentialGroup msgVGroup = layout.createSequentialGroup();
        msgVGroup.addComponent(currUsers);
        msgVGroup.addComponent(msgArea);
        msgVGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(msgText)
                .addComponent(sendBtn)
        );
        vGroup.addGroup(msgVGroup);

        // Adding color buttons and other controls
        GroupLayout.SequentialGroup colorVGroup = layout.createSequentialGroup();
        colorVGroup.addComponent(newButton);
        colorVGroup.addComponent(openButton);
        colorVGroup.addComponent(saveButton);
        colorVGroup.addComponent(saveAsButton);
        colorVGroup.addComponent(closeButton);
        colorVGroup.addComponent(tellColor);
        colorVGroup.addComponent(displayColor);
        for (JButton button : colorButtons) {
            colorVGroup.addComponent(button);
        }
        vGroup.addGroup(colorVGroup);

        layout.setVerticalGroup(vGroup);
        
        frame.pack();
        frame.setVisible(true);
        layout.linkSize(SwingConstants.HORIZONTAL, newButton, openButton, saveButton, saveAsButton, closeButton);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	// if this client is manager
                if (isManager) { 
                    if (JOptionPane.showConfirmDialog(frame,
                            "You are the manager, Are you sure to quit?", "Close white board?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        try {
                            server.managerQuit(); 
                        } catch (IOException e) {
                            System.err.println("There is an IO error");
                        } finally {
                            System.exit(0); 
                        }
                    }
                // if this client is not manager
                } else { 
                    if (JOptionPane.showConfirmDialog(frame,
                            "Are you sure you want to quit?", "Close white board?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        try {
                            server.quit(clientName);
                            updateClients(server.getClients());
                        } catch (RemoteException e) {
                            JOptionPane.showMessageDialog(null, "WhiteBoard server is not responding.");
                        } finally {
                            System.exit(0);
                        }
                    }
                }
            }
        });
    }
}

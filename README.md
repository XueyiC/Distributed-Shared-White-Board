# Distributed-Shared-White-Board
Shared whiteboards allow multiple users to draw simultaneously on a canvas 

This project involves developing a distributed shared whiteboard system using Java RMI. The system allows multiple users to connect to a shared whiteboard where they can draw, type text, and interact at the same time. This report analyses the system architecture, design choices, implementation details, and innovations design in the project.

## System Architecture
The shared whiteboard system is composed of three main components:

**Server**: Manages client connections and broadcasts updates to all connected clients.

**Client**: Provides a user interface for drawing on the whiteboard and communicates with the server to synchronize updates.

**Remote Interfaces**: Defines the methods for remote communication between the server and clients using Java RMI.

**Communication Protocol**: The system uses Java RMI for communication. The server acts as a central authority managing the state of the whiteboard and client connections. Clients send drawing updates to the server, which then broadcasts these updates to all other clients. This ensures that all clients have a consistent view of the whiteboard.

**Message Format**: The messages exchanged between clients and the server include information about the drawing actions, such as the type of brush used, color, coordinates, and text (if applicable). These messages are serialized and transmitted over the network.

## Class Diagram

![Class Diagram](Design%20Diagrams/Class%20Diagram.png)

## Sequence Diagram

![Sequence Diagram](Design%20Diagrams/Sequence%20Diagram.png)


## Implementation Details

### Server Package
**WhiteBoardServer**: Binds the server service to the RMI registry and handles client connections. It manages client interactions and ensures synchronization across all clients by broadcasting messages to all connected clients.

**ClientManager**: Manages the list of connected clients, allowing clients to be added or removed. It keeps track of active clients and provides methods to update and manage the client list.

**Server**: Handles the initialization of the whiteboard server. It sets up the RMI registry, binds the WhiteBoardServer service, and ensures the server is ready to accept client connections.

### Client Package
**WhiteBoard**: Implements the drawing whiteboard and user interface. It supports drawing tools: eraser, line, rectangle, circle, oval, and text. It handles user interactions for drawing and updates the whiteboard accordingly.

**WhiteBoardClient**: Manages the client-side operations, including connecting to the server, updating the whiteboard, and handling user inputs. It communicates with the server to join the whiteboard session, send drawing updates, and receive updates from other clients.

**Message**: Sets up the message format and handles message methods. It implements the details of messages exchanged between clients and the server, including the state of the drawing action, client name, brush type, color, coordinates, and any associated text.

### Remote Package
**ServerInterface**: Defines the methods for server-side operations that can be called remotely by clients. This includes methods for clients to join or leave the whiteboard, broadcast messages, and manage the whiteboard state.

**ClientInterface**: Defines the methods for client-side operations that can be called remotely by the server. This includes methods for updating the client’s whiteboard, receiving messages, and handling client-specific actions.

**MessageInterface**: Defines the structure of the messages exchanged between the server and clients. It includes details such as the state of the drawing action, the name of the client, the brush type, color, coordinates, and any associated text.

## Requirements

- Java Development Kit (JDK) 8 or later
- Internet connection for networked communication

## Getting Started

### Running the Whiteboard

1. **Start the Whiteboard Manager**:
   Run WhiteBoardServer.jar file:
   java -jar WhiteBoardServer.jar <serverPort>
2. **Join the Whiteboard as a User**:
   Run WhiteBoardClient.jar file:
   java -jar WhiteBoardClient.jar <serverIPAddress> <serverPort> <username>

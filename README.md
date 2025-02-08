# Chat Transfer

**Purpose:** CNT-5106 Computer Networks - Project 2 - University of Florida

This is a simple Java-based chat application that allows two users to communicate over a network connection. The application supports text messaging and file transfer between users.

## Features

- Text messaging between two users
- File transfer capability
- Multi-threaded design for simultaneous reading and writing

## Prerequisites

- Java Development Kit (JDK) 8 or higher

## How to Use

1. Compile the Java file:
   ```bash
   javac Chat.java
   ```

2. Run the application for each user:
   ```bash
   java Chat <username> <port>
   ```
   Replace `<username>` with the desired username and `<port>` with the port number to listen on.

3. When prompted, enter the port number of the user you want to connect with.

4. Start chatting! Type messages and press Enter to send.

5. To transfer a file, use the command:
   ```
   transfer <filename>
   ```
   Replace `<filename>` with the name of the file you want to send.

## Class Structure

### Chat

The main class that initializes the application and sets up the connection.

### WriteThread

Handles outgoing messages and file transfers.

### ReadThread

Manages incoming messages and file receptions.

## Notes

- The application uses localhost for connections, so it's designed for use on a single machine or local network.
- Received files are saved with "new" prefixed to the original filename.
- Error handling is implemented for various scenarios like file not found or connection issues.

## Limitations

- The application currently supports only two users.
- File transfer doesn't show progress or completion status.
- The application doesn't have a graphical user interface.

## Future Improvements

- Add support for multiple users
- Implement a graphical user interface
- Enhance file transfer with progress indicators and confirmation messages
- Add encryption for secure communication

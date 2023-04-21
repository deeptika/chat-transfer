import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Objects;

public class Chat {
    public static void main(String[] args) throws IOException {
        // creating local socket
        ServerSocket localSocket = new ServerSocket(0);

        // starting server
        System.out.println(localSocket.getLocalSocketAddress() + "'s program started!\nListening on port " + localSocket.getLocalPort());

        // start write thread
        new WriteThread().start();

        // connecting to remote port
        Socket remoteSocket = localSocket.accept();
        System.out.println("Obtained connection from " + remoteSocket.getRemoteSocketAddress() + " successfully!");

        // start read thread
        new ReadThread(remoteSocket).start();
    }
}

class WriteThread extends Thread {
    private BufferedReader standardInputStream; // stream to read user input
    private Socket remoteSocket; // socket to represent another user thread
    private PrintWriter outputStream; // output stream of current user's socket
    private BufferedReader socketInputStream; // stream to read socket's input
    public void run() {
        try {
            // connecting to another user
            System.out.print("Which user would you like to connect with? Enter their port number:\n");

            // getting details about the user's socket
            standardInputStream = new BufferedReader(new InputStreamReader(System.in));
            remoteSocket = new Socket("localhost", Integer.parseInt(standardInputStream.readLine()));
            System.out.println("Connected to: " + remoteSocket.getRemoteSocketAddress());
            outputStream = new PrintWriter(remoteSocket.getOutputStream(), true);
            socketInputStream = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));

            // file transfer or message sending
            String message;
            while ((message = standardInputStream.readLine()) != null) {
                // writing each message to the output stream
                outputStream.println(message);

                // handling file transfer
                if (message.startsWith("transfer ")) {
                    String fileName = message.substring(9);
                    File file = new File(fileName);

                    // checking if file does not exist
                    if (!file.exists()) {
                        System.out.println("ERROR - File does not exist!");
                    } else {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int writePtr;
                        // sending file to output stream
                        while ((writePtr = fileInputStream.read(buffer)) != -1) {
                            remoteSocket.getOutputStream().write(buffer, 0, writePtr);
                        }
                        remoteSocket.getOutputStream().flush();
                        fileInputStream.close();
                    }
                }
            }
        } catch (IOException ioException) {
            System.err.println("Unexpected error in sending message!");
        } finally {
            // closing streams and socket
            try {
                socketInputStream.close();
                remoteSocket.close();
            } catch (IOException ioException) {
                System.err.println("Cannot close sockets!");
            }
        }
    }
}

class ReadThread extends Thread {
    private Socket socket; // socket that reads incoming messages
    private BufferedReader socketInputStream; // input stream of socket

    public ReadThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // read incoming messages and print them to the console
            socketInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;
            while ((message = socketInputStream.readLine()) != null) {

                // handles incoming transferred file
                if (message.startsWith("transfer ")) {
                    String fileName = "new" + message.substring(9);
                    FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    // storing file
                    while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    fileOutputStream.close();
                } else {
                    // displaying message
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Unexpected error in receiving message!");
        } finally {
            // closing socket
            try {
                socketInputStream.close();
            } catch(IOException e)  {
                System.err.println("Cannot close sockets!");
            }
        }
    }
}

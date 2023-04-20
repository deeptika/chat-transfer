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
        System.out.println(localSocket.getLocalSocketAddress() + "'s program started! Listening on port " + localSocket.getLocalPort());

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
    BufferedReader standardInput;  // to read socket input
    BufferedReader socketInput;
    private Socket remoteSocket; //socket that listens to other user
    private ObjectOutputStream outputStream; // stream to write to the socket

    public void run() {
        try {
            standardInput = new BufferedReader(new InputStreamReader(System.in));

            // connect to other user's port
            System.out.println("What's the port of the user you'd like to connect to?");
            remoteSocket = new Socket("localhost", Integer.parseInt(standardInput.readLine()));
            System.out.println("Connected to " + remoteSocket.getRemoteSocketAddress() + " successfully!\n");

            // reading message from output
            outputStream = new ObjectOutputStream(remoteSocket.getOutputStream());
            standardInput = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
            //message received from the remote port
            String message;
            while ((message = standardInput.readLine()) != null) {
                // writing message to output stream
                outputStream.writeObject(message);
                outputStream.flush();

                // handling command "transfer <fileName>"
                if (message.startsWith("transfer")) {
                    String fileName = message.substring(9);
                    boolean flag = false;
                    // current working directory of this thread
                    String currentDirectory = "./";
                    File folder = new File(currentDirectory);

                    for (File file : Objects.requireNonNull(folder.listFiles())) {
                        if (file.getName().equals(fileName)) {
                            System.out.println("Found file " + file.getName() + " in working directory, commencing transfer.");
                            flag = true;
                            byte[] content = Files.readAllBytes(file.toPath());
                            outputStream.writeObject(content);
                            outputStream.flush();
                            System.out.println("File " + file.getName() + " transferred successfully!");
                        }
                    }
                    if (!flag) {
                        System.out.println("ERROR - File not found in this working directory!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
                socketInput.close();
                remoteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ReadThread extends Thread {
    BufferedReader socketInput; // to receive input from user
    ObjectInputStream inputStream; // stream to read socket input
    private final Socket socket;    // socket that listens to client
    String currentDirectory = "./"; // current working directory of this thread

    public ReadThread(Socket socket) {
        this.socket = socket;
    }

    public void run()   {
        try {
            // reading incoming messages
            socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // message received from the client
            String message;
            while ((message = socketInput.readLine()) != null) {

                // handling file transfer - obtaining transferred file and storing it
                if (message.startsWith("transfer")) {
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    String fileName = message.substring(9);
                    File file = new File(currentDirectory + "new" + fileName);
                    byte[] content = (byte[]) inputStream.readObject();
                    Files.write(file.toPath(), content);
                    System.out.println("File " + fileName + ": get is successful.");
                } else {
                    // printing messages to console
                    System.out.println(message);
                }
            }
        } catch(Exception e)    {
            e.printStackTrace();
        }   finally {
            try {
                socketInput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

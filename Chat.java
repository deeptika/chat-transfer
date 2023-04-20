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
        System.out.println(localSocket.getLocalSocketAddress() + " started!\nListening on port " + localSocket.getLocalPort());

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
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in)); // to read socket input
    private String message;    //message received from the remote port
    private Socket remoteSocket; //socket that listens to other user
    ObjectOutputStream outputStream = null; // stream to write to the socket
    String currentDirectory = "./"; // current working directory of this thread

    public void run() {
        try {
            // connect to other user's port
            System.out.println("What's the port of the user you'd like to connect to?");
            remoteSocket = new Socket("localhost", Integer.parseInt(bufferedReader.readLine()));
            System.out.println("Connected to " + remoteSocket.getRemoteSocketAddress() + " successfully!\n");

            // reading message from output
            while ((message = bufferedReader.readLine()) != null) {
                outputStream = new ObjectOutputStream(remoteSocket.getOutputStream());
                outputStream.writeObject(message);

                // handling command "transfer <fileName>"
                if (message.startsWith("transfer")) {
                    String fileName = message.substring(9);
                    boolean flag = false;
                    File folder = new File(currentDirectory);

                    for (File file : Objects.requireNonNull(folder.listFiles())) {
                        if (file.getName().equals(fileName)) {
                            System.out.println("Found file " + file.getName() + " in working directory, commencing transfer.");
                            flag = true;
                            byte[] content = Files.readAllBytes(file.toPath());
                            outputStream.writeObject(content);
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
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ReadThread extends Thread {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in)); // to receive input from user
    ObjectInputStream inputStream = null; // stream to read socket input
    private String message;    // message received from the client
    private final Socket socket;    // socket that listens to client
    String currentDirectory = "./"; // current working directory of this thread

    public ReadThread(Socket socket) {
        this.socket = socket;
    }

    public void run()   {
        try {
            while ((message = bufferedReader.readLine()) != null) {
                if (message.startsWith("transfer")) {
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    String fileName = message.substring(9);
                    File file = new File(currentDirectory + "new" + fileName);
                    byte[] content = (byte[]) inputStream.readObject();
                    Files.write(file.toPath(), content);
                    System.out.println("File " + fileName + ": get is successful.");
                } else {
                    System.out.println(message);
                }
            }
        } catch(Exception e)    {
            e.printStackTrace();
        }   finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

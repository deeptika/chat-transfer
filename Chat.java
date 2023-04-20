import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Objects;

public class Chat {
    private static final int serverPort = 4007;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(serverPort);

        //connecting to the client
        try {
            while (true) {
                // starting server
                System.out.println("Listening on port " + serverSocket.getLocalPort());

                // connecting to client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " is connected to the server!");

                // start write thread
                new WriteThread(clientSocket).start();

                // start read thread
                new ReadThread(clientSocket).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static class WriteThread extends Thread {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in)); // to receive input from user
        private String msgFromClient;    //message received from the client
        private final Socket clientSocket;    //socket that listens to client
        private Socket otherClientSocket; //socket that listens to other client
        ObjectOutputStream outputStream = null; // stream to write to the socket
        String currentDirectory = "./testFiles/"; // current working directory of this thread

        public WriteThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                //initializing input and output streams
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStream.flush();

                // connect to other user's port
                System.out.println("What's the port of the user you'd like to connect to?");
                int otherPort = Integer.parseInt(bufferedReader.readLine());
                otherClientSocket = new Socket("localhost", otherPort);
                System.out.println("Connected to: " + otherClientSocket.getRemoteSocketAddress());

                // file transfer functionality
                while ((msgFromClient = bufferedReader.readLine()) != null) {
                    outputStream.writeObject(msgFromClient);
                    outputStream.flush();

                    String[] splitCommand = msgFromClient.split("\\s+");

                    if (splitCommand.length == 2 && Objects.equals(splitCommand[0], "transfer")) {
                        boolean flag = false;

                        File folder = new File(currentDirectory);
                        for (File file : Objects.requireNonNull(folder.listFiles())) {
                            if (file.getName().equals(splitCommand[1])) {
                                System.out.println("Found file " + file.getName() + " in working directory, commencing upload.");
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

    private static class ReadThread extends Thread {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in)); // to receive input from user
        private String msgFromClient;    //message received from the client
        private final Socket clientSocket;    //socket that listens to client

        public ReadThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run()   {
            try {
                while ((msgFromClient = bufferedReader.readLine()) != null) {
                    String[] splitCommand = msgFromClient.split("\\s+");
                    if (splitCommand.length == 2 && Objects.equals(splitCommand[0], "transfer")) {
                        FileOutputStream fout = new FileOutputStream(splitCommand[1]);

                        byte[] buffer = new byte[1024];
                        int readPtr;
                        while((readPtr = clientSocket.getInputStream().read(buffer)) != -1) {
                            fout.write(buffer, 0, readPtr);
                        }
                        fout.close();
                    }   else    {
                        System.out.println(msgFromClient);
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
}

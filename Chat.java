import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
    public void run() {
        try {
            // connect to another user's port
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter port number to connect to: ");
            int remotePort = Integer.parseInt(stdIn.readLine());
            Socket socket = new Socket("localhost", remotePort);
            System.out.println("\nConnected to: " + socket.getRemoteSocketAddress());

            // start file transfer or message sending loop
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.startsWith("transfer ")) {
                    String fileName = userInput.substring(9);
                    File file = new File(fileName);
                    if (!file.exists()) {
                        System.out.println("File does not exist.");
                    } else {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            socket.getOutputStream().write(buffer, 0, bytesRead);
                        }
                        socket.getOutputStream().flush();
                        fileInputStream.close();
                    }
                }
            }

            // close socket and streams
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ReadThread extends Thread {
    private Socket socket;

    public ReadThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // read incoming messages and print them to the console
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("transfer ")) {
                    String fileName = "new" + inputLine.substring(9);
                    FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    fileOutputStream.close();
                } else {
                    System.out.println(inputLine);
                }
            }

            // close socket and streams
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

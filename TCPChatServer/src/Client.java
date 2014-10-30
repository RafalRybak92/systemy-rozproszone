import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {
    private Socket         mSocket;
    private BufferedReader mIn;
    private PrintStream    mOut;
    private BufferedReader mWrite;
    public boolean         mKilled;

    public Client(Socket socket) {
        mSocket = socket;
        initialiseStreams();
    }

    private void initialiseStreams() {
        try {
            mIn = new BufferedReader(new InputStreamReader(
                    mSocket.getInputStream()));
            mOut = new PrintStream(mSocket.getOutputStream());
            mWrite = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            System.out
                    .println("Problem with initialising pipes on socket.\n I/O Problem.");
        }
    }

    @Override
    public void run() {
        if (mSocket != null && mIn != null && mWrite != null) {
            String line;
            try {
                while (true) {
                    if (!((line = mIn.readLine()) != null)) {
                        System.out.println("You have been disconected!");
                        break;
                    }
                    if (line.equals("/quit")) {
                        break;
                    }
                    System.out.println(line);
                }
                mKilled = true;
            } catch (IOException e) {
                System.out
                        .println("Problem with initialising pipes on socket.\n I/O Problem.");
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out
                    .println("You can't run client without parameters! ip port");
        } else {
            String ipPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
            if (args[0].matches(ipPattern)) {
                int port = 0;
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException nfe) {
                    System.out.println("bad port format.");
                    return;
                }
                try {
                    Socket clientSocket = new Socket(args[0], port);
                    Client client = new Client(clientSocket);
                    new Thread(client).start();
                    while (!client.mKilled) {
                        client.mOut.println(client.mWrite.readLine().trim());
                    }
                    client.mIn.close();
                    client.mOut.close();
                    client.mWrite.close();
                    client.mSocket.close();
                } catch (UnknownHostException e) {
                    System.out.println("Unknown host/Bad IP of Server.");
                } catch (IOException e) {
                    System.out
                            .println("Problem with pipes on socket.\n I/O Problem.");
                }
            } else {
                System.out.println("bad ip");
            }
        }
    }

}

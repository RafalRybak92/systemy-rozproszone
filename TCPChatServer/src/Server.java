import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class Server {
    private ServerSocket                      mServerSocket;
    private final static int                  MAX_CLIENTS = 10;
    private int                               mPort;
    private static final Vector<ClientThread> sThreads    = new Vector<ClientThread>();

    public Server(ServerSocket serverSocket) {
        mServerSocket = serverSocket;
        mPort = mServerSocket.getLocalPort();
    }

    class ClientThread extends Thread {
        private Socket               mClientSocket;
        private Vector<ClientThread> mOtherClients;
        private BufferedReader       mInputStream = null;
        private PrintStream          mPrintStream = null;
        private String               mUserThreadName;

        public ClientThread(Socket clientSocket,
                Vector<ClientThread> otherClients) {
            mClientSocket = clientSocket;
            mOtherClients = otherClients;
        }

        @Override
        public void run() {
            try {
                mInputStream = new BufferedReader(new InputStreamReader(
                        mClientSocket.getInputStream()));
                mPrintStream = new PrintStream(mClientSocket.getOutputStream());
                mPrintStream.println("What's your name/nickname?");
                mUserThreadName = mInputStream.readLine();

                for (int i = 0; i < mOtherClients.size(); i++) {
                    ClientThread cT = mOtherClients.get(i);
                    cT.mPrintStream.println("\n Welcome new user: "
                            + mUserThreadName);
                }

                String message;
                while ((message = mInputStream.readLine()) != null) {
                    for (int i = 0; i < mOtherClients.size(); i++) {
                        ClientThread cT = mOtherClients.get(i);
                        cT.mPrintStream.println(mUserThreadName + ": "
                                + message);
                    }
                }
                synchronized (this) {
                    for (int i = 0; i < mOtherClients.size(); i++) {
                        ClientThread cT = mOtherClients.get(i);
                        cT.mPrintStream.println("XXXXXX " + mUserThreadName
                                + " LEAVES DA PARTY! XXXXXXX");
                    }

                    for (int i = 0; i < mOtherClients.size(); i++) {
                        ClientThread cT = mOtherClients.get(i);
                        if (cT == this) {
                            mOtherClients.remove(i);
                            break;
                        }
                    }
                }
                mInputStream.close();
                mPrintStream.close();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void listIps() {
        Enumeration<NetworkInterface> en;
        try {
            en = NetworkInterface.getNetworkInterfaces();

            while (en.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) en.nextElement();
                System.out.print(ni.getName() + " ");
                ArrayList<InetAddress> list = Collections.list(ni
                        .getInetAddresses());
                for (int i = 1; i < list.size(); i = +2) {
                    System.out.println(list.get(i).getHostAddress());
                }
            }
            System.out.println("port: " + mPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server tcpChatServer = null;
        Socket clientSocket = null;
        try {
            tcpChatServer = new Server(new ServerSocket(0));
            tcpChatServer.listIps();
            while (true) {
                clientSocket = tcpChatServer.mServerSocket.accept();

                if (sThreads.size() <= MAX_CLIENTS) {
                    Server.ClientThread client = tcpChatServer.new ClientThread(
                            clientSocket, sThreads);
                    sThreads.addElement(client);
                    client.start();
                } else {
                    PrintStream out = new PrintStream(
                            clientSocket.getOutputStream());
                    out.print("SERVER IS BUSY, TRY AGAIN LATER!");
                    out.close();
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.out
                    .println("Problem with initialising pipes on socket.\n I/O Problem.");
        }
    }
}

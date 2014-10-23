import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private ServerSocket                      mServerSocket;
    private final int                         MAX_CLIENTS = 10;
    private int                               mPort;
    private static final Vector<ClientThread> sThreads    = new Vector<ClientThread>();

    public Server(ServerSocket serverSocket) {
        mServerSocket = serverSocket;
        mPort = mServerSocket.getLocalPort();
        System.out.println("+++++Server Stands on: ++++++");
        System.out.println(mPort);
        System.out.println("+++++PORT++++++");
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
                    cT.mPrintStream.println("Welcome new user: "
                            + mUserThreadName);
                }
                String message = null;
                while (true) {
                    message = mInputStream.readLine();

                    if (message.equals("/quit")) {
                        break;
                    }
                    for (int i = 0; i < mOtherClients.size(); i++) {
                        ClientThread cT = mOtherClients.get(i);
                        cT.mPrintStream.println(mUserThreadName + ": "
                                + message);
                    }
                }

                for (int i = 0; i < mOtherClients.size(); i++) {
                    ClientThread cT = mOtherClients.get(i);
                    cT.mPrintStream.println("XXXXXX" + mUserThreadName
                            + " LEAVES DA PARTY! XXXXXXX");
                }

                for (int i = 0; i < mOtherClients.size(); i++) {
                    ClientThread cT = mOtherClients.get(i);
                    if (cT == this) {
                        mOtherClients.remove(i);
                        break;
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

    public static void main(String[] args) {
        Server tcpChatServer = null;
        Socket clientSocket = null;
        try {
            tcpChatServer = new Server(new ServerSocket(0));
            while (true) {
                clientSocket = tcpChatServer.mServerSocket.accept();
                if (sThreads.size() <= 9) {
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
            e.printStackTrace();
        }
    }
}

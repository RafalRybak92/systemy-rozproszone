import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private ServerSocket         mServerSocket;
    private Socket               mClientSocket;
    private final int            MAX_CLIENTS = 10;
    private int                  mPort;
    private Vector<ClientThread> mThreads    = new Vector<ClientThread>();

    public Server(ServerSocket serverSocket) {
        mServerSocket = serverSocket;
        mPort = mServerSocket.getLocalPort();
        System.out.println("+++++Server Stands on: ++++++");
        System.out.println(mPort);
        System.out.println("+++++PORT++++++");
    }

    class ClientThread implements Runnable {
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
        try {
            tcpChatServer = new Server(new ServerSocket(0));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

        }
    }
}
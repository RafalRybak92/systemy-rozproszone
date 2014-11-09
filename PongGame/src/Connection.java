import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection extends Thread {
    private Socket             mConnection;
    private ObjectOutputStream mOut;
    private ObjectInputStream  mIn;
    private Object             rawData;
    private GameStatus         receivedGameStatus;

    public Connection(Socket connection) {
        this.mConnection = connection;
    }

    @Override
    public void run() {
        super.run();
        try {
            mIn = new ObjectInputStream(mConnection.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while (true) {
            try {
                rawData = mIn.readObject();
                receivedGameStatus = (GameStatus) rawData;
                System.out.println(receivedGameStatus);

            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Connection connect = new Connection(new Socket("localhost", 2222));
            connect.start();
            connect.mOut = new ObjectOutputStream(
                    connect.mConnection.getOutputStream());
            int[] pos = { 4, 5 };
            while (true) {
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

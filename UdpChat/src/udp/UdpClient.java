import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpClient implements Runnable {
    private DatagramSocket mSocket;
    private BufferedReader mWrite;
    private PrintStream    mOut;
    private InetAddress    mConnectedAddress;
    private String         mNickname;

    public UdpClient(DatagramSocket socket, InetAddress addres) {
        mSocket = socket;
        mConnectedAddress = addres;
        mWrite = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        if (mSocket != null & mWrite != null) {
            String line;
            try {
                while ((line = mWrite.readLine()) != null) {
                    DatagramPacket dp = new DatagramPacket(line.getBytes(),
                            line.length(), mConnectedAddress, mSocket.getPort());
                    mSocket.send(dp);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket(3333);
                    byte[] b = args[0].getBytes("UTF-16");
                    InetAddress addres = InetAddress.getByName(args[0]);
                    socket.connect(addres, port);
                    UdpClient client = new UdpClient(socket, addres);
                    System.out.println(socket.getInetAddress());
                    DatagramPacket packet = new DatagramPacket(new byte[256],
                            256);
                    DatagramPacket dp = new DatagramPacket(
                            "**connection**".getBytes(),
                            "**connection**".getBytes().length, addres, port);
                    socket.send(dp);
                    new Thread(client).start();
                    while (true) {
                        socket.receive(packet);

                        System.out.println(new String(packet.getData(), packet
                                .getOffset(), packet.getLength()));
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}

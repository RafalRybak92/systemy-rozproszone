package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class UdpServer {
    private static final int                           MAX_BUFF_DATAGRAM = 256;
    private DatagramSocket                             mSocket;
    private DatagramPacket                             mPacket;
    private static ArrayList<UdpServer.DatagramClient> mGroup            = new ArrayList<UdpServer.DatagramClient>();
    private boolean                                    mKilled           = false;
    private int                                        mPort;

    public UdpServer(DatagramSocket datagramsocket) {
        mSocket = datagramsocket;
        mPort = mSocket.getLocalPort();
    }

    public boolean acceptanceThreadRun() {
        Thread acceptanceThread = new Thread(new Runnable() {

            @Override
            public void run() {

            }
        });
        return acceptanceThread.isAlive();
    }

    public boolean messageThreadController() {
        Thread messageControl = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!mKilled) {
                    try {
                        System.out.println("Running");
                        mPacket = new DatagramPacket(
                                new byte[MAX_BUFF_DATAGRAM], MAX_BUFF_DATAGRAM);
                        mSocket.receive(mPacket);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    String message = new String(mPacket.getData(), mPacket
                            .getOffset(), mPacket.getLength());
                    DatagramClient client = new DatagramClient(mPacket
                            .getAddress(), mPacket.getPort());
                    System.out.println(message);
                    System.out.println(message.length());
                    if (message.startsWith("**connection**")) {
                        new Thread(client).start();
                    }

                }
            }
        });
        messageControl.start();

        return messageControl.isAlive();
    }

    class DatagramClient extends Object implements Runnable {
        private InetAddress mAddress;
        private boolean     mShouldRun = true;
        private String      mNickname;
        private int         mClientPort;

        public DatagramClient(InetAddress adress, int port) {
            System.out.println("CreatedClient");
            mAddress = adress;
            mClientPort = port;
        }

        public String getNickname() {
            return mNickname;
        }

        public InetAddress getAddress() {
            return mAddress;
        }

        public void shouldNotBeRunning() {
            mShouldRun = false;
        }

        public int getClientPort() {
            return mClientPort;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            DatagramClient client = (DatagramClient) obj;
            if ((this.getClientPort() == client.getClientPort())
            // porownanie nazw na potrzeby zadania i pokazania na jednym
            // komputerze
                    & this.getNickname().equals(client.getNickname())) {
                return true;
            }

            return false;
        }

        @Override
        public void run() {
            System.out.println("Started thread for connection");
            DatagramPacket data = new DatagramPacket(new byte[256], 256);
            try {

                byte[] what = "What's your nickname?".getBytes();
                byte[] loggedIn = "I'am sorry but you're already logged in. Bye."
                        .getBytes();
                mSocket.send(new DatagramPacket(what, what.length, mAddress,
                        getClientPort()));
                mSocket.receive(data);

                mNickname = new String(data.getData(), data.getOffset(),
                        data.getLength());
                System.out
                        .println(this.getClientPort() + mNickname + "::added");
                if (mGroup.contains(this)) {
                    mShouldRun = false;
                    mSocket.send(new DatagramPacket(loggedIn, loggedIn.length,
                            mAddress, getClientPort()));
                    System.out.println(this.getClientPort() + mNickname
                            + "::exists");
                    return;
                } else {
                    mGroup.add(this);
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }

            while (mShouldRun) {
                try {
                    mSocket.receive(data);
                    for (int i = 0; i < mGroup.size(); i++) {
                        DatagramClient iteratedClient = mGroup.get(i);
                        if (!(iteratedClient.equals(this))) {
                            DatagramPacket packet = new DatagramPacket(
                                    data.getData(), data.getData().length,
                                    iteratedClient.getAddress(),
                                    iteratedClient.getClientPort());
                            try {
                                mSocket.send(packet);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e1) {
                    mGroup.remove(this);
                    System.out.println(e1.getMessage());
                    mShouldRun = false;
                    return;
                }
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
                for (int i = 0; i < list.size(); i++) {
                    System.out.println(list.get(i).getHostAddress());
                }
            }
            System.out.println("port: " + mPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(2222);
            UdpServer server = new UdpServer(datagramSocket);
            server.listIps();
            server.messageThreadController();

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;

public class UdpServer {
    private DatagramSocket                          mSocket;
    private DatagramPacket                          mPacket;
    private static Vector<UdpServer.DatagramClient> mGroup  = new Vector<UdpServer.DatagramClient>();
    private boolean                                 mKilled = false;

    public UdpServer(DatagramSocket datagramsocket) {
        mSocket = datagramsocket;
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
                        mSocket.receive(mPacket);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (!mGroup.contains(mPacket.getAddress())) {
                        mGroup.addElement(new DatagramClient(mPacket
                                .getAddress()));
                    }

                    for (int i = 0; i < mGroup.size(); i++) {
                        if (!(mGroup.get(i).getAddress().equals(mPacket
                                .getAddress()))) {
                            DatagramPacket packet = new DatagramPacket(
                                    mPacket.getData(),
                                    mPacket.getData().length, mGroup.get(i)
                                            .getAddress(), mSocket.getPort());
                            try {
                                mSocket.send(packet);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        messageControl.start();

        return messageControl.isAlive();
    }

    class DatagramClient extends Object {
        private InetAddress mAddress;
        private boolean     mShouldRun = true;

        public DatagramClient(InetAddress adress) {
            mAddress = adress;
        }

        public InetAddress getAddress() {
            return mAddress;
        }

        public void shouldNotBeRunning() {
            mShouldRun = false;
        }

        public boolean connectedControl() {
            Thread connectionCheck = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (mShouldRun) {
                        try {
                            mSocket.send(new DatagramPacket("?".getBytes(), 1,
                                    mAddress, mSocket.getPort()));
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }
            });
            connectionCheck.start();
            return connectionCheck.isAlive();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.equals(this)) {
                return true;
            }
            if (obj instanceof InetAddress) {
                InetAddress ipCompare = (InetAddress) obj;
                if (this.mAddress.equals(ipCompare)) {
                    return true;
                }

            } else {
                DatagramClient client = (DatagramClient) obj;
                if (this.getAddress().equals(client.getAddress())) {

                    return true;
                }
            }

            return false;
        }
    }

    public static void main(String[] args) {

    }

}

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

public class UdpPongServer {
    private DatagramSocket mServerSocket;
    private int            mServerPort;

    public UdpPongServer(DatagramSocket serversocket) {
        this.mServerSocket = serversocket;
        this.mServerPort = mServerSocket.getLocalPort();
    }

    class GameThread extends Thread {
        private Player      mPlayerOne;
        private Player      mPlayerTwo;
        private boolean     mGameRuns;
        private Ellipse2D   mBall;
        private int         mMoved     = 1;
        private int         mScoreRight;
        private int         mScoreLeft;
        private boolean     mStartGame;
        private int         mBallPosX;
        private int         mBallPosY;
        private int         mDirection = 1;
        private Rectangle2D mPlayerOnePaddle;
        private Rectangle2D mPlayerTwoPaddle;
        private int         mPlayerTwoPaddleY;
        private int         mPlayerOnePaddleY;
        private int         mPlayerTwoPort;

        public GameThread(Player playerOne, Player playerTwo) {
            this.mPlayerOne = playerOne;
            this.mPlayerTwo = playerTwo;
            this.mGameRuns = true;
            this.mStartGame = true;
        }

        @Override
        public void run() {
            PlayerReceiver playerOne = new PlayerReceiver(this);
            playerOne.start();
            if (mStartGame) {
                mPlayerOnePaddleY = (300 / 2) - 100 / 2;
                mPlayerTwoPaddleY = mPlayerOnePaddleY;
                mBallPosX = 400 / 2;
                mBallPosY = 300 / 2;
                mStartGame = false;
            }
            try {
                while (mGameRuns) {
                    String command = "BALL:" + mBallPosX + ":" + mBallPosY;
                    mServerSocket.send(new DatagramPacket(command.getBytes(),
                            command.length(), mPlayerOne.mIpAddress,
                            mPlayerOne.mPortAddress));
                    command = "BALL:" + mBallPosX + ":" + mBallPosY;
                    mServerSocket.send(new DatagramPacket(command.getBytes(),
                            command.length(), mPlayerTwo.mIpAddress,
                            mPlayerTwo.mPortAddress));
                    command = "p1-paddle:" + mPlayerTwoPaddleY;
                    mServerSocket.send(new DatagramPacket(command.getBytes(),
                            command.length(), mPlayerOne.mIpAddress,
                            mPlayerOne.mPortAddress));
                    command = "p2-paddle:" + mPlayerOnePaddleY;
                    mServerSocket.send(new DatagramPacket(command.getBytes(),
                            command.length(), mPlayerTwo.mIpAddress,
                            mPlayerTwo.mPortAddress));

                    mPlayerOnePaddle = new Rectangle(5, mPlayerOnePaddleY, 10,
                            100);

                    mPlayerTwoPaddle = new Rectangle(400 - 10 - 5,
                            mPlayerTwoPaddleY, 10, 100);
                    mBall = new Ellipse2D.Double(mBallPosX, mBallPosY, 10, 10);

                    if (mBall.intersects(mPlayerOnePaddle)
                            | mBall.intersects(mPlayerTwoPaddle)) {
                        mDirection *= -1;
                    } else if (mBall.intersects(0, 10, 400, 10)
                            | mBall.intersects(0, 300, 400, 300)) {
                        mMoved *= -1;
                    } else if (mBallPosX == 0) {
                        mScoreRight++;
                        mBallPosX = 400 / 2;
                        mBallPosY = 200 / 2;
                        mDirection *= -1;
                        command = "points:" + mScoreLeft + ":" + mScoreRight;
                        mServerSocket
                                .send(new DatagramPacket(command.getBytes(),
                                        command.length(),
                                        mPlayerTwo.mIpAddress,
                                        mPlayerTwo.mPortAddress));
                        mServerSocket
                                .send(new DatagramPacket(command.getBytes(),
                                        command.length(),
                                        mPlayerOne.mIpAddress,
                                        mPlayerOne.mPortAddress));
                    } else if (mBallPosX == 400 - 5) {
                        mScoreLeft++;
                        mBallPosX = 400 / 2;
                        mBallPosY = 300 / 2;
                        mDirection *= -1;
                        command = "points:" + mScoreLeft + ":" + mScoreRight;
                        mServerSocket
                                .send(new DatagramPacket(command.getBytes(),
                                        command.length(),
                                        mPlayerTwo.mIpAddress,
                                        mPlayerTwo.mPortAddress));
                        mServerSocket
                                .send(new DatagramPacket(command.getBytes(),
                                        command.length(),
                                        mPlayerOne.mIpAddress,
                                        mPlayerOne.mPortAddress));
                    }
                    mBallPosX = mBallPosX + mDirection;
                    mBallPosY += mMoved;
                    Thread.sleep(5);

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                mGameRuns = false;
                System.out.println("mGameRuns: " + mGameRuns);
                System.out.println("game Ended");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        class PlayerReceiver extends Thread {
            private Player     mPlayer;
            private GameThread mGame;

            public PlayerReceiver(GameThread game) {
                this.mGame = game;
            }

            @Override
            public void run() {
                DatagramPacket packet = new DatagramPacket(new byte[256], 256);
                String command;
                try {
                    while (true) {
                        System.out.println(" mGame.mGameRuns: "
                                + mGame.mGameRuns);

                        mServerSocket.receive(packet);

                        command = new String(packet.getData(),
                                packet.getOffset(), packet.getLength());
                        String[] splitted = command.split(":");
                        switch (splitted[0]) {
                            case "p1":
                                mGame.mPlayerOnePaddleY = Integer
                                        .parseInt(splitted[1]);
                                break;
                            case "p2":
                                mGame.mPlayerTwoPaddleY = Integer
                                        .parseInt(splitted[1]);
                                break;
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Player {
        private InetAddress mIpAddress;
        private int         mPortAddress;

        public Player(InetAddress ipAddress, int portAddress) {
            this.mIpAddress = ipAddress;
            this.mPortAddress = portAddress;

        }
    }

    public static void main(String[] args) {
        System.out.println("ServerStarted");
        DatagramSocket serversocket = null;
        String receivedCommand;
        DatagramPacket newPlayerPacket = new DatagramPacket(new byte[256], 256);
        Stack<Player> connectionStack = new Stack<Player>();
        try {
            serversocket = new DatagramSocket(2222);
            UdpPongServer server = new UdpPongServer(serversocket);
            while (true) {
                serversocket.receive(newPlayerPacket);
                receivedCommand = new String(newPlayerPacket.getData(),
                        newPlayerPacket.getOffset(),
                        newPlayerPacket.getLength());
                if (receivedCommand.contains("**connection**")) {
                    System.out.println("**connection**");
                    Player newPlayer = server.new Player(
                            newPlayerPacket.getAddress(),
                            newPlayerPacket.getPort());
                    connectionStack.push(newPlayer);
                    if (connectionStack.size() >= 2) {
                        Player p1 = connectionStack.pop();
                        Player p2 = connectionStack.pop();
                        GameThread game = server.new GameThread(p1, p2);
                        serversocket.send(new DatagramPacket("player1"
                                .getBytes(), "player1".length(), p1.mIpAddress,
                                p1.mPortAddress));
                        serversocket.send(new DatagramPacket("player2"
                                .getBytes(), "player2".length(), p2.mIpAddress,
                                p2.mPortAddress));
                        game.start();
                    }
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

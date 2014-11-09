import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

public class TcpPongServer {
    private ServerSocket mServerSocket;
    private int          mServerPort;

    public TcpPongServer(ServerSocket serversocket) {
        this.mServerSocket = serversocket;
        this.mServerPort = mServerSocket.getLocalPort();
    }

    class GameThread extends Thread {
        private PrintStream mOutPlayerOne;
        private PrintStream mOutPlayerTwo;
        private Socket      mPlayerOne;
        private Socket      mPlayerTwo;
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

        public GameThread(Socket playerOne, Socket playerTwo)
                throws IOException {
            this.mPlayerOne = playerOne;
            this.mPlayerTwo = playerTwo;
            this.mGameRuns = true;
            mOutPlayerOne = new PrintStream(mPlayerOne.getOutputStream());
            mOutPlayerTwo = new PrintStream(mPlayerTwo.getOutputStream());
            this.mStartGame = true;
        }

        @Override
        public void run() {

            PlayerReceiver playerOne = new PlayerReceiver(mPlayerOne, this,1);
            PlayerReceiver playerTwo = new PlayerReceiver(mPlayerTwo, this,2);
            playerOne.start();
            playerTwo.start();
            if (mStartGame) {
                mPlayerOnePaddleY = (300 / 2) - 100 / 2;
                mPlayerTwoPaddleY = mPlayerOnePaddleY;
                mBallPosX = 400 / 2;
                mBallPosY = 300 / 2;
                mStartGame = false;
            }
            try {
                while (mGameRuns) {
                    int[] ballPosXY = { mBallPosX, mBallPosY };
                    mOutPlayerOne
                            .println("BALL:" + mBallPosX + ":" + mBallPosY);
                    mOutPlayerTwo
                            .println("BALL:" + mBallPosX + ":" + mBallPosY);
                    mOutPlayerOne.println("p1-paddle:" + mPlayerTwoPaddleY);
                    mOutPlayerTwo.println("p2-paddle:" + mPlayerOnePaddleY);

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
                        mOutPlayerTwo.println("points:" + mScoreLeft + ":"
                                + mScoreRight);
                        mOutPlayerOne.println("points:" + mScoreLeft + ":"
                                + mScoreRight);
                    } else if (mBallPosX == 400 - 5) {
                        mScoreLeft++;
                        mBallPosX = 400 / 2;
                        mBallPosY = 300 / 2;
                        mDirection *= -1;
                        mOutPlayerTwo.println("points:" + mScoreLeft + ":"
                                + mScoreRight);
                        mOutPlayerOne.println("points:" + mScoreLeft + ":"
                                + mScoreRight);
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
            }

        }

        class PlayerReceiver extends Thread {
            private Socket         mPlayer;
            private BufferedReader mInputStream;
            private GameThread     mGame;
            private int mWhichPlayer;

            public PlayerReceiver(Socket player, GameThread game, int playerNumber) {
                this.mPlayer = player;
                this.mGame = game;
                this.mWhichPlayer = playerNumber;
            }

            @Override
            public void run() {
                try {
                    this.mInputStream = new BufferedReader(
                            new InputStreamReader(mPlayer.getInputStream()));
                    String command;
                    while ((command = mInputStream.readLine()) != null) {
                        System.out.println(" mGame.mGameRuns: "
                                + mGame.mGameRuns);
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
                    try {
                        if(mWhichPlayer == 1){
                            mOutPlayerTwo.println("p1-disconnected");
                        }else {
                            mOutPlayerOne.println("p2-disconnected");
                        }
                        mGameRuns =false;
                        System.out.println("Player ended.");
                        mInputStream.close();
                        mPlayer.close();

                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        System.out.println("ServerStarted");
        ServerSocket serversocket = null;
        Stack<Socket> connectionStack = new Stack<Socket>();
        try {
            serversocket = new ServerSocket(2222);
            TcpPongServer server = new TcpPongServer(serversocket);
            while (true) {
                connectionStack.push(serversocket.accept());
                if (connectionStack.size() >= 2) {
                    Socket player1 = connectionStack.pop();
                    Socket player2 = connectionStack.pop();
                    TcpPongServer.GameThread game = server.new GameThread(
                            player1, player2);
                    game.mOutPlayerOne.println("player1");
                    game.mOutPlayerTwo.println("player2");
                    game.start();
                }

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
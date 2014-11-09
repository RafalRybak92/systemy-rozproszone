import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.spec.MGF1ParameterSpec;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PongPanel extends JPanel implements ActionListener, KeyListener,
        Runnable {
    private static final int STEP          = 5;
    private static final int PADDLE_HEIGHT = 100;
    private static final int PADDLE_WIDTH  = 10;
    private static final int MARGIN        = 5;
    private static final int SCORE_HEIGHT  = 20;
    private int              mPlayerOnePaddleY;
    private int              mPlayerTwoPaddleY;
    private Rectangle2D      mPlayerOnePaddle;
    private Rectangle2D      mPlayerTwoPaddle;
    private Ellipse2D        mBall;
    private Timer            mTimer        = new Timer(5, this);
    private boolean          mStartGame    = true;
    private int              mPanelHeight;
    private int              mPanelWidth;
    private int              mBallPosX;
    private int              mBallPosY;
    private int              mScoreLeft    = 0, mScoreRight = 0;
    private DatagramSocket   mConnectionSocket;
    private int              mWhichPlayer;

    public PongPanel(DatagramSocket connectionSocket) {
        this.mConnectionSocket = connectionSocket;
        System.out.println("PongPanelCreated");
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(true);
        addKeyListener(this);
        mTimer.setInitialDelay(1000);
        mTimer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.ORANGE);
        g2d.drawLine(0, SCORE_HEIGHT, mPanelWidth, SCORE_HEIGHT);
        mPanelHeight = getHeight();
        mPanelWidth = getWidth();
        if (mStartGame) {
            mPlayerOnePaddleY = (getHeight() / 2) - PADDLE_HEIGHT / 2;
            mPlayerTwoPaddleY = mPlayerOnePaddleY;
            mBallPosX = mPanelWidth / 2;
            mBallPosY = mPanelHeight / 2;
            mStartGame = false;
        }

        mPlayerOnePaddle = new Rectangle(MARGIN, mPlayerOnePaddleY,
                PADDLE_WIDTH, PADDLE_HEIGHT);
        g2d.fill(mPlayerOnePaddle);

        mPlayerTwoPaddle = new Rectangle(mPanelWidth - PADDLE_WIDTH - MARGIN,
                mPlayerTwoPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        g2d.fill(mPlayerTwoPaddle);

        mBall = new Ellipse2D.Double(mBallPosX, mBallPosY, 10, 10);
        g2d.fill(mBall);
        String score = "Points:" + mScoreLeft + ":" + mScoreRight;
        g2d.drawString(score, (int) getBounds().getCenterX(), 10);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        String command;
        try {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    if (mWhichPlayer == 1) {
                        if (!(mPlayerOnePaddleY >= mPanelHeight - PADDLE_HEIGHT
                                - MARGIN)) {
                            mPlayerOnePaddleY = mPlayerOnePaddleY + STEP;
                            command = "p1:" + mPlayerOnePaddleY;
                            mConnectionSocket.send(new DatagramPacket(command
                                    .getBytes(), command.length(),
                                    mConnectionSocket.getInetAddress(),
                                    mConnectionSocket.getPort()));
                        }
                    } else {
                        if (!(mPlayerTwoPaddleY >= mPanelHeight - PADDLE_HEIGHT
                                - MARGIN)) {
                            mPlayerTwoPaddleY = mPlayerTwoPaddleY + STEP;
                            command = "p2:" + mPlayerTwoPaddleY;
                            mConnectionSocket.send(new DatagramPacket(command
                                    .getBytes(), command.length(),
                                    mConnectionSocket.getInetAddress(),
                                    mConnectionSocket.getPort()));
                        }
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (mWhichPlayer == 1) {
                        if (!(mPlayerOnePaddleY <= MARGIN + SCORE_HEIGHT)) {
                            mPlayerOnePaddleY = mPlayerOnePaddleY - STEP;
                            command = "p1:" + mPlayerOnePaddleY;
                            mConnectionSocket.send(new DatagramPacket(command
                                    .getBytes(), command.length(),
                                    mConnectionSocket.getInetAddress(),
                                    mConnectionSocket.getPort()));
                        }
                    } else {
                        if (!(mPlayerTwoPaddleY <= MARGIN + SCORE_HEIGHT)) {
                            mPlayerTwoPaddleY = mPlayerTwoPaddleY - STEP;
                            command = "p2:" + mPlayerTwoPaddleY;

                            mConnectionSocket.send(new DatagramPacket(command
                                    .getBytes(), command.length(),
                                    mConnectionSocket.getInetAddress(),
                                    mConnectionSocket.getPort()));
                        }
                        break;
                    }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void run() {
        String received;
        DatagramPacket receivedPacket = new DatagramPacket(new byte[256], 256);
        try {
            while (true) {
                mConnectionSocket.receive(receivedPacket);
                received = new String(receivedPacket.getData(),
                        receivedPacket.getOffset(), receivedPacket.getLength());
                System.out.println(received);
                String[] receivedSplit = received.split(":");
                switch (receivedSplit[0]) {
                    case "BALL":
                        mBallPosX = Integer.parseInt(receivedSplit[1]);
                        mBallPosY = Integer.parseInt(receivedSplit[2]);
                        break;
                    case "p1-paddle":
                        mPlayerTwoPaddleY = Integer.parseInt(receivedSplit[1]);
                        break;
                    case "p2-paddle":
                        mPlayerOnePaddleY = Integer.parseInt(receivedSplit[1]);
                        break;
                    case "player1":
                        mWhichPlayer = 1;
                        break;
                    case "player2":
                        mWhichPlayer = 2;
                        break;
                    case "points":
                        mScoreLeft = Integer.parseInt(receivedSplit[1]);
                        mScoreRight = Integer.parseInt(receivedSplit[2]);
                        break;
                    case "p1-disconnected":
                        JOptionPane.showMessageDialog(null,
                                "player 1 disconnected", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    case "p2-disconnected":
                        JOptionPane.showMessageDialog(null,
                                "player 2 disconnected", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                }
            }
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        } finally {
            JOptionPane.showMessageDialog(null, "Connection Lost", "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }

    }
}

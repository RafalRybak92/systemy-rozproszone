import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class PongGame extends JFrame {
    private DatagramSocket mConnectionSocket;

    public PongGame(DatagramSocket socket) {
        this.mConnectionSocket = socket;
        setTitle("Pong Game");
        setLocation(getScreen().width / 4, getScreen().height / 4);
        setVisible(true);
        setResizable(false);
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        PongPanel pong;
        pong = new PongPanel(mConnectionSocket);
        add(pong);
        new Thread(pong).start();
        System.out.println("Thread started");
    }

    private Dimension getScreen() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PongGame extends JFrame {
    private Socket mConnectionSocket;

    public PongGame(Socket connection) {
        this.mConnectionSocket = connection;
        setTitle("Pong Game");
        setLocation(getScreen().width / 4, getScreen().height / 4);
        setVisible(true);
        setResizable(false);
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        PongPanel pong;
        try {
            pong = new PongPanel(mConnectionSocket);
            add(pong);
            new Thread(pong).start();
            System.out.println("Thread started");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Dimension getScreen() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}

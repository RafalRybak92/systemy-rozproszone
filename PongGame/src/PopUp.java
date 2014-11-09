import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class PopUp extends JFrame implements ActionListener {
    private JButton            mConnect;
    private JButton            mCancel;
    private JTextField         mIp;
    private JTextField         mPort;
    private JLabel             mInfo;
    private String             mIpAddress;
    private int                mPortAddress;
    private ObjectInputStream  mIn;
    private ObjectOutputStream mOut;
    private Socket             mConnection;

    public PopUp() {
        setTitle("Welcome in two player Tcp PongGame");
        setLocation(getScreen().width / 3, getScreen().height / 3);
        setVisible(true);
        setResizable(false);
        setSize(getScreen().width / 4, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        initInfo();
        initIp();
        initPort();
        initCancel();
        initConnect();
        repaint();
    }

    public void initInfo() {
        mInfo = new JLabel("Where do you want to connect?");
        mInfo.setBounds(60, 0, getWidth(), 30);
        add(mInfo);
    }

    public void initCancel() {
        mCancel = new JButton("Cancel");
        mCancel.setBounds(0, 90, getWidth(), 30);
        mCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });
        add(mCancel);
    }

    public void initConnect() {
        mConnect = new JButton("Connect");
        mConnect.setBounds(0, 60, getWidth(), 30);
        mConnect.addActionListener(this);
        add(mConnect);
    }

    public void initIp() {
        mIp = new JTextField();
        mIp.setBounds(0, 31, getWidth() - 100, 30);
        mIp.setFocusable(true);
        mIp.setText("localhost");
        add(mIp);
    }

    public void initPort() {
        Rectangle mIpRect = mIp.getBounds();
        mPort = new JTextField();
        mPort.setBounds(mIpRect.width, 31, getWidth(), 30);
        mPort.setFocusable(true);
        mPort.setText("2222");
        add(mPort);
    }

    private Dimension getScreen() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public static void main(String[] args) {
        try {
            EventQueue.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    PopUp gameEvent = new PopUp();

                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Connect")) {
            mIpAddress = mIp.getText();
            try {
                mPortAddress = Integer.parseInt(mPort.getText());
            } catch (NumberFormatException nfe) {
                mPortAddress = -1;
                JOptionPane.showMessageDialog(null, "WRONG PORT FORMAT",
                        "ERROR", JOptionPane.ERROR_MESSAGE);
            }
            if (mPortAddress != -1) {
                dispose();
                try {
                    mConnection = new Socket(mIpAddress, mPortAddress);
                    mConnection.setTcpNoDelay(true);
                    new PongGame(mConnection);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(),
                            "ERROR", JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }
}

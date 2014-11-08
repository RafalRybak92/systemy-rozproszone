package com.pong.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Roophie on 2014-11-03.
 */
public class PongClient extends Thread {

    protected static final int SERVER_SOCKET = 2222;
    protected static final int MAX_BUFF = 1024;
    private byte[] reciveBuffer = new byte[MAX_BUFF];
    private byte[] sendBuffer = new byte[MAX_BUFF];
    private BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));
    private DatagramPacket recivePacket;
    private DatagramPacket sendPacket;
    private DatagramSocket clientSocket;
    private InetAddress inetAddress;
    private int port = 1234;



    public PongClient(int SOCKET) throws SocketException {
        clientSocket = new DatagramSocket(SOCKET);
    }


    public void run() {
        boolean firstConnect = true;
        while (true) {
            try {
                if(firstConnect){
                    inetAddress = InetAddress.getLocalHost();
                    send("QUdhgavvsjjhjkhk");
                    firstConnect = false;
                }
                else {
                    inetAddress = InetAddress.getLocalHost();
                    String sendedMessage = inputStream.readLine();
                    send(sendedMessage);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }

    private void recive() throws IOException {
        recivePacket = new DatagramPacket(reciveBuffer, reciveBuffer.length);
        clientSocket.receive(recivePacket);
        String receivedMessage;
        receivedMessage = new String(recivePacket.getData(), recivePacket.getOffset(), recivePacket.getLength());
        System.out.println(receivedMessage);


    }

    public void send(String MESSAGE) throws IOException {
        sendBuffer = MESSAGE.getBytes();
        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, inetAddress, SERVER_SOCKET);
        clientSocket.send(sendPacket);
    }


    public static void main(String[] args) throws IOException {
        PongClient pC = new PongClient(1344);
        pC.start();
        while(true){
            pC.recive();
        }

    }
}

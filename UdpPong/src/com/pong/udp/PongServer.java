package com.pong.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.lang.String;import java.lang.System;import java.lang.Thread;import java.net.DatagramPacket;


/**
 * Created by Roophie on 2014-11-03.
 */
public class PongServer extends Thread {

    protected static final int SOCKET = 2222;
    protected static final int MAX_BUFF = 1024;
    private byte[] reciveBuffer = new byte[MAX_BUFF];
    private byte[] sendBuffer = new byte[MAX_BUFF];
    private DatagramPacket recivePacket;
    private DatagramPacket sendPacket;
    private DatagramSocket serverSocket;
    private InetAddress inetAddress;
    private int port;
    private ArrayList<InetAddress> currentAdresses = new ArrayList<InetAddress>();
    private ArrayList<Integer> curetnPorts = new ArrayList<Integer>();





    public PongServer(int SOCKET) throws SocketException {

        serverSocket = new DatagramSocket(SOCKET);
        System.err.println("It's ALIVE");

    }


    public void run() {
        while(true) {
            try {


                recivePacket = new DatagramPacket(reciveBuffer, reciveBuffer.length);
                serverSocket.receive(recivePacket);


                String MESSAGE = new String(recivePacket.getData(), recivePacket.getOffset(), recivePacket.getLength());

                if(MESSAGE.startsWith("QUdhgavvsjjhjkhk")) {
                    System.err.println("dodano");
                    currentAdresses.add(recivePacket.getAddress());
                    curetnPorts.add(recivePacket.getPort());
                }
                else{
                    sendToAll(MESSAGE);
                }
                System.out.println(MESSAGE + " This is what i've got ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void sendToAll(String MESSAGE) throws IOException, ArrayIndexOutOfBoundsException {
        String msgToAll = MESSAGE + " From: " + recivePacket.getAddress().toString()+ " : " + recivePacket.getPort();
        for(int i = 0; i < curetnPorts.size(); ++i) {
            sendBuffer = msgToAll.getBytes();
            inetAddress = currentAdresses.get(i);
            port = curetnPorts.get(i);
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, inetAddress, port);
            serverSocket.send(sendPacket);
        }


    }

    private void recive() throws IOException {

        recivePacket = new DatagramPacket(reciveBuffer, reciveBuffer.length);
        serverSocket.receive(recivePacket);


        String MESSAGE = new String(recivePacket.getData(), recivePacket.getOffset(), recivePacket.getLength());

        if(MESSAGE.startsWith("QUdhgavvsjjhjkhk")){
            System.err.println("dodano");
            currentAdresses.add(recivePacket.getAddress());
            curetnPorts.add(recivePacket.getPort());

        }

        System.out.println(MESSAGE + " This is what i've got ");

    }

    public static void main(String[] args){
        PongServer ps = null;
        try {
            ps = new PongServer(SOCKET);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        ps.start();
    }


}

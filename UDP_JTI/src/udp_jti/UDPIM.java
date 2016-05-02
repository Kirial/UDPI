package udp_jti;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Family
 */
public class UDPIM {

    final private int mSize = 50; // packet size 
    private byte[] receiveD; // bytes that are resived 
    private byte[] send; // bytes that are to be send
    private String connection; // string that is used to connect 
    private int PORT_NR; // port nummer 
    private DatagramSocket socket = null;
    private UDPII target; // interface for sender 
    InetAddress IP;
    int portnr;
    String adr;


    /**
     * This constructor creates a socket that will listen to the port
     * <p>
     * this port is only used to create sessions, all further communications
     * will
     * <p>
     * go through another port.
     *
     * @param yourCode
     * @param p
     * @throws Exception
     */
    public UDPIM(UDPII yourCode, int p) throws Exception {
        PORT_NR = p; // portnr user wnatts to use 
        receiveD = new byte[mSize];
        send = new byte[mSize];
        target = yourCode;

        try {
            socket = new DatagramSocket(PORT_NR);
        } catch (IOException e) {
            System.out.println("Port Is in use, use another port");
        }
    }

    /**
     * 
     */
    public void listen() {

        try {
            DatagramPacket receiveP = new DatagramPacket(receiveD, receiveD.length);
            socket.receive(receiveP);
            connection = new String(receiveP.getData());
            if (connection.subSequence(0, 12).equals("Transmission")) {

                DatagramSocket newSocket;
                int newPort = 49152;
                while (true) {
                    try {
                        newSocket = new DatagramSocket(newPort);
                        InetAddress IPAddress = receiveP.getAddress();
                        int port = receiveP.getPort();

                        String Response = "ConOK." + newPort + "*";
                        send = Response.getBytes();
                        DatagramPacket conOK = new DatagramPacket(send, send.length, IPAddress, port);
                        socket.send(conOK);
                        try {
                            new Thread(new serverStart(newSocket, target, IPAddress, mSize)).start();
                        } catch (Exception ex) {
                            System.out.println("Can't create new port");
                        }

                        break;
                    } catch (SocketException e) {
                        newPort++;
                        if (newPort >= 65535) {
                            System.out.println("Error");
                            break;
                        }
                    }
                }

            }

        } catch (IOException ex) {
            System.out.println("ERROR" + Arrays.toString(ex.getStackTrace()));
        }

    }


    public void close() {
        socket.close();
    }
}

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
public class UDPIModtager {

    final private int mSize = 50; // packet size 
    int portnr; // new port nummer for connection 
    private int timeout; // time out set by user
    private int PORT_NR; // port nummer 

    private byte[] receiveD; // bytes that are resived 
    private byte[] send; // bytes that are to be send

    private String connection; // string that is used to connect 

    private UDPII target; // interface for sender 
    private InetAddress IP;

    private DatagramSocket socket = null;
    private DatagramPacket receiveP;

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
    public UDPIModtager(UDPII yourCode, int p) throws Exception {
        PORT_NR = p; // portnr user wnatts to use 
        receiveD = new byte[mSize]; // size
        send = new byte[mSize]; // size
        target = yourCode;
        receiveP = new DatagramPacket(receiveD, receiveD.length);
        try {
            socket = new DatagramSocket(PORT_NR);
        } catch (IOException e) {
            System.out.println("Port Is in use, use another port");
        }
    }

    /**
     * Listens to the port that is defined in constructor
     */
    public void listen() {

        try {
            socket.receive(receiveP);
            connection = new String(receiveP.getData());
            if (connection.subSequence(0, 12).equals("Transmission")) {

                DatagramSocket newSocket;
                int newPort = 49152;
                while (true) {
                    try {
                        newSocket = new DatagramSocket(newPort);
                        IP = receiveP.getAddress();
                        int port = receiveP.getPort();

                        String Response = "ConOK." + newPort + "*";
                        send = Response.getBytes();
                        DatagramPacket conOK = new DatagramPacket(send, send.length, IP, port);
                        socket.send(conOK);
                        try {
                            new Thread(new serverStart(newSocket, target, IP, mSize)).start();
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

    /**
     * The method will listen to the socket for t amount of time in milliseconds
     *
     * @param t
     */
    public void listenshort(int t) {
        timeout = t;
        try {
            socket.setSoTimeout(t);
            listen();
        } catch (SocketException ex) {
        }
    }

    public void close() {
        socket.close();
    }
}

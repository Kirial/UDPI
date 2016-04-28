package udp_jti;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Family
 */
public class UDPI {

    private byte[] receiveD;
    private byte[] send;
    private String connection;
    private int PORT_NR;
    //private String IP;
    private DatagramSocket socket = null;
    private String thisMessage;
    private UDPII target;
    private String dataType;
    private static String inData;
    private ArrayList<String> toBeSent;
    private DatagramSocket clientSocket;
    private String sendData;
    private byte[] sendDataBytes;
    InetAddress IP;
    int portnr;
    String adr;

    /**
     * This constructor creates a port that will listen to the port
     * <p>
     * this port is only used to create sessions, all further communications
     * will
     * <p>
     * go through the next port.
     *
     * @param yourCode
     * @param p
     * @throws Exception
     */
    public UDPI(UDPII yourCode, int p) throws Exception {
        PORT_NR = p;
        if (PORT_NR < 49152 && PORT_NR > 65535) {
            PORT_NR = 49152;
        }
        target = yourCode;
        while (true) {
            try {
                socket = new DatagramSocket(PORT_NR);
                break;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                PORT_NR++;
                System.out.println("Port is in use, i'll try using port: " + PORT_NR);
                if (PORT_NR >= 65535) {
                    throw new Exception("No Free PORT Found ");
                }
            }
        }
    }

    public UDPI() throws Exception {
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            throw new Exception("Error");
        }
    }

    public void send(int portnr, String adr) { // , String data, String dataType
        int newPort;
        String connect = "Transmission.";
        connect = connect + dataType;

        byte[] dataB = connect.getBytes();
        try {
            InetAddress address = InetAddress.getByName(adr);
            DatagramPacket conOK = new DatagramPacket(dataB, dataB.length, address, portnr);
            try {
                socket.send(conOK);
                byte[] tempData = new byte[60];
                DatagramPacket receivePacket = new DatagramPacket(tempData, tempData.length);
                socket.receive(receivePacket);
                String con = new String(receivePacket.getData());
                String newPortS = con.substring(con.indexOf('.'), con.length());
                newPort = Integer.parseInt(newPortS);
                Scanner keyboard = new Scanner(System.in);
                inData = keyboard.nextLine();
                ArrayList<String> toBeSent = new ArrayList<String>();
                buffer();
                sendOrder();

            } catch (IOException ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }

        } catch (UnknownHostException ex) {
            System.out.println("Invalid IP Adress");
        }
    }

    public void listen() {

        try {
            DatagramPacket receiveP = new DatagramPacket(receiveD, receiveD.length);
            socket.receive(receiveP);
            connection = new String(receiveP.getData());

            String subS = connection.substring(0, 11);
            dataType = connection.substring(12, connection.indexOf('.'));
            if (subS.equals("Transmission")) {
                DatagramSocket newSocket;
                int newPort = PORT_NR + 1;
                while (true) {

                    try {
                        newSocket = new DatagramSocket(newPort);
                        InetAddress IPAddress = receiveP.getAddress();
                        int port = receiveP.getPort();
                        String Response = "ConOK." + newPort;
                        byte[] sendData = Response.getBytes();
                        DatagramPacket conOK = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        new Thread(new serverStart(newSocket, target, IPAddress, dataType)).start();
                        break;
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
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
     * This method will devide the inData into chunks of 512 bytes
     */
    private void buffer() {
        while (inData.length() > 512) {
            toBeSent.add(inData.substring(0, 511));
            inData = inData.substring(512);
        }
        toBeSent.add(inData);
    }

    private void sendOrder() throws UnknownHostException, IOException {
        int runde = 0;
        for (int i = 0; i < 100; i++) {

            sendData = toBeSent.get(i+runde);
            sendDataBytes = sendData.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, InetAddress.getByName(adr), portnr);
            clientSocket.send(sendPacket);
            if (i == 99) {
                runde = runde +100;
            }
        }
        socket.setSoTimeout(20);

    }
}



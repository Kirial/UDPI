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
    private boolean listen;

    private ArrayList<String> missingPackets;
    int runde = 0;


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
        receiveD = new byte[512];
        send = new byte[512];
        if (PORT_NR < 49152 || PORT_NR > 65535) {
            PORT_NR = 49152;
            System.out.println("portError i'll try using port " + PORT_NR);
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
        receiveD = new byte[512];
        send = new byte[512];
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

                buffer();
                sendBurst();
                waitAck();

            } catch (IOException ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }

        } catch (UnknownHostException ex) {
            System.out.println("Invalid IP Adress");
        }
    }

    public void listen() {
        listen = true;
        while (listen == true) {
            try {
                DatagramPacket receiveP = new DatagramPacket(receiveD, receiveD.length);
                socket.receive(receiveP);
                connection = new String(receiveP.getData());
                if (connection.length() > 11) {
                    String subS = connection.substring(0, 12);
                    if (subS.equals("Transmission") && connection.indexOf('.') != -1) {
                        dataType = connection.substring(12, connection.indexOf('.'));

                        DatagramSocket newSocket;
                        int newPort = PORT_NR + 1;
                        while (true) {
                            try {
                                newSocket = new DatagramSocket(newPort);
                                InetAddress IPAddress = receiveP.getAddress();
                                int port = receiveP.getPort();

                                String Response = "ConOK." + newPort;
                                send = Response.getBytes();
                                DatagramPacket conOK = new DatagramPacket(send, send.length, IPAddress, port);
                                socket.send(conOK);
                                try {
                                    new Thread(new serverStart(newSocket, target, IPAddress, dataType)).start();
                                } catch (Exception ex) {
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

                }

            } catch (IOException ex) {
                System.out.println("ERROR" + Arrays.toString(ex.getStackTrace()));
            }
        }
    }

    public void stopListen() {
        listen = false;
    }

    /**
     * This method will devide the inData into chunks of 512 bytes
     */

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
    /**
     * This method will divide the inData into chunks of 512 bytes
     */
    private void buffer(){
        while (inData.length() > 487) {
            toBeSent.add(inData.substring(0, 486));
            inData = inData.substring(487);
        }
        toBeSent.add(inData);
    }

    private void sendBurst() throws UnknownHostException, IOException {
        int count = 0;
        while (count < 100 && count + runde < toBeSent.size()) {
            int antal = inData.length() % 487;
            int flere = toBeSent.size() - (runde);

            int S = 1;
            if (flere > 0) {
                S = 1;
            } else {
                S = 0;
            }

            int nummer = 0;
            String header = ("HEAD*A" + antal + "#" + nummer + "S" + S + "*HEAD");
            String data = toBeSent.get(count + runde);
            String headerMedData = header + data;
            nummer++;
            sendData = headerMedData;
            sendDataBytes = sendData.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, InetAddress.getByName(adr), portnr);
            clientSocket.send(sendPacket);
            count++;
        }
    }

    /**
     * Metode til at sende pakker enkeltvis hvis de bliver angivet i en
     * acknowledgement
     *
     * @param missing
     * @throws UnknownHostException
     * @throws IOException
     */
    private void sendSingle(int missing) throws UnknownHostException, IOException {

        sendData = toBeSent.get(missing);
        sendDataBytes = sendData.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, InetAddress.getByName(adr), portnr);
        clientSocket.send(sendPacket);
        waitAck();
    }

    private void waitAck() {

        DatagramPacket receivePacket = new DatagramPacket(receiveD, receiveD.length);
        try {
            clientSocket.setSoTimeout(20);
            clientSocket.receive(receivePacket);
            String Ack = new String(receivePacket.getData());
            ProcessAck(Ack);
            clientSocket.close();

        } catch (SocketTimeoutException timeout) {

        } catch (IOException ex) {
            Logger.getLogger(UDPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ProcessAck(String Ack) {

        String modtagetOK = Ack.substring(Ack.indexOf('K') + 1, Ack.indexOf('N'));
        String missingstring = Ack.substring(Ack.indexOf('t'));
        int missing = Integer.parseInt(missingstring);

        if (missing == 99) {
            runde = runde + 100;
            try {
                sendBurst();
            } catch (IOException ex) {
                Logger.getLogger(UDPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                sendSingle(missing);
            } catch (IOException ex) {
                Logger.getLogger(UDPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}



package udp_jti;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
public class UDPI {

    final private int mSize = 50; // packet size 
    private byte[] receiveD; // bytes that are resived 
    private byte[] send; // bytes that are to be send
    private String connection; // string that is used to connect 
    private int PORT_NR; // port nummer 
    private DatagramSocket socket = null;
    private UDPII target; // interface for sender 
    private ArrayList<ArrayList<String>> toBeSend;
    InetAddress IP;
    int portnr;
    String adr;
    private boolean listen;
    private int runde;
    private boolean nextBurst;

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
    public UDPI(UDPII yourCode, int p) throws Exception {
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

    public UDPI() throws Exception {
        nextBurst = false;

        receiveD = new byte[mSize];
        send = new byte[mSize];
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            throw new Exception("Error");
        }
    }

    public void send(int portnr, String adr, String m) {
        int newPort = 0;
        toBeSend = new ArrayList<>();
        buffer(m); // get the string divided into small chunks of data 
        String connect = "Transmission"; //head
        send = connect.getBytes();
        try {
            InetAddress address = InetAddress.getByName(adr);
            DatagramPacket conOK = new DatagramPacket(send, send.length, address, portnr);
            try {

                socket.send(conOK);
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveD, receiveD.length);
                    socket.setSoTimeout(1000);
                    socket.receive(receivePacket);
                    String con = new String(receivePacket.getData());

                    System.out.println(con);
                    if (con.substring(0, con.indexOf('.')).equals("ConOK")) {
                        String newPortS = con.substring(con.indexOf('.') + 1, con.indexOf('*'));
                        newPort = Integer.parseInt(newPortS);
                        System.out.println(newPort);

                    } else {
                        System.out.println("ERROR Connection");
                        return;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("timeout");
                    return;
                }

                runde = 0;
                for (ArrayList<String> s : toBeSend) {
                    sendBurst(newPort, address);
                    runde++;
                }

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

    public void stopListen() {
        listen = false;
    }

    public void close() {
        socket.close();
    }

    /**
     * This method will divide the inData into chunks of 512 bytes
     */
    private void buffer(String m) {
        int count = 1;
        ArrayList<String> tempArray = new ArrayList<>();
        toBeSend.add(tempArray);
        int index = 0;
        while (m.length() > (mSize - 20)) {
            toBeSend.get(index).add(m.substring(0, (mSize - 20)));
            m = m.substring((mSize - 20));
            if (count == 100) {
                toBeSend.add(tempArray);
                index++;
            }
            count++;

        }
        toBeSend.get(index).add(m);
    }

    private void sendBurst(int port, InetAddress thisAdr) {
        ArrayList<String> tempArray = toBeSend.get(runde);

        for (int i = 1; i <= tempArray.size(); i++) {
            int S;
            if ((runde + 1) == toBeSend.size()) {
                S = 0;
            } else {
                S = 1;
            }
            
            String header = ("HEAD*A" + tempArray.size() + "#" + i + "S" + S + "*HEAD");
            String headerMedData = header + tempArray.get(i-1);
            
            send = headerMedData.getBytes();
            System.out.println(Arrays.toString(send));
            DatagramPacket sendPacket;
            try {
                sendPacket = new DatagramPacket(send, send.length, thisAdr, port);
                socket.send(sendPacket);
            } catch (Exception ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }

        }

        waitAck(port, thisAdr);

    }

    /**
     * Metode til at sende pakker enkeltvis hvis de bliver angivet i en
     * acknowledgement
     *
     * @param missing
     * @throws UnknownHostException
     * @throws IOException
     */
    private void sendSingle(int missing, int newPort, InetAddress address) {
        int S;
        if ((runde + 1) == toBeSend.size()) {
            S = 0;
        } else {
            S = 1;
        }

        String header = "HEAD*A" + toBeSend.get(runde).size() + "#" + missing + "S" + S + "*HEAD";
        System.out.println(header+toBeSend.get(runde).get(missing - 1));
        send = (header + toBeSend.get(runde).get(missing - 1)).getBytes();
        try {

            DatagramPacket sendPacket = new DatagramPacket(send, send.length, address, newPort);
            socket.send(sendPacket);
        } catch (IOException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()) + "Server not found");
        }
        waitAck(newPort, address);
    }

    private void waitAck(int newPort, InetAddress address) {

        DatagramPacket receivePacket = new DatagramPacket(receiveD, receiveD.length);
        try {
            socket.setSoTimeout(200);
            socket.receive(receivePacket);
            String Ack = new String(receivePacket.getData());
            System.out.println(Ack);
            ProcessAck(Ack, newPort, address);

        } catch (SocketTimeoutException timeout) {
            sendSingle(toBeSend.get(runde).size(), newPort, address);
        } catch (IOException ex) {

        }

    }

    private void ProcessAck(String Ack, int newPort, InetAddress address) {
        int missing = 0;
        String missingstring = Ack.substring(Ack.indexOf('t')+1, Ack.indexOf('*'));
        try {
            missing = Integer.parseInt(missingstring);
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
        if (missing == 101) {
            return;
        } else {
            sendSingle(missing, newPort, address);
        }
    }
}

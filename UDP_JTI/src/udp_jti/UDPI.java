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

    final private int mSize = 512;
    private byte[] receiveD;
    private byte[] send;
    private String connection;
    private int PORT_NR;
    private DatagramSocket socket = null;
    private UDPII target;
    private String dataType;
    private ArrayList<String> toBeSend;
    private DatagramSocket clientSocket;
    private byte[] sendDataBytes;
    InetAddress IP;
    int portnr;
    String adr;
    private boolean listen;
    private int runde;
    private boolean nextBurst;

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
        receiveD = new byte[mSize];
        send = new byte[mSize];
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

    public void send(int portnr, String adr, String m) { // , String data, String dataType
        int newPort = 0;
        String connect = "Transmission.";
        connect = connect + dataType;

        send = connect.getBytes();
        try {
            InetAddress address = InetAddress.getByName(adr);
            DatagramPacket conOK = new DatagramPacket(send, send.length, address, portnr);
            try {
                int countTry = 20;
                while (countTry < 300) {
                    socket.send(conOK);
                    try {
                        DatagramPacket receivePacket = new DatagramPacket(receiveD, receiveD.length);
                        socket.setSoTimeout(20);
                        socket.receive(receivePacket);

                        String con = new String(receivePacket.getData());
                        String newPortS = con.substring(con.indexOf('.'), con.length());
                        newPort = Integer.parseInt(newPortS);
                    } catch (SocketTimeoutException e) {
                        countTry = countTry + 40;
                    }
                }
                runde = 0;
                toBeSend = new ArrayList<>();
                buffer(m);
                int rounds = toBeSend.size()/100+1;
                while (runde < rounds) {
                    sendBurst(newPort, address);
                    waitAck(newPort, address);
                }

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
                                    new Thread(new serverStart(newSocket, target, IPAddress, dataType,mSize)).start();
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
     * This method will divide the inData into chunks of 512 bytes
     */
    private void buffer(String m) {
        while (m.length() > 487) {
            toBeSend.add(m.substring(0, 486));
            m = m.substring(487);
        }
        toBeSend.add(m);
    }

    private void sendBurst(int port, InetAddress thisAdr) {
        int count = 0;
        while (count < 100 && toBeSend.size() > (runde * 100 + count)) {
            int flere = toBeSend.size() - (runde * 100);

            int S;
            if (flere > 0) {
                S = 1;
            } else {
                S = 0;
            }
            int antal = toBeSend.size() - (runde * 100);
            if (antal >= 100) {
                antal = 100;
            }
            int nummer = count + 1;
            String header = ("HEAD*A" + antal + "#" + nummer + "S" + S + "*HEAD");
            String data = toBeSend.get(count + (runde * 100));
            String headerMedData = header + data;
            sendDataBytes = headerMedData.getBytes();
            DatagramPacket sendPacket;
            try {
                sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, thisAdr, port);
                socket.send(sendPacket);
            } catch (Exception ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }
            count++;
            if (count == 100) {
                count = 0;
                runde++;
            }
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
    private void sendSingle(int missing,int newPort,InetAddress address) {

        sendDataBytes = toBeSend.get(missing+runde*100).getBytes();
        try {
            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length,address ,newPort );
            socket.send(sendPacket);
        } catch (IOException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
        waitAck(newPort, address);
    }

    private void waitAck(int newPort, InetAddress address) {
        while (!nextBurst) {
            DatagramPacket receivePacket = new DatagramPacket(receiveD, receiveD.length);
            try {
                clientSocket.setSoTimeout(20);
                clientSocket.receive(receivePacket);
                String Ack = new String(receivePacket.getData());
                ProcessAck(Ack,newPort, address);
                clientSocket.close();

            } catch (SocketTimeoutException timeout) {

            } catch (IOException ex) {
                sendSingle(100 * runde + 100,newPort, address);
            }
        }
    }

    private void ProcessAck(String Ack,int newPort, InetAddress address) {
        int missing = 0;
        String missingstring = Ack.substring(Ack.indexOf('t'));
        try {
            missing = Integer.parseInt(missingstring);
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
        if (missing == 101) {
            runde++;
        } else {
            sendSingle(missing, newPort, address);
        }
    }
}

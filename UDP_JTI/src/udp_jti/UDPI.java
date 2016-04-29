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
    private ArrayList<String> toBeSend;
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
                //System.out.println(e.getMessage());
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

    public void send(int portnr, String adr, String m) {
        int newPort = 0;
        String connect = "Transmission";        
        send = connect.getBytes();
        try {
            InetAddress address = InetAddress.getByName(adr);
            DatagramPacket conOK = new DatagramPacket(send, send.length, address, portnr);
            try {
                int countTry = 40;
                while (countTry < 200) {
                    socket.send(conOK);
                    try {
                        DatagramPacket receivePacket = new DatagramPacket(receiveD, receiveD.length);
                        socket.setSoTimeout(countTry);
                        socket.receive(receivePacket);
                        String con = new String(receivePacket.getData());
                        System.out.println(con);
                        if (con.substring(0, con.indexOf('.')).equals("ConOK")) {
                            String newPortS = con.substring(con.indexOf('.'), con.length());
                            newPort = Integer.parseInt(newPortS);
                        } else {
                            System.out.println("ERROR Connection");
                            return;
                        }
                    } catch (SocketTimeoutException e) {
                        countTry = countTry + 40;
                        System.out.println("timeout");
                    }
                }
                if (countTry >= 200) {
                    System.out.println("Timeout Error Host Not Responding");
                    return;
                }
                runde = 0;
                toBeSend = new ArrayList<>();
                buffer(m);
                int rounds = toBeSend.size() / 101 + 1;
                while (runde < rounds) {
                    sendBurst(newPort, address);
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
                if (connection.equals("Transmission")) {

                    DatagramSocket newSocket;
                    int newPort =49152;
                    System.out.println("HEJ");
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
            send = headerMedData.getBytes();
            DatagramPacket sendPacket;
            try {
                sendPacket = new DatagramPacket(send, send.length, thisAdr, port);
                socket.send(sendPacket);
            } catch (Exception ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }
            count++;
            if (count == 100) {
                count = 0;
                waitAck(port, thisAdr);
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
    private void sendSingle(int missing, int newPort, InetAddress address) {

        send = toBeSend.get((missing + runde * 100)).getBytes();
        try {
            DatagramPacket sendPacket = new DatagramPacket(send, send.length, address, newPort);
            socket.send(sendPacket);
        } catch (IOException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()) + "Server not found");
        }
        waitAck(newPort, address);
    }

    private void waitAck(int newPort, InetAddress address) {
        nextBurst = false;
        while (!nextBurst) {
            DatagramPacket receivePacket = new DatagramPacket(receiveD, receiveD.length);
            try {
                socket.setSoTimeout(200);
                socket.receive(receivePacket);
                String Ack = new String(receivePacket.getData());
                ProcessAck(Ack, newPort, address);
                socket.close();

            } catch (SocketTimeoutException timeout) {
                sendSingle(100 * runde + 100, newPort, address);
            } catch (IOException ex) {

            }
        }
    }

    private void ProcessAck(String Ack, int newPort, InetAddress address) {
        int missing = 0;
        String missingstring = Ack.substring(Ack.indexOf('t'));
        try {
            missing = Integer.parseInt(missingstring);
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
        if (missing == 101) {
            runde++;
            nextBurst = true;
        } else {
            sendSingle(missing, newPort, address);
        }
    }
}

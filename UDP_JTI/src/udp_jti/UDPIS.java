/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_jti;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author taras
 */
public class UDPIS {

    final private int mSize = 50; // packet size 
    private byte[] receiveD; // bytes that are resived 
    private byte[] send; // bytes that are to be send
    private int runde;
    private ArrayList<ArrayList<String>> toBeSend;
    private DatagramSocket socket = null;

    public UDPIS() throws Exception {

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

    private void buffer(String m) {
        int count = 1;
        toBeSend.add(new ArrayList<>());
        int index = 0;

        while (m.length() > (mSize - 20)) {
            toBeSend.get(index).add(m.substring(0, (mSize - 20)));
            m = m.substring((mSize - 20));
            if (count == 10) {
                toBeSend.add(new ArrayList<>());
                index++;
                count = 0;
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
            String headerMedData = header + tempArray.get(i - 1);

            send = headerMedData.getBytes();
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

        String header = "HEAD*A" + (toBeSend.get(runde)).size() + "#" + missing + "S" + S + "*HEAD";
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
        String missingstring = Ack.substring(Ack.indexOf('t') + 1, Ack.indexOf('*'));
        try {
            missing = Integer.parseInt(missingstring);
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
        if (missing != 101) {
            sendSingle(missing, newPort, address);
        }
    }

}

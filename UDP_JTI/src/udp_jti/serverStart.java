package udp_jti;

import java.net.*;
import java.util.*;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author: Taras Karpin S153067, Jesper Kirial S153300 Date: 26-04-2016 Purpose:
 * To secure integrity of the data
 */
class serverStart implements Runnable {

    private ArrayList<String[]> packet; // ArrayList Of String that well be returned to the caller
    private DatagramSocket socket; // socket that is send to and from 
    private byte[] receiveD; // data from sender 
    private byte[] sendD; // data to send 
    private String message; // temp message 
    private UDPII target; // who calls the function so we can send message back

    private DatagramPacket receivePacket;
    private String antalS;
    private int antal;
    private String indexS;
    private int index;
    private String sessionStatus;
    private boolean modtaget[];
    private boolean continueSession = false;
    private boolean firstPacket;
    private int missing;
    private InetAddress sender;
    private int port;
    boolean allRes;

    /**
     * Constructor takes as the input datagram socet Target where it will return
     * message ip addres of the one who created connection Data type that is to
     * send
     *
     */
    public serverStart(DatagramSocket s, UDPII t, InetAddress i, int dataSize) throws Exception {
        packet = new ArrayList<>();
        receiveD = new byte[dataSize]; // maxsize to be received 
        sendD = new byte[dataSize]; // maxsize to be send 
        target = t; // target of the caller
        socket = s; // port that we send from  
        sender = i;
        receivePacket = new DatagramPacket(receiveD, receiveD.length); //receive packet objekt  
        continueSession = true; // the continue Session will stop after all packets are received. 
    }

    @Override
    public void run() {

        int indexArray = 0;
        while (continueSession){
            firstPacket = true; // wait for the first packet is received. 
            try {
                //If the first message never arives the connection is closed after 1 second. 
                try {
                    socket.setSoTimeout(5000);// sets time out to one second. 
                    socket.receive(receivePacket); // get first Packet
                    port = receivePacket.getPort();
                } catch (SocketTimeoutException timeout) {
                    socket.close(); // timeout socet closes.
                    System.out.println("ERROR" + Arrays.toString(timeout.getStackTrace()));
                    return;
                }

                if (receivePacket.getAddress().equals(sender)) { // check ip of the sender ignore rest

                    message = new String(receivePacket.getData()); // get message (string)
                    clearD(receiveD);
                    getData();// separate data 
                    packet.add(new String[antal + 1]);
                    modtaget = getField(antal + 1); // creates a array of boollean variables 
                    marck(indexArray); // add to list after

                    firstPacket = false;
                    allRes = false;

                    while (!allRes) {
                        try {
                            socket.setSoTimeout(50 * (antal - index));
                            socket.receive(receivePacket); // receive data  
                            if (receivePacket.getAddress().equals(sender)) {
                                message = new String(receivePacket.getData());
                                clearD(receiveD);
                                getData(); // sepparate data 
                                marck(indexArray);
                            }
                        } catch (SocketTimeoutException timeout) {
                            index = antal;
                        }

                        if (index == antal) {
                            missing = getMissing(); // get number of packet missíng 

                            while (missing != 101) {
                                int ok = missing - 1;
                                String thisAk = "AK" + ok + "Next" + missing + "*";
                                sendD = thisAk.getBytes();
                                DatagramPacket AK = new DatagramPacket(sendD, sendD.length, sender, port);
                                socket.send(AK);
                                try {
                                    socket.setSoTimeout(200);
                                    socket.receive(receivePacket);
                                    if (receivePacket.getAddress().equals(sender)) {
                                        message = new String(receivePacket.getData());
                                        clearD(receiveD);
                                        getData();
                                        marck(indexArray);
                                    }
                                } catch (SocketTimeoutException timeout) {

                                }
                                missing = getMissing(); // get number of packet missíng 
                            }
                            String thisAk = "AK" + 100 + "Next" + 101 + "*";
                            sendD = thisAk.getBytes();
                            DatagramPacket AK = new DatagramPacket(sendD, sendD.length, sender, port);
                            socket.send(AK);
                            allRes = true;
                        }

                    }
                    indexArray++;

                }
            } catch (Exception ex) {
                System.out.print(Arrays.toString(ex.getStackTrace()));
            }
        }

        target.myCode(connectData());
    }

    /**
     * Get Data separated. Header from original Message.
     */
    private void getData() {
        System.out.println(message);
        message = message.replace("HEAD*A", "");
        antalS = message.substring(0, message.indexOf('#'));
        indexS = message.substring(message.indexOf('#') + 1, message.indexOf("S"));
        sessionStatus = message.substring(message.indexOf('S') + 1, message.indexOf("*"));
        if (sessionStatus.equals("0") && firstPacket == true) {
            continueSession = false;
        }
        
        String replaceM = antalS + "#" + indexS + "S" + sessionStatus + "*HEAD";
        message = message.replace(replaceM, "");

        try {
            antal = Integer.parseInt(antalS);
            index = Integer.parseInt(indexS);

        } catch (Exception numbers) {
            System.out.println(numbers.getMessage());
        }

    }

    /**
     * Creates array of antal elements and sets them to false.
     *
     * @param antal
     * @return t
     */
    private boolean[] getField(int antal) {
        boolean[] t = new boolean[antal];
        for (int i = 0; i < antal; i++) {
            t[i] = false;
        }
        return t;
    }

    /**
     * get missing packet
     *
     * @return
     */
    private int getMissing() {
        for (int i = 1; i <= antal; i++) {
            if (!(modtaget[i])) {
                return i;
            }
        }
        return 101;

    }

    /**
     * add data to message.
     */
    private void marck(int i) {
        if (!(modtaget[index])) {
            packet.get(i)[index] = message;
            modtaget[index] = true;
        }
    }

    /**
     * returns arrayList with datatype and data.
     */
    private String connectData() {
        String S = "";

        for (String[] s : packet) {
            for (int i = 1; i<s.length; i++) {
                S = S + s[i];
            }
        }
        return S;
    }

    private void clearD(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

}

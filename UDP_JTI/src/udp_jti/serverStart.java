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

    private ArrayList<String> packet; // ArrayList Of String that well be returned to the caller
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
    private int countSession = 0;
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
    }

    @Override
    public void run() {
        continueSession = true; // the continue Session will stop after all packets are received.  
        missing = 101; // this means no packets are missing 

        while (continueSession) {
            firstPacket = true; // wait for the first packet is received. 
            try {
                //If the first message never arives the connection is closed after 1 second. 
                try {
                    socket.setSoTimeout(5000);// sets time out to one second. 
                    socket.receive(receivePacket); // get first Packet
                } catch (SocketTimeoutException timeout) {
                    socket.close(); // timeout socet closes.
                    System.out.println("ERROR" + Arrays.toString(timeout.getStackTrace()));
                    return;
                }
                if (receivePacket.getAddress().equals(sender)) { // check ip of the sender ignore rest

                    message = new String(receivePacket.getData()); // get message (string)
                    allRes = false;
                    getData();// separate data 
                    String[] SessionsString = new String[antal + 1];
                    modtaget = getField(antal + 1); // creates a array of boollean variables 
                    marck(SessionsString); // add to list after
                    if (antal == index && sessionStatus.equals("0")) {
                        allRes = true;
                        continueSession = false;
                    }
                    firstPacket = false;
                    while (!allRes) {
                        try {
                            socket.setSoTimeout(50 * (antal - index));
                            socket.receive(receivePacket); // receive data  
                            if (receivePacket.getAddress().equals(sender)) {
                                message = new String(receivePacket.getData());
                                getData(); // sepparate data 
                                marck(SessionsString);
                            }
                        } catch (SocketTimeoutException timeout) {
                            index = antal;
                        }
                        if (index == antal) {

                            do {
                                missing = getMissing(); // get number of packet missíng 
                                int ok = missing - 1;
                                String thisAk = "AK" + ok + "Next" + missing+"*";
                                byte[] sendData = thisAk.getBytes();
                                DatagramPacket AK = new DatagramPacket(sendData, sendData.length, sender, port);
                                socket.send(AK);
                                try {
                                    socket.setSoTimeout(200);
                                    socket.receive(receivePacket);
                                    if (receivePacket.getAddress().equals(sender)) {
                                        message = new String(receivePacket.getData());
                                        getData();
                                        marck(SessionsString);
                                    }
                                } catch (SocketTimeoutException timeout) {
                                }

                            } while (missing != 101);
                        }

                    }
                    String temps = "";
                    for (int i = 1; i < SessionsString.length; i++) {
                        temps = temps + SessionsString[i];
                    }
                    packet.add(temps);

                }
            } catch (Exception ex) {
                System.out.print(Arrays.toString(ex.getStackTrace()));
            }
            countSession++;
        }

        target.myCode(connectData());
    }

    /**
     * Get Data separated. Header from original Message.
     */
    private void getData() {
        message = message.replace("HEAD*A", "");
        antalS = message.substring(0, message.indexOf('#'));
        indexS = message.substring(message.indexOf('#') + 1, message.indexOf("S"));
        sessionStatus = message.substring(message.indexOf('S') + 1, message.indexOf("*"));
        System.out.println(message);
        if (sessionStatus.equals("1") && firstPacket == true) {
            continueSession = true;
        } else {
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
        for (int i = 1; i < antal; i++) {
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
    private void marck(String[] s) {
        if (!(modtaget[index])) {
            s[index] = message;
            modtaget[(index)] = true;

        }
    }

    /**
     * returns arrayList with datatype and data.
     */
    private String connectData() {
        String S = "";

        for (String s : packet) {
            S = S + s;
        }
        return S;
    }

}

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
    
    
    private DatagramSocket socket; 
    private byte[] receiveD;
    private byte[] sendD;
    private String message;
    private UDPII target;
    
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
    
    /**
     * Constructor takes as the input datagram socet 
     * Target where it will return message
     * ip addres of the one who created connection
     * Data type that is to send 
     * 
    */
    public serverStart(DatagramSocket s, UDPII t, InetAddress i, String data) {
        
        packet.add(data); // add Data type
        receiveD = new byte[512]; // maxsize to be received 
        sendD = new byte[512]; // maxsize to be send 
        target = t; // target of the caller
        socket = s; // port that we send from  
        receivePacket = new DatagramPacket(receiveD, receiveD.length); //receive packet objekt  

    } 

    @Override
    public void run() {
        
        continueSession = true; // the continue Session will stop after all packets are received.  
        missing = 101; // this means no packets are missing 
        
        while (continueSession) {

            firstPacket = true; // wait for the first packet is received. 
            try {
                socket.receive(receivePacket); // get first Packet
                if (receivePacket.getAddress().equals(sender)) { // check ip of the sender 
                    message = new String(receivePacket.getData()); // get message (string)                     
                    getData();// separate data 
                    if (!(modtaget[index])) {
                        packet.add(index, message);
                        modtaget[index] = true;
                    }
                    modtaget = getField(antal); // creates a array of boollean variables 
                    firstPacket = false;
                    boolean allRes = false;
                    while (!allRes) {
                        socket.receive(receivePacket); // receive data  
                        message = new String(receivePacket.getData()); 
                        getData(); // sepparate data 
                        if (!(modtaget[index])) { // if the index 
                            packet.add(index, message); // if packet is already saved dont save it again. 
                            modtaget[index] = true;
                        }

                        if (index == antal) {
                            missing = getMissing();
                            do {

                                int ok = missing - 1;
                                String thisAk = "AK" + ok + "Next" + missing;
                                byte[] sendData = thisAk.getBytes();
                                DatagramPacket AK = new DatagramPacket(sendData, sendData.length, sender, port);

                                socket.receive(receivePacket);
                                message = new String(receivePacket.getData());
                                getData();
                                if (!(modtaget[index])) {
                                    packet.add(index, message);
                                    modtaget[index] = true;
                                }

                                missing = getMissing();
                            } while (missing != 101);
                        }
                    }
                }
                target.myCode(packet);
            } catch (Exception ex) {
                System.out.print(Arrays.toString(ex.getStackTrace()));
            }

        }
    }
    /**
     * Get Data separated. Header from original Message.  
     */
    private void getData() {
        message = message.replace("HEAD*A", "");
        antalS = message.substring(0, message.indexOf('#'));
        indexS = message.substring(message.indexOf('#') + 1, message.indexOf("S"));
        sessionStatus = message.substring(message.indexOf('S') + 1, message.indexOf("*"));
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
     * @param antal
     * @return t 
     */
    private boolean[] getField(int antal) {
        boolean[] t = new boolean[antal];
        for (int i = 1; i <= antal; i++) {
            t[i] = false;
        }
        return t;
    }

    private int getMissing() {
        for (int i = 1; i <= antal; i++) {
            if (!(modtaget[i])) {
                return i;
            }
        }
        return 101;

    }
}

package udp_jti;


import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private String IP;
    private DatagramSocket socket = null;
    private String thisMessage;
    private UDPII target;
    private String dataType;

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
                        String Response = "ConOK" + newPort;
                        byte[] sendData = Response.getBytes();
                        DatagramPacket conOK = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        new Thread(new serverStart(newSocket, target, IPAddress,dataType)).start();
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

}

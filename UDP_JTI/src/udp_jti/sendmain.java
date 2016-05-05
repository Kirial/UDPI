/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_jti;

import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author taras
 */
public class sendmain {

    public static void main(String[] args) {
        System.out.println("Afsender");
        while (true) {
            Scanner keyboard = new Scanner(System.in, "windows-1252");
            System.out.println("Indtast IP adressen på modtageren");
            String indtastIP = keyboard.nextLine();
            String IP = indtastIP;
          //  String IP = "127.0.0.1";
            int Port = 50000;
            try {
        System.out.println("Indtast tekst der skal sendes");

                String inData = keyboard.nextLine();
                System.out.println("Sending");
                UDPISender socket = new UDPISender();
                socket.send(Port, IP, inData);
            } catch (Exception ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }
        }
    }
}

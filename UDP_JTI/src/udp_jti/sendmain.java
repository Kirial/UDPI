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
        System.out.println("Indtast tekst der skal sendes");
        while (true) {
            Scanner keyboard = new Scanner(System.in,"windows-1252");
               String IP = "10.16.236.9";
                //String IP = "127.0.0.1";
                int Port = 50000;
            try {

                String inData = keyboard.nextLine();
                System.out.println("Sending");
                UDPI socket = new UDPI();
                socket.send(Port, IP, inData);
            } catch (Exception ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }
        }
    }
}

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
        while (true) {
            Scanner keyboard = new Scanner(System.in);
               // String IP = "10.16.225.40";
                String IP = "127.0.0.1";
                int Port = 50000;
            try {

                String inData = keyboard.nextLine();
                System.out.println("Sendeing");
                UDPI socket = new UDPI();
                socket.send(Port, IP, inData);
            } catch (Exception ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }
        }
    }
}

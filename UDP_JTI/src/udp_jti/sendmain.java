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
        try {
            Scanner keyboard = new Scanner(System.in);
            String inData = keyboard.nextLine();
            UDPI socket = new UDPI();
            String IP = "127.0.0.1";
            int Port = 5000;
            socket.send(Port, IP,inData);
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
    }
}

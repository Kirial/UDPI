/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_jti;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author taras
 */
public class UDP_JTI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // modtager main 
        UDPII target = new UDPII(){

            @Override
            public void myCode(ArrayList<String> m) {
               System.out.println("type: "+m.get(0)+" string: "+m.get(1));
            }

        };
        
        try {
            UDPI nySocket = new UDPI(target,4500);
            while(true){
            nySocket.listen();
            }
        } catch (Exception test) {
            System.out.println("test");
        }

    }
}

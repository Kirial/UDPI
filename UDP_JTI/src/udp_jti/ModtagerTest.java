/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_jti;

import java.util.ArrayList;



/**
 *
 * @author taras
 */
public class ModtagerTest{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // modtager main 
        UDPII target = new UDPII(){

            @Override
            public void myCode(ArrayList<String> m) {
               System.out.println("type: "+m.get(0)+" string: "+m.get(1)); // get message and then print it
            }

        };
        
        try {
            UDPI nySocket = new UDPI(target,50000);
            while(true){
            nySocket.listen();
            }
        } catch (Exception test) {
            System.out.println("test");
        }

    }
}

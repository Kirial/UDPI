/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_jti;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author taras
 */
public class ModtagerTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Serveren");
        System.out.println("Din IP adresse er. ");
        try {
            InetAddress inet = InetAddress.getLocalHost();
            InetAddress[] ips = InetAddress.getAllByName(inet.getCanonicalHostName());
            System.out.println(ips[0]);
            
  if (ips  != null ) {
    for (int i = 0; i < ips.length; i++) {
      System.out.println(ips[i]);
      
    }
          System.out.println("vælg en af dem.");
          System.out.println("");
          System.out.println("");
          

  }
} catch (UnknownHostException e) {

        }
        // modtager main 
        UDPII target = new UDPII() {

            @Override
            public void myCode(String m) {
                System.out.println("modtaget: " + m); // get message and then print it
                String sendM = "User Datagram Protocol, UDP er en protokol til overførsel af data. UDP er en del af Internet-protokolstakken, som oftest benævnes TCP/IP. I protokolstakken anvendes enten TCP eller UDP. UDP giver ingen garanti for at data kommer frem (eller rettere: Afsenderen får ikke besked hvis data ikke kommer frem, ligeledes får afsender ikke besked hvorvidt data er modtaget) igen User Datagram Protocol, UDP er en protokol til overførsel af data. UDP er en del af Internet-protokolstakken, som oftest benævnes TCP/IP. I protokolstakken anvendes enten TCP eller UDP. UDP giver ingen garanti for at data kommer frem (eller rettere: Afsenderen får ikke besked hvis data ikke kommer frem, ligeledes får afsender ikke besked hvorvidt data er modtaget)";
                if(m.contentEquals(sendM)){
                    System.out.println("alt er modtaget");
                }
            }

        };

        try {
            UDPIModtager nySocket = new UDPIModtager(target, 50000);
            while (true) {
                nySocket.listen();
            }
        } catch (Exception test) {
            System.out.println("Error");
        }

    }
}

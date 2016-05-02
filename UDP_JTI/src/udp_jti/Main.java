/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_jti;

import java.util.Scanner;

/**
 *
 * @author jespe
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("  _______                                  _                           ");
        System.out.println(" |__   __|                    ___         | |                          ");
        System.out.println("    | | __ _ _ __ __ _ ___   ( _ )        | | ___  ___ _ __   ___ _ __ ");
        System.out.println("    | |/ _` | '__/ _` / __|  / _ \\/\\  _   | |/ _ \\/ __| '_ \\ / _ \\ '__|");
        System.out.println("    | | (_| | | | (_| \\__ \\ | (_>  < | |__| |  __/\\__ \\ |_) |  __/ |   ");
        System.out.println("    |_|\\__,_|_|  \\__,_|___/  \\___/\\/  \\____/ \\___||___/ .__/ \\___|_|   ");
        System.out.println("                                                      | |              ");
        System.out.println("                                                      |_|              ");
        System.out.println("UPDI - Udviklet af Taras Karpin & Jesper Kirial.");
        System.out.println("UPDI giver integritet i transmission af tekst mellem to klienter");
        System.out.println("");
        System.out.println("Velkommen til UPDI.");
        System.out.println("Tast 1: for at sende en besked");
        System.out.println("Tast 2: for at modtage en besked");
        System.out.println("Tast 3: for at afslutte");
        System.out.print("Valg: ");
        Scanner keyboard = new Scanner(System.in);
        int valg = keyboard.nextInt();

        switch (valg) {
            case 1:
                sendmain.main(args);
            case 2:
                ModtagerTest.main(args);
            case 3:
                return;
        }

    }

}

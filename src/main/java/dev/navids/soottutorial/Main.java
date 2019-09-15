package dev.navids.soottutorial;

import dev.navids.soottutorial.hellosoot.HelloSoot;

import java.util.Arrays;

public class Main {
    public static void main(String[] args){
        if (args.length == 0){
            System.err.println("You must provide the name of the Java class file that you want to run.");
            return;
        }
        if(args[0].equals("HelloSoot"))
            HelloSoot.main(Arrays.copyOfRange(args, 1, args.length));
        else
            System.err.println("The class '" + args[0] + "' does not exists or does not have a main method.");
    }
}

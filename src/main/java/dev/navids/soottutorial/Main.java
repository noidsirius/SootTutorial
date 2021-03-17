package dev.navids.soottutorial;

import dev.navids.soottutorial.android.AndroidClassInjector;
import dev.navids.soottutorial.android.AndroidLogger;
import dev.navids.soottutorial.android.AndroidCallgraph;
import dev.navids.soottutorial.android.AndroidPointsToAnalysis;
import dev.navids.soottutorial.basicapi.BasicAPI;
import dev.navids.soottutorial.hellosoot.HelloSoot;
import dev.navids.soottutorial.intraanalysis.npanalysis.NPAMain;
import dev.navids.soottutorial.intraanalysis.usagefinder.UsageFinder;

import java.util.Arrays;

public class Main {
    public static void main(String[] args){
        if (args.length == 0){
            System.err.println("You must provide the name of the Java class file that you want to run.");
            return;
        }
        String[] restOfTheArgs = Arrays.copyOfRange(args, 1, args.length);
        if(args[0].equals("HelloSoot"))
            HelloSoot.main(restOfTheArgs);
        else if(args[0].equals("BasicAPI"))
            BasicAPI.main(restOfTheArgs);
        else if(args[0].equals("AndroidLogger")) {
            AndroidLogger.main(restOfTheArgs);
        }
        else if(args[0].equals("AndroidClassInjector")) {
            AndroidClassInjector.main(restOfTheArgs);
        }
        else if(args[0].equals("AndroidCallGraph")) {
            AndroidCallgraph.main(restOfTheArgs);
        }
        else if(args[0].equals("AndroidPTA")) {
            AndroidPointsToAnalysis.main(restOfTheArgs);
        }
        else if(args[0].equals("UsageFinder"))
            UsageFinder.main(restOfTheArgs);
        else if(args[0].equals("NullPointerAnalysis"))
            NPAMain.main(restOfTheArgs);
        else
            System.err.println("The class '" + args[0] + "' does not exists or does not have a main method.");
    }
}

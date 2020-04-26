package dev.navids.soottutorial.intraanalysis.usagefinder;

import soot.*;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.options.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class UsageFinder {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "IntraAnalysis";
    public static String clsName = "UsageExample";

    public static void setupSoot() {
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(sourceDirectory);
        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
    }

    public static boolean doesInvokeTheMethod(Unit u, String methodSubsignature, String classSignature) {
        AtomicBoolean result = new AtomicBoolean(false);
        u.apply(new AbstractStmtSwitch() {
            @Override
            public void caseInvokeStmt(InvokeStmt invokeStmt) {
                String invokedSubsignature = invokeStmt.getInvokeExpr().getMethod().getSubSignature();
                String invokedClassSignature = invokeStmt.getInvokeExpr().getMethod().getDeclaringClass().getName();
                if (invokedSubsignature.equals(methodSubsignature)) {
                    if (classSignature == null || invokedClassSignature.equals(classSignature)) {
                        result.set(true);
                    }
                }

            }
        });
        return result.get();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide a method subsignature to search for its usages.");
            return;
        }
        setupSoot();
        String usageMethodSubsignature = args[0];
        String usageClassSignature = null;
        String classMessage = "";
        if (args.length > 1) {
            usageClassSignature = args[1];
            classMessage = " of the class " + usageClassSignature;
        }
        System.out.println("Searching the usages of method " + usageMethodSubsignature + classMessage + "...");
        SootClass mainClass = Scene.v().getSootClass(clsName);
        for (SootMethod sm : mainClass.getMethods()) {
            JimpleBody body = (JimpleBody) sm.retrieveActiveBody();


            List<Unit> usageFound = new ArrayList<>();
            for (Iterator<Unit> it = body.getUnits().snapshotIterator(); it.hasNext(); ) {
                Unit u = it.next();
                if (doesInvokeTheMethod(u, usageMethodSubsignature, usageClassSignature))
                    usageFound.add(u);
            }
            if (usageFound.size() > 0) {
                System.out.println(usageFound.size() + " Usage(s) found in the method " + sm.getSignature());
                for (Unit u : usageFound) {
                    System.out.println("   " + u.toString());
                }
            }

        }
    }
}

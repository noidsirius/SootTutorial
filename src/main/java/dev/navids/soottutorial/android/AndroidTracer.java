package dev.navids.soottutorial.android;

import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.options.Options;
import soot.util.Chain;

import java.io.IOException;
import java.util.Arrays;
        import java.util.Collections;
        import java.util.Map;

public class AndroidTracer {

    static String androidJar = "path/to/android/jar";
    static String apkPath = "path/to/apk";
    static String outputPath = "path/to/outupt/dir";


    private static void setupSoot() {
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().set_android_jars(androidJar);
        Options.v().set_process_dir(Collections.singletonList(apkPath));
        Options.v().set_include_all(true);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_output_dir(outputPath);
        Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
    }

    public static void main(String[] args){
        setupSoot();
        // Find the package name of the APK. The injected class must be inside this package.
        String packageName = getPackageName();
        String staticCounterSignature =  packageName+".StaticCounter";
        SootClass staticCounterClass = new SootClass(staticCounterSignature, Modifier.PUBLIC);
        staticCounterClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        staticCounterClass.setApplicationClass();
        // counterField is a static integer field in StaticCounter class.
        SootField counterField = new SootField("counter", IntType.v(), Modifier.PUBLIC | Modifier.STATIC);
        staticCounterClass.addField(counterField);
        // incrementAndLog method increment counterField and prints its value.
        SootMethod incNLogMethod = addIncNLogMethod(staticCounterClass, counterField);
        // Add a transformation pack in order to add incrementAndLog method at top of each method in the app
        PackManager.v().getPack("jtp").add(new Transform("jtp.myLogger", new BodyTransformer() {
            @Override
            protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
                // Check if this method is not the incrementAndLog method
                if(b.getMethod().getSignature().equals(incNLogMethod.getSignature()))
                    return;
                UnitPatchingChain units = b.getUnits();
                Unit injectionUnitPosition = getFirstInsertableUnit(b);
                // Add a log message to show what method is calling incrementAndLogs
                addLogUnits(b, injectionUnitPosition, "STATICCOUNTER METHOD " + b.getMethod().getSignature());
                units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(incNLogMethod.makeRef())), injectionUnitPosition);
                b.validate();
            }
        }));
        PackManager.v().runPacks();
        PackManager.v().writeOutput();

    }

    public static SootMethod addIncNLogMethod(SootClass staticCounterClass, SootField counterField){
        SootMethod incMethod = new SootMethod("incrementAndLog",
                Arrays.asList(new Type[]{}),
                VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
        staticCounterClass.addMethod(incMethod);
        JimpleBody body = Jimple.v().newBody(incMethod);

        UnitPatchingChain units = body.getUnits();
        // Increment counterField by one
        Local counterLocal = Jimple.v().newLocal("counterLocal", IntType.v());
        body.getLocals().add(counterLocal);
        units.add(Jimple.v().newAssignStmt(counterLocal, Jimple.v().newStaticFieldRef(counterField.makeRef())));
        units.add(Jimple.v().newAssignStmt(counterLocal,
                Jimple.v().newAddExpr(counterLocal, IntConstant.v(1))));
        units.add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(counterField.makeRef()),counterLocal));

        // This is the return statement of the body, we will use this unit to insert the logger before it.
        Unit returnUnit = Jimple.v().newReturnVoidStmt();
        units.add(returnUnit);
        addLogUnits(body, returnUnit, "STATICCOUNTER VALUE", counterLocal);
        body.validate();
        incMethod.setActiveBody(body);
        return incMethod;
    }

    public static void addLogUnits(Body body, Unit injecUnitPosition, String tag){
        addLogUnits(body, injecUnitPosition, tag, null);
    }

    public static void addLogUnits(Body body, Unit injectionUnitPosition, String tag, Value intValue){
        // For more information please refer to https://github.com/Sable/soot/wiki/Instrumenting-Android-Apps-with-Soot
        UnitPatchingChain units = body.getUnits();
        String TAG = tag + " ";
        Local psLocal = Jimple.v().newLocal("psLocal", RefType.v("java.io.PrintStream"));
        body.getLocals().add(psLocal);

        units.insertBefore(Jimple.v().newAssignStmt(
                psLocal, Jimple.v().newStaticFieldRef(
                        Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), injectionUnitPosition);
        SootMethod printMethod = Scene.v().getSootClass("java.io.PrintStream")
                .getMethod("void print(java.lang.Object)");
        units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(psLocal, printMethod.makeRef(), StringConstant.v(TAG))), injectionUnitPosition);
        if(intValue != null) {
            SootMethod printIntMethod = Scene.v().getSootClass("java.io.PrintStream")
                    .getMethod("void print(int)");
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(psLocal, printIntMethod.makeRef(), intValue)), injectionUnitPosition);
        }
        units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(psLocal, printMethod.makeRef(), StringConstant.v("\n"))), injectionUnitPosition);
    }

    private static String getPackageName() {
        String packageName = "";
        try {
            ProcessManifest manifest = new ProcessManifest(apkPath);
            packageName = manifest.getPackageName();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return packageName;
    }

    // A helper method to find the first unit that we can inject our code before it.
    // There should be no unit before JIdentity statements
    public static Unit getFirstInsertableUnit(Body b){

        Chain<Unit> units = b.getUnits();
        Unit afterLastIdentity = null;
        for(Unit u2 : units){
            if(u2 instanceof JIdentityStmt)
                afterLastIdentity = u2;
            else
                break;
        }
        if(afterLastIdentity == null)
            afterLastIdentity = units.getFirst();
        else
            afterLastIdentity = units.getSuccOf(afterLastIdentity);
        return afterLastIdentity;
    }
}
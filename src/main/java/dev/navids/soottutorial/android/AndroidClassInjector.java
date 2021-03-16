package dev.navids.soottutorial.android;

import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AndroidClassInjector {

    private final static String USER_HOME = System.getProperty("user.home");
    private static String androidJar = USER_HOME + "/Library/Android/sdk/platforms";
    static String androidDemoPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "Android";
    static String apkPath = androidDemoPath + File.separator + "/calc.apk";
    static String outputPath = androidDemoPath + File.separator + "/Instrumented";

    public static void main(String[] args){
        if(System.getenv().containsKey("ANDROID_HOME"))
            androidJar = System.getenv("ANDROID_HOME")+ File.separator+"platforms";
        // Clean the outputPath
        final File[] files = (new File(outputPath)).listFiles();
        if (files != null && files.length > 0) {
            Arrays.asList(files).forEach(File::delete);
        }
        // Initialize Soot
        AndroidUtils.setupSoot(androidJar, apkPath, outputPath);
        // Find the package name of the APK
        String packageName = AndroidUtils.getPackageName(apkPath);
        // Create and inject a class with a field and a method to the APK
        SootMethod incNLogMethod = injectCode(packageName);
        // Add a transformation pack in order to insert incrementAndLog method at top of each method in the app
        PackManager.v().getPack("jtp").add(
                new Transform("jtp.myLogger", new StaticMethodCallInjector(incNLogMethod)));
        PackManager.v().runPacks();
        PackManager.v().writeOutput();

    }

    static SootMethod injectCode(String packageName) {
        SootClass staticCounterClass = createCounterClass(packageName);
        SootField counterField = addCounterFieldToClass(staticCounterClass);
        return addIncNLogMethod(staticCounterClass, counterField);
    }

    static class StaticMethodCallInjector extends BodyTransformer{

        SootMethod incNLogMethod;
        public StaticMethodCallInjector(SootMethod incNLogMethod) {
            super();
            this.incNLogMethod = incNLogMethod;
        }

        @Override
        protected void internalTransform(Body b, String s, Map<String, String> map) {
            JimpleBody body = (JimpleBody) b;
            // Check if this method is not the incrementAndLog method and an Android Framework method
            if(b.getMethod().getSignature().equals(incNLogMethod.getSignature()) || AndroidUtils.isAndroidMethod(b.getMethod()))
                return;
            UnitPatchingChain units = b.getUnits();
            List<Unit> generated = new ArrayList<>();
            // Add a log message to show what method is calling incrementAndLogs
            generated.addAll(AndroidUtils.generateLogStmts(body, "Beginning of method " + b.getMethod().getSignature()));
            // Call incrementAndLog method
            generated.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(incNLogMethod.makeRef())));
            units.insertBefore(generated, body.getFirstNonIdentityStmt());
            b.validate();
        }
    }

    static SootClass createCounterClass(String packageName) {
        // The new class must be inside the APK package.
        String staticCounterSignature =  packageName+".SootTutorialStaticCounter";
        SootClass staticCounterClass = new SootClass(staticCounterSignature, Modifier.PUBLIC);
        staticCounterClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        staticCounterClass.setApplicationClass();
        return staticCounterClass;
    }

    static SootField addCounterFieldToClass(SootClass staticCounterClass) {
        // counterField is a static integer field in StaticCounter class.
        SootField counterField = new SootField("counter", IntType.v(), Modifier.PUBLIC | Modifier.STATIC);
        staticCounterClass.addField(counterField);
        return counterField;
    }


    static SootMethod addIncNLogMethod(SootClass staticCounterClass, SootField counterField){
        // incrementAndLog method increment counterField and prints its value.
        SootMethod incMethod = new SootMethod("incrementAndLog",
                Arrays.asList(new Type[]{}),
                VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
        staticCounterClass.addMethod(incMethod);
        JimpleBody body = Jimple.v().newBody(incMethod);

        UnitPatchingChain units = body.getUnits();
        // Increment counterField by one
        Local counterLocal = AndroidUtils.generateNewLocal(body, IntType.v());
        units.add(Jimple.v().newAssignStmt(counterLocal, Jimple.v().newStaticFieldRef(counterField.makeRef())));
        units.add(Jimple.v().newAssignStmt(counterLocal,
                Jimple.v().newAddExpr(counterLocal, IntConstant.v(1))));
        units.add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(counterField.makeRef()),counterLocal));

        // Log the counter value
        units.addAll(AndroidUtils.generateLogStmts(body, "Counter's value: ", counterLocal));

        // The method should be finished by a return
        Unit returnUnit = Jimple.v().newReturnVoidStmt();
        units.add(returnUnit);
        body.validate();
        incMethod.setActiveBody(body);
        return incMethod;
    }
}
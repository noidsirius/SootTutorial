package dev.navids.soottutorial.android;

import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.*;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.internal.JIdentityStmt;
import soot.options.Options;
import soot.util.Chain;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AndroidLogger {

    private final static String USER_HOME = System.getProperty("user.home");
    private static String androidJar = USER_HOME + "/Library/Android/sdk/platforms";
    static String apkPath = "path/to/apk";
    static String outputPath = "path/to/outupt/dir";
    private static String TAG = "<SOOT_TUTORIAL>";


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
        apkPath = "/Users/navid/IdeaProjects/dynamic_feature_module_analysis/apks/12-PingApp/app-debug.apk";
        outputPath = "/tmp";
        setupSoot();
        // Add a transformation pack in order to add the statement "System.out.println(<content>) at the beginning of each Application method
        PackManager.v().getPack("jtp").add(new Transform("jtp.myLogger", new BodyTransformer() {
            @Override
            protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
                if(isAndroidMethod(b.getMethod()))
                    return;
                JimpleBody body = (JimpleBody) b;
                UnitPatchingChain units = b.getUnits();
                // There should be no unit before JIdentity statements; so, we find the first unit that we can inject our code before it.
                Unit injectionUnitPosition = body.getFirstNonIdentityStmt();
                // The message that we want to log
                String content = String.format("%s Beginning of method %s", TAG, body.getMethod().getSignature());
                // In order to call "System.out.println" we need to have a local containing "System.out" value
                Local psLocal = Jimple.v().newLocal("psLocal"+ body.getLocalCount(), RefType.v("java.io.PrintStream"));
                body.getLocals().add(psLocal);
                // Assign "System.out" to psLocal
                SootField sysOutField = Scene.v().getField("<java.lang.System: java.io.PrintStream out>");
                AssignStmt sysOutAssignStmt = Jimple.v().newAssignStmt(psLocal, Jimple.v().newStaticFieldRef(sysOutField.makeRef()));
                // Inject the generated assign statement right before the first injectable unit
                units.insertBefore(sysOutAssignStmt, injectionUnitPosition);
                // Create println method call, provide its parameter, and inject it
                SootMethod printlnMethod = Scene.v().grabMethod("<java.io.PrintStream: void println(java.lang.String)>");
                Value printlnParamter = StringConstant.v(content);
                InvokeStmt printlnMethodCall = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(psLocal, printlnMethod.makeRef(), printlnParamter));
                units.insertBefore(printlnMethodCall, injectionUnitPosition);
                // Validate the body to ensure that our code injection does not introduce any problem (at least statically)
                b.validate();
            }
        }));
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }

    private static boolean isAndroidMethod(SootMethod sootMethod){
        String clsSig = sootMethod.getDeclaringClass().getName();
        List<String> androidPrefixPkgNames = Arrays.asList("android.", "com.google.android", "androidx.");
        return androidPrefixPkgNames.stream().map(clsSig::startsWith).reduce(false, (res, curr) -> res || curr);
    }

}
package dev.navids.soottutorial.android;

import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.options.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AndroidUtils {
    public static final String TAG = "<SOOT_TUTORIAL>";

    public static void setupSoot(String androidJar, String apkPath, String outputPath) {
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

    public static List<Unit> generateLogStmts(JimpleBody b, String msg) {
        return generateLogStmts(b, msg, null);
    }

    public static List<Unit> generateLogStmts(JimpleBody b, String msg, Value value) {
        List<Unit> generated = new ArrayList<>();
        Value logMessage = StringConstant.v(msg);
        Value logType = StringConstant.v(TAG);
        Value logMsg = logMessage;
        if (value != null)
            logMsg = AndroidUtils.appendTwoStrings(b, logMessage, value, generated);
        SootMethod sm = Scene.v().getMethod("<android.util.Log: int i(java.lang.String,java.lang.String)>");
        StaticInvokeExpr invokeExpr = Jimple.v().newStaticInvokeExpr(sm.makeRef(), logType, logMsg);
        generated.add(Jimple.v().newInvokeStmt(invokeExpr));
        return generated;
    }

    private static Local appendTwoStrings(Body b, Value s1, Value s2, List<Unit> generated) {
        RefType stringType = Scene.v().getSootClass("java.lang.String").getType();
        SootClass builderClass = Scene.v().getSootClass("java.lang.StringBuilder");
        RefType builderType = builderClass.getType();
        NewExpr newBuilderExpr = Jimple.v().newNewExpr(builderType);
        Local builderLocal = generateNewLocal(b, builderType);
        generated.add(Jimple.v().newAssignStmt(builderLocal, newBuilderExpr));
        Local tmpLocal = generateNewLocal(b, builderType);
        Local resultLocal = generateNewLocal(b, stringType);

        VirtualInvokeExpr appendExpr = Jimple.v().newVirtualInvokeExpr(builderLocal,
                builderClass.getMethod("java.lang.StringBuilder append(java.lang.String)").makeRef(), toString(b, s2, generated));
        VirtualInvokeExpr toStrExpr = Jimple.v().newVirtualInvokeExpr(builderLocal, builderClass.getMethod("java.lang.String toString()").makeRef());

        generated.add(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(builderLocal, builderClass.getMethod("void <init>(java.lang.String)").makeRef(), s1)));
        generated.add(Jimple.v().newAssignStmt(tmpLocal, appendExpr));
        generated.add(Jimple.v().newAssignStmt(resultLocal, toStrExpr));

        return resultLocal;
    }

    public static Value toString(Body b, Value value, List<Unit> generated) {
        SootClass stringClass = Scene.v().getSootClass("java.lang.String");
        if (value.getType().equals(stringClass.getType()))
            return value;
        Type type = value.getType();

        if (type instanceof PrimType) {
            Local tmpLocal = generateNewLocal(b, stringClass.getType());
            generated.add(Jimple.v().newAssignStmt(tmpLocal,
                    Jimple.v().newStaticInvokeExpr(stringClass.getMethod("java.lang.String valueOf(" + type.toString() + ")").makeRef(), value)));
            return tmpLocal;
        } else if (value instanceof Local){
            Local base = (Local) value;
            SootMethod toStrMethod = Scene.v().getSootClass("java.lang.Object").getMethod("java.lang.String toString()");
            Local tmpLocal = generateNewLocal(b, stringClass.getType());
            generated.add(Jimple.v().newAssignStmt(tmpLocal,
                    Jimple.v().newVirtualInvokeExpr(base, toStrMethod.makeRef())));
            return tmpLocal;
        }
        else{
            throw new RuntimeException(String.format("The value %s should be primitive or local but it's %s", value, value.getType()));
        }
    }

    public static Local generateNewLocal(Body body, Type type) {
        LocalGenerator lg = new LocalGenerator(body);
        return lg.generateLocal(type);
    }

    public static String getPackageName(String apkPath) {
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

    public static boolean isAndroidMethod(SootMethod sootMethod){
        String clsSig = sootMethod.getDeclaringClass().getName();
        List<String> androidPrefixPkgNames = Arrays.asList("android.", "com.google.android", "androidx.");
        return androidPrefixPkgNames.stream().map(clsSig::startsWith).reduce(false, (res, curr) -> res || curr);
    }
}

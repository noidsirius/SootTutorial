package dev.navids.soottutorial.android;

import dev.navids.soottutorial.visual.AndroidCallGraphFilter;
import dev.navids.soottutorial.visual.Visualizer;
import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class PointsToAnalysis {
    private final static String USER_HOME = System.getProperty("user.home");
    private static String androidJar = USER_HOME + "/Library/Android/sdk/platforms";
    static String androidDemoPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "Android";
    static String apkPath = androidDemoPath + File.separator + "/st_demo.apk";


    public static void main(String[] args){
        Set<String> validClsNames = new HashSet<>();
        try {
            ProcessManifest manifest = new ProcessManifest(apkPath);
            for(AXmlNode node : manifest.getActivities()){
                validClsNames.add(node.getAttribute("name").getValue().toString());
                System.out.println(node.getAttribute("name").getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
        config.getAnalysisFileConfig().setTargetAPKFile(apkPath);
        config.getAnalysisFileConfig().setAndroidPlatformDir(androidJar);
        config.setTaintAnalysisEnabled(false);
        config.setAliasingAlgorithm(InfoflowConfiguration.AliasingAlgorithm.FlowSensitive);
        config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
        config.setEnableReflection(true);
        config.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.SPARK);
        SetupApplication app = new SetupApplication(config);
        app.constructCallgraph();
//        UnitGraph ug = new ClassicCompleteUnitGraph(app.getDummyMainMethod().getDeclaringClass().getMethods().get(3).getActiveBody());
//        Visualizer.v().addUnitGraph(ug, false);
//        Visualizer.v().draw();
//        if(1 == 1)
//            return;
//        if(Scene.v().getPointsToAnalysis() instanceof DumbPointerAnalysis) {
//            try {
//                Method method = SetupApplication.class.getDeclaredMethod("constructCallgraphInternal");
//                method.setAccessible(true);
//                method.invoke(app);
//            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println(Scene.v().getPointsToAnalysis());
//        CallGraph callGraph = Scene.v().getCallGraph();
//        for(SootClass sootClass : app.getEntrypointClasses()){
//            for(SootMethod sootMethod : sootClass.getMethods()){
//                soot.PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
//                for(Unit unit : sootMethod.getActiveBody().getUnits()){
//                    if (unit instanceof InvokeStmt){
//                        InvokeStmt invokeStmt = (InvokeStmt) unit;
//                        Scene.v();
//                    }
//                }
//                for(Local local : sootMethod.getActiveBody().getLocals()){
//                    pointsToAnalysis.reachingObjects(local);
//                    Scene.v();
//                }
//            }
//        }
//        boolean drawGraph = true;
//        if (drawGraph) {
//            Visualizer.v().addCallGraph(callGraph, new AndroidCallGraphFilter(AndroidUtils.getPackageName(apkPath)), new Visualizer.AndroidNodeAttributeConfig(true));
//            Visualizer.v().draw();
//        }

    }

}

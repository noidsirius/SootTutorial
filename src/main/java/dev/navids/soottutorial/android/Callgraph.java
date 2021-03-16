package dev.navids.soottutorial.android;

import dev.navids.soottutorial.visual.AndroidCallGraphFilter;
import dev.navids.soottutorial.visual.Visualizer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.io.File;
import java.util.Iterator;

public class Callgraph {
    private final static String USER_HOME = System.getProperty("user.home");
    private static String androidJar = USER_HOME + "/Library/Android/sdk/platforms";
    static String androidDemoPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "Android";
    static String apkPath = androidDemoPath + File.separator + "/st_demo.apk";

    public static void main(String[] args){
        if(System.getenv().containsKey("ANDROID_HOME"))
            androidJar = System.getenv("ANDROID_HOME")+ File.separator+"platforms";
        final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
        config.getAnalysisFileConfig().setTargetAPKFile(apkPath);
        config.getAnalysisFileConfig().setAndroidPlatformDir(androidJar);
        config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
        config.setEnableReflection(true);
        config.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.SPARK);
        SetupApplication app = new SetupApplication(config);
        app.constructCallgraph();
        CallGraph callGraph = Scene.v().getCallGraph();
        String appPackageName = AndroidUtils.getPackageName(apkPath);
        int compIndex = 0;
        AndroidCallGraphFilter androidCallGraphFilter = new AndroidCallGraphFilter(appPackageName);
        for(SootClass sootClass: androidCallGraphFilter.getValidClasses()){
            System.out.println(String.format("Class %d: %s", ++compIndex, sootClass.getName()));
            for(SootMethod sootMethod : sootClass.getMethods()){
                int incomingEdge = 0;
                for(Iterator<Edge> it = callGraph.edgesInto(sootMethod); it.hasNext();incomingEdge++,it.next());
                int outgoingEdge = 0;
                for(Iterator<Edge> it = callGraph.edgesOutOf(sootMethod); it.hasNext();outgoingEdge++,it.next());
                System.out.println(String.format("\tMethod %s, #IncomeEdges: %d, #OutgoingEdges: %d", sootMethod.getName(), incomingEdge, outgoingEdge));
            }
        }
        boolean drawGraph = true;
        if (drawGraph) {
            Visualizer.v().addCallGraph(callGraph,
                    androidCallGraphFilter,
                    new Visualizer.AndroidNodeAttributeConfig(false));
            Visualizer.v().draw();
        }

    }

}

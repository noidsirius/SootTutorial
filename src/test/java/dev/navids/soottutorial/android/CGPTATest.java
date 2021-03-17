package dev.navids.soottutorial.android;

import org.junit.Test;
import soot.*;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.sets.DoublePointsToSet;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.scalar.Pair;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CGPTATest {
    private String mainActivityDialogueClassName = "MainActivity$2$1";
    private String apkPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "Android" + File.separator + "/st_demo.apk";
    private String androidJar = System.getenv().containsKey("ANDROID_HOME")
                                    ? System.getenv("ANDROID_HOME")+ File.separator+"platforms"
                                    : System.getProperty("user.home") + "/Library/Android/sdk/platforms";

    @Test
    public void testConfig(){
        String packageName = AndroidUtil.getPackageName(apkPath);
        assertEquals("dev.navids.multicomp1", packageName);
        InfoflowAndroidConfiguration config = AndroidUtil.getFlowDroidConfig(apkPath, androidJar);
        assertEquals(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination, config.getCodeEliminationMode());
        assertEquals(InfoflowConfiguration.CallgraphAlgorithm.SPARK, config.getCallgraphAlgorithm());
        SetupApplication app = new SetupApplication(config);
        app.constructCallgraph();
        assertEquals(3, app.getEntrypointClasses().size());
    }

    @Test
    public void testCHACallGraph(){
        InfoflowAndroidConfiguration config = AndroidUtil.getFlowDroidConfig(apkPath, androidJar, InfoflowConfiguration.CallgraphAlgorithm.CHA);
        assertEquals(InfoflowConfiguration.CallgraphAlgorithm.CHA, config.getCallgraphAlgorithm());
        SetupApplication app = new SetupApplication(config);
        app.constructCallgraph();
        CallGraph callGraph = Scene.v().getCallGraph();
        SootMethod childBaseMethod = Scene.v().getMethod(AndroidCallgraph.childBaseMethodSignature);
        boolean flag = false;
        for (Iterator<Edge> it = callGraph.edgesInto(childBaseMethod); it.hasNext(); ) {
            Edge edge = it.next();
            if(edge.src().getDeclaringClass().getShortName().equals(mainActivityDialogueClassName))
                flag = true;
        }
        assertTrue(flag);
        SootMethod childMethod = Scene.v().getMethod(AndroidCallgraph.childMethodSignature);
        SootMethod parentMethod = Scene.v().getMethod(AndroidCallgraph.parentMethodSignature);
        SootMethod mainActivityEntryMethod = Scene.v().getMethod(AndroidCallgraph.mainActivityEntryPointSignature);
        Map<SootMethod, SootMethod> reachableParentMap = AndroidCallgraph.getAllReachableMethods(mainActivityEntryMethod);
        assertTrue(reachableParentMap.containsKey(childMethod));
        assertTrue(reachableParentMap.containsKey(parentMethod));
        String possiblePath = AndroidCallgraph.getPossiblePath(reachableParentMap, parentMethod);
        assertEquals(3, possiblePath.split("->").length);
    }

    @Test
    public void testSPARKCallGraph(){
        SetupApplication app = new SetupApplication(AndroidUtil.getFlowDroidConfig(apkPath, androidJar));
        app.constructCallgraph();
        CallGraph callGraph = Scene.v().getCallGraph();
        SootMethod childBaseMethod = Scene.v().getMethod(AndroidCallgraph.childBaseMethodSignature);
        boolean flag = false;
        for (Iterator<Edge> it = callGraph.edgesInto(childBaseMethod); it.hasNext(); ) {
            Edge edge = it.next();
            if(edge.src().getDeclaringClass().getShortName().equals(mainActivityDialogueClassName))
                flag = true;
        }
        assertFalse(flag);
        SootMethod childMethod = Scene.v().getMethod(AndroidCallgraph.childMethodSignature);
        SootMethod parentMethod = Scene.v().getMethod(AndroidCallgraph.parentMethodSignature);
        SootMethod mainActivityEntryMethod = Scene.v().getMethod(AndroidCallgraph.mainActivityEntryPointSignature);
        Map<SootMethod, SootMethod> reachableParentMap = AndroidCallgraph.getAllReachableMethods(mainActivityEntryMethod);
        assertTrue(reachableParentMap.containsKey(childMethod));
        assertFalse(reachableParentMap.containsKey(parentMethod));
        String possiblePath = AndroidCallgraph.getPossiblePath(reachableParentMap, childMethod);
        assertEquals(4, possiblePath.split("->").length);
    }

    @Test
    public void testPointsToAnalysis(){
        SetupApplication app = new SetupApplication(AndroidUtil.getFlowDroidConfig(apkPath, androidJar));
        app.constructCallgraph();
        List<Pair<Local, String>> allParentChildLocals = AndroidPointsToAnalysis.getParentChildClassLocals();
        assertEquals(6, allParentChildLocals.size());
        soot.PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        assertTrue(pointsToAnalysis instanceof PAG);
        PointsToSet ptaSet1 = pointsToAnalysis.reachingObjects(allParentChildLocals.get(0).getO1());
        PointsToSet ptaSet2 = pointsToAnalysis.reachingObjects(allParentChildLocals.get(1).getO1());
        PointsToSet ptaSet4 = pointsToAnalysis.reachingObjects(allParentChildLocals.get(3).getO1());
        PointsToSet ptaSet6 = pointsToAnalysis.reachingObjects(allParentChildLocals.get(5).getO1());
        assertTrue(ptaSet1 instanceof DoublePointsToSet);
        assertTrue(ptaSet1.hasNonEmptyIntersection(ptaSet2));
        assertFalse(ptaSet1.hasNonEmptyIntersection(ptaSet4));
        assertTrue(ptaSet1.hasNonEmptyIntersection(ptaSet6));
        assertFalse(ptaSet2.hasNonEmptyIntersection(ptaSet6));
        assertFalse(ptaSet4.hasNonEmptyIntersection(ptaSet6));
    }


}
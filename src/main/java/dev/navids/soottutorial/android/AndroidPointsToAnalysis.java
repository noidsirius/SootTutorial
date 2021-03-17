package dev.navids.soottutorial.android;

import dev.navids.soottutorial.visual.AndroidCallGraphFilter;
import dev.navids.soottutorial.visual.Visualizer;
import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.sets.DoublePointsToSet;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AndroidPointsToAnalysis {
    private final static String USER_HOME = System.getProperty("user.home");
    private static String androidJar = USER_HOME + "/Library/Android/sdk/platforms";
    static String androidDemoPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "Android";
    static String apkPath = androidDemoPath + File.separator + "/st_demo.apk";
    static String classParentName = "dev.navids.multicomp1.ClassParent";
    static String classChildName = "dev.navids.multicomp1.ClassChild";
    static String intermediaryMethodSignature = "<dev.navids.multicomp1.MyReceiver: void intermediaryMethod()>";

    static boolean isParentChildClassLocal(Local local){
        return local.getType().toString().equals(classParentName) || local.getType().toString().equals(classChildName);
    }

    public static void main(String[] args){
        SetupApplication app = new SetupApplication(AndroidUtil.getFlowDroidConfig(apkPath, androidJar));
        // By constructing call graph, PointsTo analysis implicitly will be executed
        app.constructCallgraph();
        soot.PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        SootMethod intermediaryMethod = Scene.v().getMethod(intermediaryMethodSignature);
        for(Local local : intermediaryMethod.getActiveBody().getLocals()){
            if(isParentChildClassLocal(local)){
                ((DoublePointsToSet)pointsToAnalysis.reachingObjects(local)).getOldSet().forall(new P2SetVisitor() {
                    @Override
                    public void visit(Node n) {
                        System.out.println(String.format("Local %s in intermediaryMethod is allocated at %s", local, n));
                    }
                });
            }
        }
        System.out.println("----------");

        // Reporting aliases relation among all local values with type ClassParent or ClassChild
        List<Pair<Local, String>> allParentChildLocals = getParentChildClassLocals();
        String header = "\t";
        for(int i=0; i< allParentChildLocals.size(); i++) {
            System.out.println(String.format("Local %d: %s", i + 1, allParentChildLocals.get(i).getO2()));
            header += (i+1)+"\t";
        }
        System.out.println("----------");
        System.out.println("Aliases (1 -> the locals on row and column MAY points to the same memory location, 0 -> otherwise)");
        System.out.println(header);
        for(int i=0; i< allParentChildLocals.size(); i++){
            Local leftLocal = allParentChildLocals.get(i).getO1();
            String row = (i+1) + "\t";
            PointsToSet leftSet = pointsToAnalysis.reachingObjects(leftLocal);
            for(int j=0; j< allParentChildLocals.size(); j++) {
                Local rightLocal = allParentChildLocals.get(j).getO1();
                PointsToSet rightSet = pointsToAnalysis.reachingObjects(rightLocal);
                row += (leftSet.hasNonEmptyIntersection(rightSet)? "1" : "0") +"\t";
            }
            System.out.println(row);
        }

    }

    public static List<Pair<Local, String>> getParentChildClassLocals() {
        List<Pair<Local, String>> allParentChildLocals = new ArrayList<>();
        for(SootClass sootClass : Scene.v().getApplicationClasses()){
            for(SootMethod sootMethod : sootClass.getMethods()){
                if(!sootMethod.hasActiveBody())
                    continue;
                for(Unit unit : sootMethod.getActiveBody().getUnits()){
                    if (unit instanceof InvokeStmt){
                        InvokeStmt invokeStmt = (InvokeStmt) unit;
                        if(invokeStmt.getInvokeExpr() instanceof VirtualInvokeExpr){
                            VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeStmt.getInvokeExpr();
                            Local baseLocal = (Local) virtualInvokeExpr.getBase();
                            if(isParentChildClassLocal(baseLocal)){
                                String label = String.format("%s.%s {%s}", sootClass.getShortName(), sootMethod.getName(), virtualInvokeExpr);
                                allParentChildLocals.add(new Pair<>(baseLocal, label));
                            }

                        }
                    }
                }
            }
        }
        return allParentChildLocals;
    }

}

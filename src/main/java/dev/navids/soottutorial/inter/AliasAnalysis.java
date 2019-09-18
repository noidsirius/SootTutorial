package dev.navids.soottutorial.inter;

import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.options.Options;
import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.util.*;

public class AliasAnalysis {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "InterAnalysis";
    public static String clsName = "AliasExample";

    public static void setupSoot() {
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_output_format(Options.output_format_jimp);
//        Options.v().set_prepend_classpath(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);
        Options.v().set_whole_program(true);
//        Map<String, String> sparksOptions = new HashMap<>();
//        HashMap sparksOptions = new HashMap();
//        sparksOptions.put("enabled","true");
//        sparksOptions.put("verbose","true");
//        sparksOptions.put("ignore-types","false");
//        sparksOptions.put("force-gc","false");
//        sparksOptions.put("pre-jimplify","false");
//        sparksOptions.put("vta","false");
//        sparksOptions.put("rta","false");
//        sparksOptions.put("field-based","false");
//        sparksOptions.put("types-for-sites","false");
//        sparksOptions.put("merge-stringbuffer","true");
//        sparksOptions.put("string-constants","false");
//        sparksOptions.put("simulate-natives","true");
//        sparksOptions.put("simple-edges-bidirectional","false");
//        sparksOptions.put("on-fly-cg","true");
//        sparksOptions.put("simplify-offline","false");
//        sparksOptions.put("simplify-sccs","false");
//        sparksOptions.put("ignore-types-for-sccs","false");
//        sparksOptions.put("propagator","worklist");
//        sparksOptions.put("set-impl","double");
//        sparksOptions.put("double-set-old","hybrid");
//        sparksOptions.put("double-set-new","hybrid");
//        sparksOptions.put("dump-html","false");
//        sparksOptions.put("dump-pag","false");
//        sparksOptions.put("dump-solution","false");
//        sparksOptions.put("topo-sort","false");
//        sparksOptions.put("dump-types","true");
//        sparksOptions.put("class-method-var","true");
//        sparksOptions.put("dump-answer","false");
//        sparksOptions.put("add-tags","false");
//        sparksOptions.put("set-mass","false");
//
//        SparkTransformer.v().transform("",sparksOptions);
        Options.v().setPhaseOption("cg", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "propagator:worklist");
        Options.v().setPhaseOption("cg.spark", "simple-edges-bidirectional:false");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
        Options.v().setPhaseOption("cg.spark", "set-impl:double");
        Options.v().setPhaseOption("cg.spark", "double-set-old:hybrid");
        Options.v().setPhaseOption("cg.spark", "double-set-new:hybrid");

//        Options.v().setPhaseOption("spark", "enabled:true");

        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();

        Scene.v().loadNecessaryClasses();
//        System.out.println(sc.getMethods());
        Scene.v().setEntryPoints(Collections.singletonList(sc.getMethod("void main()")));
        PackManager.v().runPacks();


    }


    public static void main(String[] args) {
        setupSoot();
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        SootClass mainClass = Scene.v().getSootClass(clsName);
        for (SootMethod sm : mainClass.getMethods()) {
            JimpleBody body = (JimpleBody) sm.retrieveActiveBody();
            for(Local l : body.getLocals()){
                PointsToSetInternal pointsToSet = (PointsToSetInternal)pointsToAnalysis.reachingObjects(l);
                pointsToSet.forall(new P2SetVisitor() {
                    @Override
                    public void visit(Node node) {
                        System.out.println(l + " " + node);
                    }
                });
            }
        }
        System.out.println(Scene.v().getApplicationClasses().size());
    }
}

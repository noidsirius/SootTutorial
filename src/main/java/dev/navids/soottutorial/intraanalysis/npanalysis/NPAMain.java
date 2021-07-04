package dev.navids.soottutorial.intraanalysis.npanalysis;

import dev.navids.soottutorial.visual.Visualizer;
import soot.*;
import soot.jimple.JimpleBody;
import soot.options.Options;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;

public class NPAMain {
    private final static String parameterMessage = "Please provide arguments, 1: MethodName (A, B, C), 2: Analysis Mode (MAY, MAY_P, MUST), 3: Visualize ('viz') ";
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "IntraAnalysis";
    public static String clsName = "NullPointerExample";

    public static void setupSoot() {
        G.reset();
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_prepend_classpath(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);
        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
    }


    public static void main(String[] args) {
        setupSoot();
        String methodLabel = "B";
        NullPointerAnalysis.AnalysisMode analysisMode = NullPointerAnalysis.AnalysisMode.MAY;
        if (args.length >= 1) {
            if (!"ABC".contains(args[0])) {
                System.out.println(parameterMessage);
                return;
            }
            methodLabel = args[0];
        }
        if (args.length >= 2) {
            switch (args[1]) {
                case "MAY":
                    analysisMode = NullPointerAnalysis.AnalysisMode.MAY;
                    break;
                case "MUST":
                    analysisMode = NullPointerAnalysis.AnalysisMode.MUST;
                    break;
                case "MAY_P":
                    analysisMode = NullPointerAnalysis.AnalysisMode.MAY_P;
                    break;
                default:
                    System.out.println(parameterMessage);
                    return;
            }
        }
        boolean visualize = (args.length >= 3 && args[2].equals("viz"));
        SootMethod sm = Scene.v().grabMethod(String.format("<NullPointerExample: void method%s()>", methodLabel));
        JimpleBody body = (JimpleBody) sm.retrieveActiveBody();
        UnitGraph unitGraph = new TrapUnitGraph(body);
        NullPointerAnalysis npa = new NullPointerAnalysis(unitGraph, analysisMode, body);
        if (visualize)
            Visualizer.v().animateIntraGraph(unitGraph, npa);
        System.out.println("NullPointerException Warnings:");
        for (Unit unit : body.getUnits()) {
            for (ValueBox usedValueBox : unit.getUseBoxes()) {
                if (usedValueBox.getValue() instanceof Local) {
                    Local usedLocal = (Local) usedValueBox.getValue();
                    if (npa.getFlowBefore(unit).contains(usedLocal)) {
                        System.out.println("    Line " + unit.getJavaSourceStartLineNumber() + ": " + npa.analysisMode + " NullPointer usage of local " + usedLocal + " in unit " + unit);
                    }
                }
            }
        }
    }
}

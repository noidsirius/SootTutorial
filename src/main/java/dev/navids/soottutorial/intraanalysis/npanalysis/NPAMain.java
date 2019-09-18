package dev.navids.soottutorial.intraanalysis.npanalysis;

import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.options.Options;
import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NPAMain {
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
        SootClass mainClass = Scene.v().getSootClass(clsName);
        for (SootMethod sm : mainClass.getMethods()) {
            System.out.println("Method: " + sm.getSignature());
            JimpleBody body = (JimpleBody) sm.retrieveActiveBody();
            UnitGraph unitGraph = new TrapUnitGraph(body);
            List<NullPointerAnalysis> npAnalyzers = new ArrayList<>();
            npAnalyzers.add(new NullPointerAnalysis(unitGraph, NullPointerAnalysis.AnalysisMode.MUST));
            npAnalyzers.add(new NullPointerAnalysis(unitGraph, NullPointerAnalysis.AnalysisMode.MAY_O));
            npAnalyzers.add(new NullPointerAnalysis(unitGraph, NullPointerAnalysis.AnalysisMode.MAY_P));
            int c = 0;
            for(Unit unit : body.getUnits()){
                c++;
//                System.out.println("("+c+") " + unit + " --- " + mayNPAnalysis.getFlowBefore(unit) + " " + mustNPAnalysis.getFlowBefore(unit));
//                if(nullPointerAnalysis.getFlowBefore(unit).size() == 0)
//                    continue;
                for(ValueBox usedValueBox : unit.getUseBoxes()){
                    if(usedValueBox.getValue() instanceof Local){
                        Local usedLocal = (Local) usedValueBox.getValue();
                        for(NullPointerAnalysis npa: npAnalyzers){
                            if(npa.getFlowBefore(unit).contains(usedLocal)){
                                System.out.println("    Line " + unit.getJavaSourceStartLineNumber() +": " + npa.analysisMode + " NullPointer usage of local " + usedLocal + " in unit " + unit);
                            }
                        }

//                        if(mustNPAnalysis.getFlowBefore(unit).contains(usedLocal)){
//                            System.out.println("    Line " + unit.getJavaSourceStartLineNumber() +": MUST NullPointer usage of local " + usedLocal + " in unit (" + c +") " + unit);
//                        }
                    }
                    if(unit instanceof InvokeStmt && usedValueBox.getValue().getType().equals(NullType.v())){
                        System.out.println("    Line " + unit.getJavaSourceStartLineNumber() +": MUST NullPointer usage in unit (" + c +") " + unit);
                    }
                }
            }
        }
    }
}

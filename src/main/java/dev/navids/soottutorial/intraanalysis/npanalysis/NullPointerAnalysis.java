package dev.navids.soottutorial.intraanalysis.npanalysis;


import soot.Local;
import soot.Unit;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NullPointerAnalysis extends ForwardFlowAnalysis<Unit, NullFlowSet> {

    enum AnalysisMode {
        MUST,
        MAY_P,
        MAY
    }
    public AnalysisMode analysisMode;
    Map<Integer, Unit> outNFSToUnit = new HashMap<>();
    public List<Pair<Unit, String>> messages = new ArrayList<>();
    JimpleBody body;
    public NullPointerAnalysis(DirectedGraph graph, AnalysisMode analysisMode, JimpleBody body) {
        super(graph);
        this.body = body;
        this.analysisMode = analysisMode;
        doAnalysis();
    }

    @Override
    protected void flowThrough(NullFlowSet inSet, Unit unit, NullFlowSet outSet) {
        outNFSToUnit.put(outSet.id, unit);
        inSet.copy(outSet);
        kill(inSet, unit, outSet);
        generate(inSet, unit, outSet);
        messages.add(new Pair<>(unit, outSet.toString()));
    }

    @Override
    protected NullFlowSet newInitialFlow() {
        return new NullFlowSet();
    }


    @Override
    protected void merge(NullFlowSet inSet1, NullFlowSet inSet2, NullFlowSet outSet) {
        Unit possibleSrcUnit = null;
        if(outNFSToUnit.containsKey(inSet1.id))
            possibleSrcUnit = outNFSToUnit.get(inSet1.id);
        else if(outNFSToUnit.containsKey(inSet2.id))
            possibleSrcUnit = outNFSToUnit.get(inSet2.id);
        else if(inSet1.parents.size() > 0){
            for(int i=inSet1.parents.size()-1; i>=0; i--)
                if(outNFSToUnit.containsKey(inSet1.parents.get(i).id))
                    possibleSrcUnit = outNFSToUnit.get(inSet1.parents.get(i).id);
        }
        else if(inSet2.parents.size() > 0){
            for(int i=inSet2.parents.size()-1; i>=0; i--)
                if(outNFSToUnit.containsKey(inSet2.parents.get(i).id))
                    possibleSrcUnit = outNFSToUnit.get(inSet2.parents.get(i).id);
        }
        Unit possibleTgtUnit = null;
        if(possibleSrcUnit != null && graph.getSuccsOf(possibleSrcUnit).size() == 1)
            possibleTgtUnit = graph.getSuccsOf(possibleSrcUnit).get(0);
        if(analysisMode != AnalysisMode.MUST)
            inSet1.union(inSet2, outSet);
        else
            inSet1.intersection(inSet2, outSet);
        messages.add(new Pair<>(possibleTgtUnit, outSet.toString() + " (M)"));
    }

    @Override
    protected void copy(NullFlowSet source, NullFlowSet dest) {
        if(source != dest)
            dest.parents.add(source);
        source.copy(dest);
    }

    protected void kill(NullFlowSet inSet, Unit unit, NullFlowSet outSet){
        unit.apply(new AbstractStmtSwitch() {
            @Override
            public void caseAssignStmt(AssignStmt stmt) {
                Local leftOp = (Local) stmt.getLeftOp();
                outSet.remove(leftOp);
            }
        });
    }

    protected void generate(NullFlowSet inSet, Unit unit, NullFlowSet outSet){
        unit.apply(new AbstractStmtSwitch() {
            @Override
            public void caseAssignStmt(AssignStmt stmt) {
                Local leftOp = (Local) stmt.getLeftOp();
                stmt.getRightOp().apply(new AbstractJimpleValueSwitch() {
                    @Override
                    public void caseLocal(Local v) {
                        if (inSet.contains(v))
                            outSet.add(leftOp);
                    }

                    @Override
                    public void caseNullConstant(NullConstant v) {
                        outSet.add(leftOp);
                    }

                    @Override
                    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
                        if(analysisMode == AnalysisMode.MAY_P)
                            outSet.add(leftOp);
                    }

                    @Override
                    public void caseStaticInvokeExpr(StaticInvokeExpr v) {
                        if(analysisMode == AnalysisMode.MAY_P)
                            outSet.add(leftOp);
                    }

                    @Override
                    public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
                        if(analysisMode == AnalysisMode.MAY_P)
                            outSet.add(leftOp);
                    }
                });
            }

            @Override
            public void caseIdentityStmt(IdentityStmt stmt) {

                Local leftOp = (Local) stmt.getLeftOp();
                if(analysisMode == AnalysisMode.MAY_P)
                    if(!(stmt.getRightOp() instanceof ThisRef))
                        outSet.add(leftOp);
            }
        });
    }

}


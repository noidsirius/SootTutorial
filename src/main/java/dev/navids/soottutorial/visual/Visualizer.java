package dev.navids.soottutorial.visual;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;
import soot.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;

import java.util.*;

public class Visualizer {
    public static Visualizer instance;
    public static Visualizer v() {
        if(instance == null)
            instance = new Visualizer();
        return instance;
    }

    private Graph graph;
    private SpriteManager sman;
    private Viewer viewer;
    private final static String[] colors = new String[]{"red", "blue", "darkgoldenrod", "magenta", "purple", "mediumaquamarine", "brown", "seagreen", "darkslategrey", "indianred"};
    private Visualizer(){
        if (graph != null)
            graph.clear();
        if(viewer != null)
            viewer.close();

        graph = new SingleGraph("Soot");
        sman = new SpriteManager(graph);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        String GCSS = "";
        GCSS += "node {\n" +
                "}\n";
        GCSS += "node.cfg_node {\n" +
                "\tsize-mode: fit;\n"+
                "\tshape: rounded-box;\n"+
                "\tstroke-mode: plain;\n"+
                "\tpadding: 5px, 5px;\n"+
                "\ttext-size: 16;\n" +
                "\ttext-color: white;\n" +
                "}\n";
        GCSS += "node.cfg_branch {\n" +
                "\tfill-color: blue;\n" +
                "}\n";
        GCSS += "node.cfg_terminal {\n" +
                "\tfill-color: rgb(00,74,65);\n" +
                "}\n";
        for(String color : colors) {
            GCSS += "node."+color+" {\n" +
                    "\ttext-color: "+color+";\n" +
                    "\tshape: rounded-box;\n"+
                    "\tfill-color: white;\n"+
                    "}\n";
        }
        GCSS += "node.default_color {\n" +
                "\ttext-color: black;\n" +
                "\tshape: rounded-box;\n"+
                "\tfill-color: white;\n"+
                "}\n";
        GCSS += "node.cg_lib_class {\n" +
                "\tfill-color: blue;\n" +
                "\ttext-color: white;\n" +
                "\tshape: rounded-box;\n"+
                "}\n";
        GCSS += "node.cg_node {\n" +
                "\tsize-mode: fit;\n"+
                "\tstroke-mode: plain;\n"+
                "\tpadding: 5px, 5px;\n"+
                "\ttext-size: 16;\n" +
                "}\n";
        GCSS += "node.cg_dummy_node {\n" +
                "\tshape: box;\n" +
                "\tfill-color: darkslateblue;\n"+
                "\ttext-color: white;\n" +
                "}\n";
        GCSS += "edge {\n" +
                "\ttext-alignment: along;\n" +
                "\ttext-background-mode: plain;\n" +
                "\ttext-size: 16;\n" +
                "}\n";
        GCSS += "edge.cg {\n" +
                "\tarrow-size: 16;\n" +
                "\tshape: cubic-curve;\n" +
                "}\n";
        graph.setAttribute("ui.stylesheet", GCSS);
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public void addUnitGraph(UnitGraph unitGraph){
        addUnitGraph(unitGraph, true);
    }

    public void addUnitGraph(UnitGraph unitGraph, boolean indexLabel){

        int index = 0;
        for (Unit unit : unitGraph) {
            index += 1;
            String aid = getID(unit);
            Node aNode = graph.addNode(aid);
            String label = indexLabel ? String.valueOf(index) : getLabelFromUnit(unit);
            if (unit instanceof JIfStmt)
                aNode.addAttribute("ui.class", "cfg_node, cfg_branch");
            else if (unitGraph.getHeads().contains(unit))
                aNode.addAttribute("ui.class", "cfg_node, cfg_terminal");
            else if (unitGraph.getTails().contains(unit))
                aNode.addAttribute("ui.class", "cfg_node, cfg_terminal");
            else
                aNode.addAttribute("ui.class", "cfg_node");
            aNode.setAttribute("ui.label", label);
        }
        for (Unit unit : unitGraph) {
            for (Unit suc : unitGraph.getSuccsOf(unit)) {
                String aid = getID(unit);
                String bid = getID(suc);
                graph.addEdge(aid + bid, aid, bid, true);
            }
        }
        Unit header = unitGraph.getHeads().get(0);
        String hid = getID(header);
        Node head = graph.getNode(hid);
        int x = 50, y = 2000;
        int c = 1;
        Random random = new Random();
        for(Iterator<Node> it = head.getBreadthFirstIterator(true); it.hasNext();){
            Node node = it.next();
            node.setAttribute("xyz", x, y, 0);
            x += 50 + random.nextInt(50)-25;
            y -= c * 50 + random.nextInt(50);
            c *= -1;
        }
    }

    private String getLabelFromUnit(Unit unit) {
        StringBuilder label;
        label = new StringBuilder(unit.toString());
        if (unit instanceof JAssignStmt){
            JAssignStmt jAssignStmt = (JAssignStmt) unit;
            if(jAssignStmt.containsInvokeExpr() && jAssignStmt.getInvokeExpr() instanceof JVirtualInvokeExpr){
                label = new StringBuilder(jAssignStmt.getLeftOp().toString() + "=");
                JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) jAssignStmt.getInvokeExpr();
                label.append(jVirtualInvokeExpr.getBase()).append(".").append(jVirtualInvokeExpr.getMethod().getName());
                if(jVirtualInvokeExpr.getArgCount() > 0){
                    label.append("(");
                    for(Value v : jVirtualInvokeExpr.getArgs()){
                        label.append(v.toString()).append(",");
                    }
                    label = new StringBuilder(label.substring(0, label.length() - 1));
                    label.append(")");
                }
            }
        }
        if (unit instanceof JInvokeStmt){
            JInvokeStmt jInvokeStmt = (JInvokeStmt) unit;
            if(jInvokeStmt.containsInvokeExpr() && jInvokeStmt.getInvokeExpr() instanceof JVirtualInvokeExpr){
                JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) jInvokeStmt.getInvokeExpr();
                label = new StringBuilder(jVirtualInvokeExpr.getBase() + "." + jVirtualInvokeExpr.getMethod().getName());
                if(jVirtualInvokeExpr.getArgCount() > 0){
                    label.append("(");
                    for(Value v : jVirtualInvokeExpr.getArgs()){
                        label.append(v.toString()).append(",");
                    }
                    label = new StringBuilder(label.substring(0, label.length() - 1));
                    label.append(")");
                }
            }
        }
        label = new StringBuilder(label.toString().replace("java.lang.", "").replace("java.io.", ""));
        return label.toString();
    }

    public interface NodeAttributeConfig {
        Pair<String, String> getClassLabelPair(SootMethod sootMethod);
    }

    private static class DefaultNodeAttributeConfig implements NodeAttributeConfig{
        @Override
        public Pair<String, String> getClassLabelPair(SootMethod sootMethod) {
            return new Pair<>("cg_node, default_color", sootMethod.getSignature());
        }
    }

    public static class AndroidNodeAttributeConfig implements NodeAttributeConfig{
        List<String> visitedClasses = new ArrayList<>();
        boolean coloredNode;

        public AndroidNodeAttributeConfig(boolean coloredNodes) {
            this.coloredNode = coloredNodes;
        }

        @Override
        public Pair<String, String> getClassLabelPair(SootMethod sootMethod) {
            Pair<String, String> nodeClassLabelPair = new Pair<>();
            String uiClass = "cg_node";
            String nodeLabel = sootMethod.getDeclaringClass().getShortName() + "." + sootMethod.getName();
            if(sootMethod.getDeclaringClass().toString().contains("dummy")) {
                uiClass = "cg_node, cg_dummy_node";
                String[] parts = sootMethod.getName().split("_");
                nodeLabel = parts[parts.length-1];
            }
            else if(coloredNode) {
                String clsName = sootMethod.getDeclaringClass().toString();
                if (!visitedClasses.contains(clsName))
                    visitedClasses.add(clsName);
                int clsIndex = visitedClasses.indexOf(clsName);
                if(clsIndex < colors.length)
                    uiClass += ", " + colors[clsIndex];
                else
                    uiClass += ", default_color";
            }
            else{
                uiClass += ", default_color";
            }
            nodeClassLabelPair.setO1(uiClass);
            nodeClassLabelPair.setO2(nodeLabel);
            return nodeClassLabelPair;
        }
    }

    public void addCallGraph(CallGraph callGraph, CallGraphFilter cgFilter, NodeAttributeConfig nodeAttributeConfig){
        if(nodeAttributeConfig == null)
            nodeAttributeConfig = new DefaultNodeAttributeConfig();
        for (soot.jimple.toolkits.callgraph.Edge edge : callGraph) {
            try {
                if (cgFilter != null && !cgFilter.isValidEdge(edge))
                    continue;
                String aid = getID(edge.src());
                String bid = getID(edge.tgt());
                if (hasEdge(aid + bid + getID(edge.srcUnit())))
                    continue;
                if (!hasNode(aid)) {
                    Node aNode = graph.addNode(aid);
                    Pair<String, String> nodeClassLabelPair = nodeAttributeConfig.getClassLabelPair(edge.src());
                    aNode.addAttribute("ui.class", nodeClassLabelPair.getO1());
                    aNode.setAttribute("ui.label", nodeClassLabelPair.getO2());
                }
                if (!hasNode(bid)) {
                    Node bNode = graph.addNode(bid);
                    Pair<String, String> nodeClassLabelPair = nodeAttributeConfig.getClassLabelPair(edge.tgt());
                    bNode.addAttribute("ui.class", nodeClassLabelPair.getO1());
                    bNode.setAttribute("ui.label", nodeClassLabelPair.getO2());
                }
                Edge e = graph.addEdge(aid + bid + getID(edge.srcUnit()), aid, bid, true);
                e.addAttribute("ui.class", "cg");
            } catch (Exception exc) {
                System.err.println(exc.getMessage());
            }
        }

    }

    private boolean hasNode(String id){
        try {
            Node node = graph.getNode(id);
            return node != null;
        }catch (Exception e){

        }
        return false;
    }

    private boolean hasEdge(String id){
        try {
            Edge edge = graph.getEdge(id);
            return edge != null;
        }catch (Exception ignored){

        }
        return false;
    }

    public void draw(){
        if(viewer != null)
            viewer.close();
        viewer = graph.display();
//        View view = viewer.getDefaultView();
        Layout layout = new LinLog();
        viewer.enableAutoLayout(layout);
//        layout.setStabilizationLimit(10);
    }

    public void close(){
        if (viewer != null)
            viewer.close();
        if (graph != null)
            graph.clear();
        graph = null;
        sman = null;
    }

    private String getID(Object a){
//        String id = Integer.toString(System.identityHashCode(a));
        String id = Integer.toString(a.hashCode());
        StringBuilder newId = new StringBuilder();
        for(int i=0; i< id.length(); i++){
            newId.append((char) (97 + id.codePointAt(i) - "0".codePointAt(0)));
        }
        return newId.toString();
    }


}

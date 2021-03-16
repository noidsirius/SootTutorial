package dev.navids.soottutorial.visual;

public interface CallGraphFilter {
    boolean isValidEdge(soot.jimple.toolkits.callgraph.Edge edge);
}

package dev.navids.soottutorial.intraanalysis.npanalysis;

import soot.Local;
import soot.toolkits.scalar.AbstractBoundedFlowSet;

import java.util.*;
import java.util.stream.Collectors;

public class NullFlowSet extends AbstractBoundedFlowSet<Local> {

    private Set<Local> nullLocals = new HashSet<>();
    private static int counter = 0;
    int id = -1;
    List<NullFlowSet> parents = new ArrayList<>();
    public NullFlowSet() {
        super();
        id = counter++;
    }

    @Override
    public void clear() {
        nullLocals.clear();
    }

    @Override
    public NullFlowSet clone() {
        NullFlowSet myClone = new NullFlowSet();
        myClone.nullLocals.addAll(this.nullLocals);
        return myClone;
    }

    @Override
    public boolean isEmpty() {
        return nullLocals.isEmpty();
    }

    @Override
    public int size() {
        return nullLocals.size();
    }

    @Override
    public void add(Local local) {
        nullLocals.add(local);
    }

    @Override
    public void remove(Local local) {
        if(nullLocals.contains(local))
            nullLocals.remove(local);
    }

    @Override
    public boolean contains(Local local) {
        return nullLocals.contains(local);
    }

    @Override
    public Iterator<Local> iterator() {
        return nullLocals.iterator();
    }

    @Override
    public List<Local> toList() {
        return new ArrayList<>(nullLocals);
    }

    @Override
    public String toString() {
        List<Local> locals = new ArrayList<>(nullLocals);
        locals.sort(Comparator.comparing(Local::getName));
        return locals.stream().map(Local::getName).collect(Collectors.joining(","));
    }
}

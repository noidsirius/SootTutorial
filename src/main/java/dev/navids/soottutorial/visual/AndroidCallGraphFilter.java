package dev.navids.soottutorial.visual;

import dev.navids.soottutorial.android.AndroidUtils;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;

public class AndroidCallGraphFilter implements CallGraphFilter {
    public List<SootClass> getValidClasses() {
        return validClasses;
    }

    private List<SootClass> validClasses = new ArrayList<>();
    public AndroidCallGraphFilter(String appPackageName) {
        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            if (!sootClass.getName().contains(appPackageName))
                continue;
            if (sootClass.getName().contains(appPackageName + ".R") || sootClass.getName().contains(appPackageName + ".BuildConfig"))
                continue;
            validClasses.add(sootClass);
        }
    }

    private boolean isValidMethod(SootMethod sootMethod){
        if(AndroidUtils.isAndroidMethod(sootMethod))
            return false;
        if(sootMethod.getDeclaringClass().getPackageName().startsWith("java"))
            return false;
        if(sootMethod.toString().contains("<init>") || sootMethod.toString().contains("<clinit>"))
            return false;
        if(sootMethod.getName().equals("dummyMainMethod"))
            return false;
        return true;
    }

    @Override
    public boolean isValidEdge(soot.jimple.toolkits.callgraph.Edge sEdge) {
        if(!sEdge.src().getDeclaringClass().isApplicationClass())// || sEdge.tgt().getDeclaringClass().isApplicationClass())
            return false;
        if(!isValidMethod(sEdge.src()) || !isValidMethod(sEdge.tgt()))
            return false;
        boolean flag = validClasses.contains(sEdge.src().getDeclaringClass());
        flag |= validClasses.contains(sEdge.tgt().getDeclaringClass());
        return flag;
    }
}

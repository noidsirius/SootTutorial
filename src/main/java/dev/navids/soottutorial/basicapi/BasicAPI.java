package dev.navids.soottutorial.basicapi;

import soot.*;
import soot.jimple.*;
import soot.options.Options;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BasicAPI {

    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "BasicAPI";
    public static String circleClassName = "Circle";

    public static void setupSoot() {
        G.reset();
        Options.v().set_prepend_classpath(true);
// Uncomment line below to allow phantom references.
//        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();
    }

    public static void main(String[] args) {
        setupSoot();
        // Access to classes
        System.out.println("-----Class-----");
        SootClass circleClass = Scene.v().getSootClass(circleClassName);
        System.out.println(String.format("The class %s is loaded with %d methods! ", circleClass.getName(), circleClass.getMethodCount()));
        String wrongClassName = "Circrle";
        SootClass notExistedClass = Scene.v().getSootClassUnsafe(wrongClassName);
        System.out.println(String.format("getClassUnsafe: Is the class %s null? %b", wrongClassName, notExistedClass==null));
        try{
            notExistedClass = Scene.v().getSootClass(wrongClassName);
            System.out.println(String.format("getClass creates a phantom class for %s: %b", wrongClassName, notExistedClass.isPhantom()));
        }catch (Exception exception){
            System.out.println(String.format("getClass throws an exception for class %s.", wrongClassName));
        }
        // Access to methods
        System.out.println(String.format("List of %s's methods:", circleClass.getName()));
        for(SootMethod sootMethod : circleClass.getMethods())
            System.out.println(String.format("- %s",sootMethod.getName()));
        System.out.println("-----Method-----");
        SootMethod getCircleCountMethod = circleClass.getMethod("int getCircleCount()");
        System.out.println(String.format("Method Signature: %s", getCircleCountMethod.getSignature()));
        System.out.println(String.format("Method Subsignature: %s", getCircleCountMethod.getSubSignature()));
        System.out.println(String.format("Method Name: %s", getCircleCountMethod.getName()));
        System.out.println(String.format("Declaring class: %s", getCircleCountMethod.getDeclaringClass()));
        int modifers = getCircleCountMethod.getModifiers();
        System.out.println(String.format("Method %s is public: %b, is static: %b, is final: %b", getCircleCountMethod.getName(),
                                        Modifier.isPublic(modifers),
                                        Modifier.isStatic(modifers),
                                        Modifier.isFinal(modifers)));
        SootMethod constructorMethod = circleClass.getMethodByName("<init>");
        try{
            SootMethod areaMethod = circleClass.getMethodByName("area");
        }
        catch (Exception exception){
            System.out.println("Th method 'area' is overloaded and Soot cannot retrieve it by name");
        }
        SootMethod areaMethod = circleClass.getMethod("int area(boolean)");
        // Access to body (units, locals)
        System.out.println("-----Body-----");
        JimpleBody body = (JimpleBody) areaMethod.getActiveBody();
        System.out.println(String.format("Local variables count: %d", body.getLocalCount()));
        for (Unit u : body.getUnits()) {
            Stmt stmt = (Stmt) u;
            System.out.println("Stmt: " + stmt);
            if(stmt.containsInvokeExpr()){
                InvokeExpr invokeExpr = stmt.getInvokeExpr();
                invokeExpr.apply(new AbstractJimpleValueSwitch() {
                    @Override
                    public void caseStaticInvokeExpr(StaticInvokeExpr v) {
                        System.out.println(String.format("    StaticInvokeExpr '%s' from class '%s'", v, v.getType()));
                    }

                    @Override
                    public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
                        System.out.println(String.format("    VirtualInvokeExpr '%s' from local '%s' with type %s", v, v.getBase(), v.getBase().getType()));
                    }

                    @Override
                    public void defaultCase(Object v) {
                        super.defaultCase(v);
                    }
                });
            }
            stmt.apply(new AbstractStmtSwitch() {
                @Override
                public void caseIfStmt(IfStmt stmt) {
                    System.out.println(String.format("    (Before change) if condition '%s' is true goes to stmt '%s'", stmt.getCondition(), stmt.getTarget()));
                    stmt.setTarget(body.getUnits().getSuccOf(u));
                    System.out.println(String.format("    (After change) if condition '%s' is true goes to stmt '%s'", stmt.getCondition(), stmt.getTarget()));
                }
            });
        }
        for(Trap trap : body.getTraps()){
            System.out.println(trap);
        }

        try {
            body.validate();
            System.out.println("Body is validated! No inconsistency found.");
        }
        catch (Exception exception){
            System.out.println("Body is not validated!");
        }

    }

}

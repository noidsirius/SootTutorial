package dev.navids.soottutorial.android;

import org.junit.Before;
import org.junit.Test;
import soot.*;

import java.io.File;

import static org.junit.Assert.*;
public class AndroidInstrumentTest {

    private String packageName;
    private String incNLogMethodSignature="<com.numix.calculator.SootTutorialStaticCounter: void incrementAndLog()>";
    private String anAPKMethodSignature="<com.numix.calculator.History: void write(java.io.DataOutput)>";
    @Before
    public void setUp(){

        String androidJar = System.getProperty("user.home") + "/Library/Android/sdk/platforms";
        if(System.getenv().containsKey("ANDROID_HOME"))
            androidJar = System.getenv("ANDROID_HOME")+ File.separator+"platforms";
        String apkPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "Android" + File.separator + "/calc.apk";
        packageName = AndroidUtil.getPackageName(apkPath);
        assertEquals(packageName, "com.numix.calculator");
        InstrumentUtil.setupSoot(androidJar, apkPath, "/tmp");
        assertNotNull(Scene.v().grabMethod(anAPKMethodSignature));
    }

    @Test
    public void testInjectCode(){
        SootClass staticCounterClass = AndroidClassInjector.createCounterClass(packageName);
        assertFalse(Scene.v().getSootClass(staticCounterClass.getName()).isPhantom());

        SootField counterField = AndroidClassInjector.addCounterFieldToClass(staticCounterClass);
        assertTrue(staticCounterClass.declaresField(counterField.getSubSignature()));

        SootMethod incNLogMethod = AndroidClassInjector.addIncNLogMethod(staticCounterClass, counterField);
        assertEquals(incNLogMethod.getSignature(), incNLogMethodSignature);
        Unit lastUnit = incNLogMethod.getActiveBody().getUnits().getLast();
        assertTrue(incNLogMethod.getActiveBody().getUnits().getPredOf(lastUnit).toString().contains(InstrumentUtil.TAG));
    }

    @Test
    public void testTransformer(){
        SootMethod incNLogMethod = AndroidClassInjector.injectCode(packageName);
        BodyTransformer bodyTransformer = new AndroidClassInjector.StaticMethodCallInjector(incNLogMethod);
        SootMethod anAPKMethod = Scene.v().grabMethod(anAPKMethodSignature);
        bodyTransformer.transform(anAPKMethod.retrieveActiveBody());

        boolean hasIncNLogMethodCall = false;
        boolean hasLogMethodCall = false;
        for(Unit unit : anAPKMethod.getActiveBody().getUnits()){
            if(unit.toString().contains(incNLogMethodSignature))
                hasIncNLogMethodCall = true;
            if(unit.toString().contains("<android.util.Log: int i(java.lang.String,java.lang.String)>"))
                hasLogMethodCall = true;
        }
        assertTrue(hasLogMethodCall);
        assertTrue(hasIncNLogMethodCall);
    }
}
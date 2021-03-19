package dev.navids.multicomp1;

public class ClassChild extends ClassParent {
    @Override
    public void baseMethod(){
        childMethod();
    }

    public void childMethod(){
        System.out.println("In child");
    }
}

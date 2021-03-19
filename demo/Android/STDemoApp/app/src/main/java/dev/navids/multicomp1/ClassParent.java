package dev.navids.multicomp1;

public class ClassParent {
    public void baseMethod(){}

    public void unreachableMethod(){
        System.out.println("Nobody calls me");
    }
}

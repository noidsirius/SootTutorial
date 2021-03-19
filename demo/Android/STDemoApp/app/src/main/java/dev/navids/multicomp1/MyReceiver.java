package dev.navids.multicomp1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
//    ClassA instanceA = new ClassA();
    ClassParent instanceFromSecondActivity;
    public MyReceiver() {
        instanceFromSecondActivity = SecondActivity.parentInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        intermediaryMethod();
        new ClassParent().baseMethod();
        System.out.println("---- Receive something");
    }

    public void intermediaryMethod(){
        instanceFromSecondActivity.baseMethod();
    }
}

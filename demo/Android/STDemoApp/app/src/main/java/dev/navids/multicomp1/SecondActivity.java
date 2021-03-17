package dev.navids.multicomp1;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    MyReceiver myReceiver = new MyReceiver();
    static ClassParent parentInstance = new ClassParent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentInstance.baseMethod();
        setContentView(R.layout.activity_main2);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(myReceiver, intentFilter);
        findViewById(R.id.use_button).setOnClickListener(new Button3());
    }
    class Button3 implements View.OnClickListener{
        ClassChild childInstance = new ClassChild();
        @Override
        public void onClick(View v) {
            childInstance.baseMethod();
        }
    }
}

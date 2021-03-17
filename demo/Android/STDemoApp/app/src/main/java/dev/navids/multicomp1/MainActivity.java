package dev.navids.multicomp1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    ClassParent childInstance = new ClassChild();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.second_act_button).setOnClickListener(new Button1());
        findViewById(R.id.dialog_button).setOnClickListener( new Button2());
    }

    class Button1 implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        }
    }

    class Button2 implements View.OnClickListener{
        class Dialogue1 implements DialogInterface.OnClickListener{
            ClassChild childInstance = new ClassChild();
            @Override
            public void onClick(DialogInterface dialog, int which) {
                childInstance.baseMethod();
            }
        }
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("A dialog")
                    .setMessage("Call 'baseMethod' through ClassChild?")
                    .setPositiveButton(android.R.string.yes, new Dialogue1())
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        childInstance.baseMethod();
        System.out.println("---- onStart");
    }


}

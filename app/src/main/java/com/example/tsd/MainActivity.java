package com.example.tsd;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAccEl = (Button) findViewById(R.id.buttonAccEl);
        btnAccEl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newAccEl();
            }
        });

        Button btnPackEl = (Button) findViewById(R.id.buttonPackEl);
        btnPackEl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newPackEl();
            }
        });

        Button btnThermoPackEl = (Button) findViewById(R.id.buttonThermoPackEl);
        btnThermoPackEl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newThermoPackEl();
            }
        });

        Button btnAccWire = (Button) findViewById(R.id.buttonAccWire);
        btnAccWire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newAccWire();
            }
        });
    }

    ActivityResultLauncher<Intent> packActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData().hasExtra("id")&& result.getData().hasExtra("nam")) {
                        int id = result.getData().getIntExtra("id",-1);
                        String nam = result.getData().getStringExtra("nam");
                        Intent intent = new Intent(MainActivity.this, ActivityPack.class);
                        intent.putExtra("title","Упаковка электродов");
                        intent.putExtra("id_cex",id);
                        intent.putExtra("cl_op",1);
                        intent.putExtra("cex",nam);
                        startActivity(intent);
                    }
                }
            });

    private void newPackEl() {
        Intent intent = new Intent(MainActivity.this, ActivityCex.class);
        intent.putExtra("title","Упаковка электродов");
        packActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> thermoPackActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData().hasExtra("id")&& result.getData().hasExtra("nam")) {
                        int id = result.getData().getIntExtra("id",-1);
                        String nam = result.getData().getStringExtra("nam");
                        Intent intent = new Intent(MainActivity.this, ActivityPack.class);
                        intent.putExtra("title","Термопак");
                        intent.putExtra("id_cex",id);
                        intent.putExtra("cl_op",2);
                        intent.putExtra("cex",nam);
                        startActivity(intent);
                    }
                }
            });

    private void newThermoPackEl() {
        Intent intent = new Intent(MainActivity.this, ActivityCex.class);
        intent.putExtra("title","Термопак");
        thermoPackActivityResultLauncher.launch(intent);
    }

    private void newAccEl() {
        Intent intent = new Intent(MainActivity.this, ActivityAcc.class);
        intent.putExtra("title","Отправить электроды");
        intent.putExtra("prefix","e");
        startActivity(intent);
    }

    private void newAccWire() {
        Intent intent = new Intent(MainActivity.this, ActivityAcc.class);
        intent.putExtra("title","Отправить проволоку");
        intent.putExtra("prefix","w");
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_F1){
            newAccEl();
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_F2){
            newAccWire();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
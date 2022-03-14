package com.example.tsd;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

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

        Button btnAccWire = (Button) findViewById(R.id.buttonAccWire);
        btnAccWire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newAccWire();
            }
        });
    }

    private void newAccEl() {
        Intent intent = new Intent(MainActivity.this, AccElActivity.class);
        startActivity(intent);
    }

    private void newAccWire() {
        Intent intent = new Intent(MainActivity.this, AccWireActivity.class);
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
package com.example.tsd;

import androidx.appcompat.app.AppCompatActivity;
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
    }

    private void newAccEl() {
        Intent intent = new Intent(MainActivity.this, AccElActivity.class);
        startActivity(intent);
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Toast.makeText(getApplicationContext(), "Нажата кнопка "+event.toString(), Toast.LENGTH_SHORT).show();
        if (event.getKeyCode() == KeyEvent.KEYCODE_4){
            newAccEl();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
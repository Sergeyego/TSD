package com.example.tsd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        //TextView headerView = findViewById(R.id.selectedMenuItem);
        switch(id){
            case R.id.action_acc_el:
                Intent intent = new Intent(this, AccElActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_acc_wire:
                //headerView.setText("Открыть");
                return true;
            case R.id.action_acc_control_el:
                //headerView.setText("Сохранить");
                return true;
            case R.id.action_acc_control_wire:
                //headerView.setText("Сохранить");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
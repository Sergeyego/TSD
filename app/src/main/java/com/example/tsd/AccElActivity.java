package com.example.tsd;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccElActivity extends AppCompatActivity {

    Button btnUpd;
    RecyclerView rv;

    private List<RVAdapter.Acc> accs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_el);

        btnUpd = (Button) findViewById(R.id.btnUpd);

        accs = new ArrayList<>();

        rv = (RecyclerView) findViewById(R.id.rvList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        //RVAdapter adapter = new RVAdapter(accs);
        //rv.setAdapter(adapter);

        btnUpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeGetRequest();
            }
        });
    }

    private void makeGetRequest() {
        new HttpReqGet().execute("http://192.168.1.10:3000/diam");
    }

    private class HttpReqGet extends HttpReq{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(),"resp: "+server_response, Toast.LENGTH_SHORT).show();

            for (int i=0; i<10; i++){
                RVAdapter.Acc a = new RVAdapter.Acc(String.valueOf(i),"Приемка из производства",new Date(),i);
                accs.add(a);
            }

            RVAdapter.OnStateClickListener stateClickListener = new RVAdapter.OnStateClickListener() {
                @Override
                public void onStateClick(RVAdapter.Acc a, int position) {

                    Toast.makeText(getApplicationContext(), "Был выбран пункт " + a.id,
                            Toast.LENGTH_SHORT).show();
                }
            };

            RVAdapter adapter = new RVAdapter(accs,stateClickListener);
            rv.setAdapter(adapter);
        }
    }
}
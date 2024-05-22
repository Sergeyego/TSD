package com.example.tsd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityPack extends AppCompatActivity {

    private RecyclerView rv;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PackAdapter adapter;
    int id_cex;
    int cl_op;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pack);
        Bundle arguments = getIntent().getExtras();
        this.setTitle(arguments.get("title").toString());

        TextView tvTitle = (TextView) findViewById(R.id.textViewTitle);
        tvTitle.setText(arguments.get("cex").toString());
        id_cex=arguments.getInt("id_cex",-1);
        cl_op=arguments.getInt("cl_op",-1);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutPack);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        rv = (RecyclerView) findViewById(R.id.packList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));

        PackAdapter.OnStateClickListener stateClickListener = new PackAdapter.OnStateClickListener() {
            @Override
            public void onStateClick(PackAdapter.Pack a, int position) {
            }
        };
        adapter = new PackAdapter(stateClickListener);
        rv.setAdapter(adapter);

        refresh();
    }

    private void refresh() {

        String query="pack/e/data/"+id_cex+"/"+cl_op;

        HttpReq.onPostExecuteListener getAccListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                swipeRefreshLayout.setRefreshing(false);
                if (!err.isEmpty()){
                    Toast.makeText(ActivityPack.this,err, Toast.LENGTH_LONG).show();
                } else {
                    updList(resp);
                }
            }
        };

        HttpReq getAcc = new HttpReq(getAccListener);
        getAcc.execute(query);
    }

    private void updList(String jsonResp){
        List<PackAdapter.Pack> packs = new ArrayList<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(ActivityPack.this,"Не удалось разобрать ответ от сервера: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            adapter.refresh(packs);
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                String time=obj.getString("time");
                String marka=obj.getString("marka")+"\n П. "+obj.getString("parti");
                double kvo = obj.getDouble("kvo");
                String pallet=obj.getString("pallet");
                String rab=obj.getString("rab");
                String master="Мастер: "+obj.getString("master");
                PackAdapter.Pack a = new PackAdapter.Pack(time,marka,kvo,pallet,rab,master);
                packs.add(a);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ActivityPack.this,"Не удалось разобрать ответ от сервера: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                adapter.refresh(packs);
                return;
            }
        }
        adapter.refresh(packs);
    }
}
package com.example.tsd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ActivityCex extends AppCompatActivity {

    private RecyclerView rv;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CexAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cex);
        Bundle arguments = getIntent().getExtras();
        this.setTitle(arguments.get("title").toString());

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutCex);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        rv = (RecyclerView) findViewById(R.id.cexList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));

        CexAdapter.OnStateClickListener stateClickListener = new CexAdapter.OnStateClickListener() {
            @Override
            public void onStateClick(CexAdapter.Cex a, int position) {
                Intent intent = new Intent();
                intent.putExtra("id", a.id);
                intent.putExtra("nam", a.nam);
                setResult(RESULT_OK, intent);
                finish();
            }
        };
        adapter = new CexAdapter(stateClickListener);
        rv.setAdapter(adapter);

        refresh();
    }

    private void refresh() {

        String query="pack/e/cex";

        HttpReq.onPostExecuteListener getAccListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                swipeRefreshLayout.setRefreshing(false);
                if (!err.isEmpty()){
                    Toast.makeText(ActivityCex.this,err, Toast.LENGTH_LONG).show();
                } else {
                    updList(resp);
                }
            }
        };

        HttpReq getAcc = new HttpReq(getAccListener);
        getAcc.execute(query);
    }

    private void updList(String jsonResp){
        List<CexAdapter.Cex> cexs = new ArrayList<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(ActivityCex.this,"Не удалось разобрать ответ от сервера: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            adapter.refresh(cexs);
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                int id=obj.getInt("id");
                String nam=obj.getString("nam");
                CexAdapter.Cex a = new CexAdapter.Cex(nam,id);
                cexs.add(a);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ActivityCex.this,"Не удалось разобрать ответ от сервера: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                adapter.refresh(cexs);
                return;
            }
        }
        adapter.refresh(cexs);
    }
}
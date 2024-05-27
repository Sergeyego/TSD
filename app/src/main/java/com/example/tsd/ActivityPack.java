package com.example.tsd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
    String current_id_rab;

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
        current_id_rab="";

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
                int id_src=obj.getInt("id_src");
                String pallet = "";
                if (id_src!=0){
                    pallet+=obj.getString("src")+"\n";
                }
                pallet+=obj.getString("pallet");
                String rab=obj.getString("rab");
                String master="Мастер: "+obj.getString("master");
                PackAdapter.Pack a = new PackAdapter.Pack(time,marka,kvo,pallet,rab,master,id_src);
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

    private void newPackDialog(int id_part, String pallet){
        Bundle bundle = new Bundle();
        bundle.putInt("id_part",id_part);
        bundle.putInt("cl_op",cl_op);
        bundle.putString("pallet",pallet);

        DialogPackEdt.acceptListener listener = new DialogPackEdt.acceptListener() {
            @Override
            public void accept(int id_part, double kvo, int kvom, int id_src, String id_master) {
                insertPack(id_part, kvo, kvom, id_src, id_master, pallet);
            }
        };

        DialogPackEdt dialog = new DialogPackEdt(listener);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogPackEdt");
    }

    private void rabScanFinished(String jsonResp){
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(ActivityPack.this,"Не удалось разобрать ответ от сервера: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (jsonArray.length()>0){
            try {
                JSONObject obj=jsonArray.getJSONObject(0);
                current_id_rab=obj.getString("id");
                String snam=obj.getString("snam");
                scanProd(snam);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ActivityPack.this,"Не удалось разобрать ответ от сервера: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Toast.makeText(ActivityPack.this,"Не найден работник с таким номером", Toast.LENGTH_LONG).show();
        }
    }

    ActivityResultLauncher<Intent> addProdActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData().hasExtra("barcode")) {
                        String barcode = result.getData().getStringExtra("barcode");
                        BarcodDecoder.Barcod b=BarcodDecoder.decode(barcode);
                        if (b.ok && b.id_part>0 && b.type.equals("e")){
                            newPackDialog(b.id_part, b.barcodeCont);
                        } else {
                            Toast.makeText(ActivityPack.this,"Не удалось разобрать штрихкод", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

    private void scanProd(String rab){
        Intent intent = new Intent(ActivityPack.this, DialogBarcode.class);
        intent.putExtra("title",rab+"\n"+"Отсканируйте упаковочный лист или этикетку");
        addProdActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> addRabActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData().hasExtra("barcode")) {
                        String barcode = result.getData().getStringExtra("barcode");
                        String query="pack/rab/"+barcode;
                        HttpReq.onPostExecuteListener getAccListener = new HttpReq.onPostExecuteListener() {
                            @Override
                            public void postExecute(String resp, String err) {
                                swipeRefreshLayout.setRefreshing(false);
                                if (!err.isEmpty()){
                                    Toast.makeText(ActivityPack.this,err, Toast.LENGTH_LONG).show();
                                } else {
                                    rabScanFinished(resp);
                                }
                            }
                        };
                        HttpReq getAcc = new HttpReq(getAccListener);
                        getAcc.execute(query);
                    }
                }
            });

    private void newPack(){
        Intent intent = new Intent(ActivityPack.this, DialogBarcode.class);
        intent.putExtra("title","Отсканируйте бейджик с табельным номером");
        addRabActivityResultLauncher.launch(intent);
    }

    private void insertPack(int id_part, double kvo, int kvom, int id_src, String id_master, String pallet){
        HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (err.isEmpty()){
                    refresh();
                } else {
                    Toast.makeText(ActivityPack.this,err, Toast.LENGTH_LONG).show();
                }
            }
        };

        JSONObject obj = new JSONObject();
        try {
            obj.put("id_part", id_part);
            obj.put("kvo",kvo);
            obj.put("pack_kvo",kvom);
            obj.put("pallet",pallet);
            obj.put("id_cex",id_cex);
            obj.put("id_src",id_src);
            obj.put("id_rab",current_id_rab);
            obj.put("id_master",id_master);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] par = new String[3];
        par[0]="pack/e/data";
        par[1]="POST";
        par[2]= obj.toString();
        new HttpReq(listener).execute(par);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pack, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_pack_new:
                newPack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3){
            newPack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
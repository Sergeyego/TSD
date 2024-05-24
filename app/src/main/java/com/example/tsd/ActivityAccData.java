package com.example.tsd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityAccData extends AppCompatActivity {
    
    private RecyclerView rvData;
    private TextView lblTotal;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int id_acc;
    private String numDoc;
    private Date dateDoc;
    private int id_type;
    private boolean addFlag;
    private AccDataAdapter adapter;
    private String prefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_data);
        Bundle arguments = getIntent().getExtras();
        numDoc = arguments.get("num").toString();
        String type = arguments.get("type").toString();
        id_acc = arguments.getInt("id");
        id_type = arguments.getInt("id_type");
        prefix=arguments.get("prefix").toString();

        String sdat = arguments.getString("date");
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        dateDoc = simpledateformat.parse(sdat,pos);
        addFlag = false;

        this.setTitle("№ "+numDoc+" от "+DateFormat.format("dd.MM.yy", dateDoc).toString());

        TextView lblType = (TextView) findViewById(R.id.lblAccType);
        lblType.setText(type);

        lblTotal = (TextView) findViewById(R.id.lblElAccItogo);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutAccData);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        rvData = (RecyclerView) findViewById(R.id.rvListAccData);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvData.setLayoutManager(llm);
        rvData.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));

        registerForContextMenu(rvData);

        adapter = new AccDataAdapter(null);
        rvData.setAdapter(adapter);

        refresh();
    }

    private void refresh() {
        
        String id=String.valueOf(id_acc);
        String query="acceptances/"+prefix+"/data/"+id;
        HttpReq.onPostExecuteListener getAccDataListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                swipeRefreshLayout.setRefreshing(false);
                if (!err.isEmpty()){
                    Toast.makeText(ActivityAccData.this,err, Toast.LENGTH_LONG).show();
                } else {
                    updList(resp);
                    if (addFlag){
                        addFlag=false;
                        newAccDataEl();
                    }
                }
            }
        };
        new HttpReq(getAccDataListener).execute(query);
    }

    private void updList(String jsonResp){
        List<AccDataAdapter.AccData> accsd = new ArrayList<>();
        double total=0;
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
            lblTotal.setText("");
            adapter.refresh(accsd);
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                int id=obj.getInt("id");
                int id_part=obj.getInt("id_part");
                int numcont=obj.getInt("numcont");
                double kvo=obj.getDouble("kvo");
                int kvom=obj.isNull("shtuk") ? 0 : obj.getInt("shtuk");
                boolean ok=obj.getBoolean("chk");
                String npart=obj.getString("n_s");
                String datPart=obj.getString("dat_part");
                String marka=obj.getString("marka");
                String packEd=obj.getString("pack_ed");
                String pack_group=obj.getString("pack_group");
                String barcodecont=obj.isNull("barcodecont") ? "" : obj.getString("barcodecont");

                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");

                Date dPart = new Date();
                try {
                    dPart=simpledateformat.parse(datPart);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String mnom=marka+"\n("+packEd;
                if (pack_group.isEmpty() || pack_group.equals("-")){
                    mnom+=")";
                } else {
                    mnom+="/"+pack_group+")";
                }
                String part=npart+" от "+DateFormat.format("dd.MM.yy", dPart).toString();
                total+=kvo;

                AccDataAdapter.AccData a = new AccDataAdapter.AccData(mnom,part,barcodecont,numcont,id,id_part,kvo,kvom,ok);
                accsd.add(a);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
                lblTotal.setText("");
                adapter.refresh(accsd);
                return;
            }
        }
        DecimalFormat ourForm = new DecimalFormat("###,##0.00");
        lblTotal.setText("Итого: "+ourForm.format(total)+" кг");
        adapter.refresh(accsd);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_acc_data, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_acc_data_new:
                newAccDataEl();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3){
            newAccDataEl();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateAccData(int id, int id_part, double kvo, int kvom, int numcont){
        HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (err.isEmpty()){
                    refresh();
                } else {
                    Toast.makeText(ActivityAccData.this,err, Toast.LENGTH_LONG).show();
                }
            }
        };

        JSONObject obj = new JSONObject();
        try {
            obj.put("kvo",kvo);
            obj.put("shtuk",kvom);
            obj.put("numcont",numcont);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] par = new String[3];
        par[0]="acceptances/"+prefix+"/data/"+String.valueOf(id);
        par[1]="PATCH";
        par[2]= obj.toString();
        new HttpReq(listener).execute(par);
    }

    private void insertAccData(int id_part, double kvo, int kvom, int numcont, String barcodecont){
        HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (err.isEmpty()){
                    addFlag=true;
                    refresh();
                } else {
                    Toast.makeText(ActivityAccData.this,err, Toast.LENGTH_LONG).show();
                }
            }
        };

        JSONObject obj = new JSONObject();
        try {
            obj.put("id_part", id_part);
            obj.put("kvo",kvo);
            obj.put("shtuk",kvom);
            obj.put("numcont",numcont);
            obj.put("barcodecont",barcodecont);
            obj.put("chk",!barcodecont.isEmpty());
            obj.put("id_nakl",id_acc);

            obj.put("id_type",id_type);
            obj.put("dat", DateFormat.format("yyyy-MM-dd", dateDoc).toString());
            obj.put("docs",numDoc);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] par = new String[3];
        par[0]="acceptances/"+prefix+"/data";
        par[1]="POST";
        par[2]= obj.toString();
        new HttpReq(listener).execute(par);
    }

    private void newAccDataDialog(int id_p, double kvo, int kvom, String barcodecont){
        Bundle bundle = new Bundle();
        int n=1;
        if (adapter.getItemCount()>0){
            n+=adapter.getItem(adapter.getItemCount()-1).numcont;
        }
        bundle.putInt("idpart",id_p);
        bundle.putDouble("kvo",kvo);
        bundle.putInt("kvom",kvom);
        bundle.putInt("numcont",n);
        bundle.putString("barcodecont",barcodecont);
        bundle.putString("prefix",prefix);
        bundle.putBoolean("new",true);

        DialogAccDataEdt.acceptListener listener = new DialogAccDataEdt.acceptListener() {
            @Override
            public void accept(int id_part, double kvo, int kvom, int numcont, String barcodecont) {
                insertAccData(id_part,kvo,kvom,numcont, barcodecont);
            }
        };

        DialogAccDataEdt dialog = new DialogAccDataEdt(listener);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogElAccDataNew");
    }

    ActivityResultLauncher<Intent> addActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData().hasExtra("barcode")) {
                        String barcode = result.getData().getStringExtra("barcode");
                        BarcodDecoder.Barcod b=BarcodDecoder.decode(barcode);
                        if (b.ok && b.id_part>0 && b.type.equals(prefix)){
                            newAccDataDialog(b.id_part,b.kvo,b.kvom,b.barcodeCont);
                        } else {
                            Toast.makeText(ActivityAccData.this,"Не удалось разобрать штрихкод", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

    private void newAccDataEl(){
        Intent intent = new Intent(ActivityAccData.this, DialogBarcode.class);
        intent.putExtra("title","Отсканируйте упаковочный лист");
        addActivityResultLauncher.launch(intent);
    }

    private void edtAccData(int pos){

        AccDataAdapter.AccData data = adapter.getItem(pos);
        Bundle bundle = new Bundle();
        bundle.putInt("idpart",data.id_part);
        bundle.putDouble("kvo",data.kvo);
        bundle.putInt("kvom",data.kvom);
        bundle.putInt("numcont",data.numcont);
        bundle.putString("barcodecont",data.namcont);
        bundle.putString("prefix",prefix);
        bundle.putBoolean("new",false);

        DialogAccDataEdt.acceptListener listener = new DialogAccDataEdt.acceptListener() {
            @Override
            public void accept(int id_part, double kvo, int kvom, int numcont, String barcodecont) {
                updateAccData(data.id,id_part,kvo,kvom,numcont);
            }
        };

        DialogAccDataEdt dialog = new DialogAccDataEdt(listener);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogAccDataEdt"+prefix);
    }

    private void delAccData(int pos){
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAccData.this);
        AccDataAdapter.AccData item = adapter.getItem(pos);
        builder.setTitle("Подтвердите удаление")
                .setMessage("Удалить "+item.marka+" "+item.parti+"?")
                .setCancelable(false)
                .setPositiveButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
                                    @Override
                                    public void postExecute(String resp, String err) {
                                        if (err.isEmpty()) {
                                            refresh();
                                        } else {
                                            Toast.makeText(ActivityAccData.this,err, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                };

                                String[] par = new String[3];
                                par[0]="acceptances/"+prefix+"/data/"+String.valueOf(item.id);
                                par[1]="DELETE";
                                par[2]= "";
                                new HttpReq(listener).execute(par);

                            }
                        }).setNegativeButton("Нет",null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int pos = item.getGroupId();
        if (pos>=0 && pos<adapter.getItemCount()){
            switch (item.getItemId()) {
                case AccDataAdapter.MENU_ACC_DATA_EDT:
                    edtAccData(pos);
                    break;
                case AccDataAdapter.MENU_ACC_DATA_DEL:
                    delAccData(pos);
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }
}
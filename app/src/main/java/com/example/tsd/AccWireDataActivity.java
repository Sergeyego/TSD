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
import androidx.annotation.Nullable;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AccWireDataActivity extends AppCompatActivity {
    
    private RecyclerView rvData;
    private TextView lblTotal;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int id_acc;
    private String numDoc;
    private Date dateDoc;
    //private int id_type;
    private boolean addFlag;
    private AccDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_data);
        Bundle arguments = getIntent().getExtras();
        numDoc = arguments.get("num").toString();
        String type = arguments.get("type").toString();
        id_acc = arguments.getInt("id");
        //id_type = arguments.getInt("id_type");

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
        String query="wire_warehouse?id_waybill=eq."+id+"&select=id,id_wparti,m_netto,pack_kvo,numcont,barcodecont,chk,wire_parti(wire_parti_m!wire_parti_id_m_fkey(n_s,dat,provol!wire_parti_m_id_provol_fkey(nam),diam!wire_parti_m_id_diam_fkey(diam)),wire_pack_kind(short),wire_pack(pack_ed,pack_group)),wire_whs_waybill(num,dat,wire_way_bill_type(prefix,nam))&order=id";

        HttpReq.onPostExecuteListener getAccDataListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                swipeRefreshLayout.setRefreshing(false);
                if (!err.isEmpty()){
                    Toast.makeText(AccWireDataActivity.this,err, Toast.LENGTH_LONG).show();
                } else {
                    updList(resp);
                    if (addFlag){
                        addFlag=false;
                        newAccData();
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
                int id_part=obj.getInt("id_wparti");
                int numcont=obj.getInt("numcont");
                double kvo=obj.getDouble("m_netto");
                int kvom=obj.isNull("pack_kvo") ? 0 : obj.getInt("pack_kvo");
                boolean ok=obj.getBoolean("chk");
                JSONObject objParti = obj.getJSONObject("wire_parti").getJSONObject("wire_parti_m");
                String npart=objParti.getString("n_s");
                String datPart=objParti.getString("dat");
                double diam=objParti.getJSONObject("diam").getDouble("diam");
                String marka=objParti.getJSONObject("provol").getString("nam");
                String spool =obj.getJSONObject("wire_parti").getJSONObject("wire_pack_kind").getString("short");
                String packEd=obj.getJSONObject("wire_parti").getJSONObject("wire_pack").getString("pack_ed");
                String pack_group=obj.getJSONObject("wire_parti").getJSONObject("wire_pack").getString("pack_group");
                JSONObject objNakl = obj.getJSONObject("wire_whs_waybill");
                String numNakl=objNakl.getString("num");
                String datNakl=objNakl.getString("dat");
                JSONObject objType=objNakl.getJSONObject("wire_way_bill_type");
                String prefix=objType.isNull("prefix")? "" : objType.getString("prefix");
                String barcodecont=obj.isNull("barcodecont") ? "" : obj.getString("barcodecont");
                //String typeNam=objNakl.getJSONObject("prod_nakl_tip").getString("nam");

                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal=Calendar.getInstance();
                try {
                    cal.setTime(simpledateformat.parse(datNakl));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date dPart = new Date();
                try {
                    dPart=simpledateformat.parse(datPart);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String pack=packEd;
                if (!pack_group.equals("-")){
                    pack+="/"+pack_group;
                }

                String mnom=marka+" ф "+String.valueOf(diam)+" "+spool+"\n("+pack+")";
                String part=npart+" от "+DateFormat.format("dd.MM.yy", dPart).toString();
                String namcont= barcodecont.isEmpty() ? ("EUR-"+prefix+cal.get(Calendar.YEAR)+"-"+numNakl+"-"+String.valueOf(numcont)) : barcodecont;
                total+=kvo;

                AccDataAdapter.AccData a = new AccDataAdapter.AccData(mnom,part,namcont,numcont,id,id_part,kvo,kvom,ok);
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
                newAccData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3){
            newAccData();
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
                    Toast.makeText(AccWireDataActivity.this,err, Toast.LENGTH_LONG).show();
                }
            }
        };

        JSONObject obj = new JSONObject();
        try {
            obj.put("id_wparti", id_part);
            obj.put("m_netto",kvo);
            if (kvom>0){
                obj.put("pack_kvo",kvom);
            }
            obj.put("numcont",numcont);
            obj.put("id_waybill",id_acc);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] par = new String[3];
        par[0]="wire_warehouse?id=eq."+String.valueOf(id);
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
                    Toast.makeText(AccWireDataActivity.this,err, Toast.LENGTH_LONG).show();
                }
            }
        };

        JSONObject obj = new JSONObject();
        try {
            obj.put("id_wparti", id_part);
            obj.put("m_netto",kvo);
            obj.put("pack_kvo",kvom);
            obj.put("numcont",numcont);
            obj.put("id_waybill",id_acc);
            obj.put("barcodecont",barcodecont);
            obj.put("chk",!barcodecont.isEmpty());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] par = new String[3];
        par[0]="wire_warehouse";
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

        DialogAccDataEdtWire.acceptListener listener = new DialogAccDataEdtWire.acceptListener() {
            @Override
            public void accept(int id_part, double kvo, int kvom, int numcont, String barcodecont) {
                insertAccData(id_part,kvo,kvom,numcont,barcodecont);
            }
        };

        DialogAccDataEdtWire dialog = new DialogAccDataEdtWire(listener);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogWireAccDataNew");
    }

    ActivityResultLauncher<Intent> addActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        String barcode = result.getData().getStringExtra("barcode");
                        BarcodDecoder.Barcod b=BarcodDecoder.decode(barcode);
                        if (b.ok && b.id_part>0 && b.type.equals("w")){
                            newAccDataDialog(b.id_part,b.kvo,b.kvom,b.barcodeCont);
                        } else {
                            Toast.makeText(AccWireDataActivity.this,"Не удалось разобрать штрихкод", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

    private void newAccData(){
        Intent intent = new Intent(AccWireDataActivity.this, DialogBarcode.class);
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

        DialogAccDataEdtWire.acceptListener listener = new DialogAccDataEdtWire.acceptListener() {
            @Override
            public void accept(int id_part, double kvo, int kvom, int numcont, String barcodecont) {
                updateAccData(data.id,id_part,kvo,kvom,numcont);
            }
        };

        DialogAccDataEdtWire dialog = new DialogAccDataEdtWire(listener);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogWireAccDataEdt");
    }

    private void delAccData(int pos){
        AlertDialog.Builder builder = new AlertDialog.Builder(AccWireDataActivity.this);
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
                                            Toast.makeText(AccWireDataActivity.this,err, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                };

                                String[] par = new String[3];
                                par[0]="wire_warehouse?id=eq."+String.valueOf(item.id);
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
package com.example.tsd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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

public class AccElDataActivity extends AppCompatActivity {
    
    private RecyclerView rvData;
    private TextView lblTotal;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int id_acc;
    private String numDoc;
    private Date dateDoc;
    private int id_type;
    private boolean addFlag;
    private boolean checkFlag;
    private AccDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_data);
        Bundle arguments = getIntent().getExtras();
        numDoc = arguments.get("num").toString();
        String type = arguments.get("type").toString();
        id_acc = arguments.getInt("id");
        id_type = arguments.getInt("id_type");

        String sdat = arguments.getString("date");
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        dateDoc = simpledateformat.parse(sdat,pos);
        addFlag = false;
        checkFlag = false;

        this.setTitle("??? "+numDoc+" ???? "+DateFormat.format("dd.MM.yy", dateDoc).toString());

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
        String query="prod?id_nakl=eq."+id+"&select=id,id_part,kvo,shtuk,numcont,barcodecont,chk,parti!prod_id_p_fkey(n_s,dat_part,elrtr(marka),diam,el_pack(pack_ed,pack_group)),prod_nakl(num,dat,prod_nakl_tip!prod_nakl_id_ist_fkey(prefix,nam))&order=id";

        HttpReq.onPostExecuteListener getAccDataListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                swipeRefreshLayout.setRefreshing(false);
                if (!err.isEmpty()){
                    Toast.makeText(AccElDataActivity.this,err, Toast.LENGTH_LONG).show();
                } else {
                    updList(resp);
                    if (addFlag){
                        addFlag=false;
                        newAccDataEl();
                    } else if (checkFlag){
                        checkFlag=false;
                        checkAccData("???????????????????????? ?????????????????? ?????????????????????? ????????");
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
            Toast.makeText(getApplicationContext(),"???? ?????????????? ?????????????????? ?????????? ???? ??????????????", Toast.LENGTH_SHORT).show();
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
                JSONObject objParti = obj.getJSONObject("parti");
                String npart=objParti.getString("n_s");
                String datPart=objParti.getString("dat_part");
                double diam=objParti.getDouble("diam");
                String marka=objParti.getJSONObject("elrtr").getString("marka");
                String packEd=objParti.getJSONObject("el_pack").getString("pack_ed");
                String pack_group=objParti.getJSONObject("el_pack").getString("pack_group");
                JSONObject objNakl = obj.getJSONObject("prod_nakl");
                String numNakl=objNakl.getString("num");
                String datNakl=objNakl.getString("dat");
                JSONObject objType=objNakl.getJSONObject("prod_nakl_tip");
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

                String mnom=marka+" ?? "+String.valueOf(diam)+"\n("+packEd+"/"+pack_group+")";
                String part=npart+" ???? "+DateFormat.format("dd.MM.yy", dPart).toString();
                String namcont= barcodecont.isEmpty() ? ("EUR-"+prefix+cal.get(Calendar.YEAR)+"-"+numNakl+"-"+String.valueOf(numcont)) : barcodecont;
                total+=kvo;

                AccDataAdapter.AccData a = new AccDataAdapter.AccData(mnom,part,namcont,numcont,id,id_part,kvo,kvom,ok);
                accsd.add(a);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"???? ?????????????? ?????????????????? ?????????? ???? ??????????????", Toast.LENGTH_SHORT).show();
                lblTotal.setText("");
                adapter.refresh(accsd);
                return;
            }
        }
        DecimalFormat ourForm = new DecimalFormat("###,##0.00");
        lblTotal.setText("??????????: "+ourForm.format(total)+" ????");
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
            case R.id.action_acc_data_check:
                checkAccData("???????????????????????? ?????????????????????? ????????");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3){
            newAccDataEl();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_F4){
            checkAccData("???????????????????????? ?????????????????????? ????????");
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
                    Toast.makeText(AccElDataActivity.this,err, Toast.LENGTH_LONG).show();
                }
            }
        };

        JSONObject obj = new JSONObject();
        try {
            obj.put("id_part", id_part);
            obj.put("kvo",kvo);
            if (kvom>0){
                obj.put("shtuk",kvom);
            }
            obj.put("numcont",numcont);

            obj.put("id_ist",id_type);
            obj.put("dat", DateFormat.format("yyyy-MM-dd", dateDoc).toString());
            obj.put("docs",numDoc);
            obj.put("id_nakl",id_acc);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] par = new String[3];
        par[0]="prod?id=eq."+String.valueOf(id);
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
                    Toast.makeText(AccElDataActivity.this,err, Toast.LENGTH_LONG).show();
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

            obj.put("id_ist",id_type);
            obj.put("dat", DateFormat.format("yyyy-MM-dd", dateDoc).toString());
            obj.put("docs",numDoc);
            obj.put("id_nakl",id_acc);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] par = new String[3];
        par[0]="prod";
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

        DialogAccDataEdtEl.acceptListener listener = new DialogAccDataEdtEl.acceptListener() {
            @Override
            public void accept(int id_part, double kvo, int kvom, int numcont, String barcodecont) {
                insertAccData(id_part,kvo,kvom,numcont, barcodecont);
            }
        };

        DialogAccDataEdtEl dialog = new DialogAccDataEdtEl(listener);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogElAccDataNew");
    }

    private void newAccDataEl(){

        DialogBarcode.acceptListener listener = new DialogBarcode.acceptListener() {
            @Override
            public void accept(String barcode) {
                BarcodDecoder.Barcod b = BarcodDecoder.decode(barcode);
                if (b.ok && b.id_part>0 && b.type.equals("e")){
                    newAccDataDialog(b.id_part,b.kvo,b.kvom,b.barcodeCont);
                } else {
                    Toast.makeText(AccElDataActivity.this,"???? ?????????????? ?????????????????? ????????????????", Toast.LENGTH_LONG).show();
                }
            }
        };

        DialogBarcode dialog = new DialogBarcode("???????????????????????? ?????????????????????? ????????",listener);
        dialog.show(getSupportFragmentManager(), "dialogElAccBarcode");
    }

    private void edtAccData(int pos){

        AccDataAdapter.AccData data = adapter.getItem(pos);
        Bundle bundle = new Bundle();
        bundle.putInt("idpart",data.id_part);
        bundle.putDouble("kvo",data.kvo);
        bundle.putInt("kvom",data.kvom);
        bundle.putInt("numcont",data.numcont);
        bundle.putString("barcodecont",data.namcont);

        DialogAccDataEdtEl.acceptListener listener = new DialogAccDataEdtEl.acceptListener() {
            @Override
            public void accept(int id_part, double kvo, int kvom, int numcont, String barcodecont) {
                updateAccData(data.id,id_part,kvo,kvom,numcont);
            }
        };

        DialogAccDataEdtEl dialog = new DialogAccDataEdtEl(listener);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogElAccDataEdt");
    }

    private void delAccData(int pos){
        AlertDialog.Builder builder = new AlertDialog.Builder(AccElDataActivity.this);
        AccDataAdapter.AccData item = adapter.getItem(pos);
        builder.setTitle("?????????????????????? ????????????????")
                .setMessage("?????????????? "+item.marka+" "+item.parti+"?")
                .setCancelable(false)
                .setPositiveButton("????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
                                    @Override
                                    public void postExecute(String resp, String err) {
                                        if (err.isEmpty()) {
                                            refresh();
                                        } else {
                                            Toast.makeText(AccElDataActivity.this,err, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                };

                                String[] par = new String[3];
                                par[0]="prod?id=eq."+String.valueOf(item.id);
                                par[1]="DELETE";
                                par[2]= "";
                                new HttpReq(listener).execute(par);

                            }
                        }).setNegativeButton("??????",null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setOk(AccDataAdapter.AccData a){

        HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (err.isEmpty()){
                    checkFlag=true;
                    refresh();
                } else {
                    Toast.makeText(AccElDataActivity.this,err, Toast.LENGTH_LONG).show();
                }
            }
        };

        JSONObject obj = new JSONObject();
        try {
            obj.put("chk", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] par = new String[3];
        par[0]="prod?id=eq."+String.valueOf(a.id);
        par[1]="PATCH";
        par[2]= obj.toString();
        new HttpReq(listener).execute(par);
    }

    private void scanCont(List<AccDataAdapter.AccData> cont){
        DialogBarcode.acceptListener listener = new DialogBarcode.acceptListener() {
            @Override
            public void accept(String barcode) {
                boolean ok=false;
                for (AccDataAdapter.AccData item : cont){
                    if (barcode.equals(item.namcont)){
                        ok=true;
                        setOk(item);
                        break;
                    }
                }
                if (ok){
                    Toast.makeText(AccElDataActivity.this,"??????????????!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AccElDataActivity.this,"???????????????? ???? ?????????????????????????? ??????????????! ???????????????? ???????????????????? ????????????????!", Toast.LENGTH_LONG).show();
                    scanCont(cont);
                }
            }
        };

        String nams="";

        for (AccDataAdapter.AccData item : cont){
            if (!nams.isEmpty()){
                nams+=" ?????? ";
            }
            nams+=item.namcont;
        }

        DialogBarcode dialog = new DialogBarcode("???????????????? ?? ???????????????????????? ???????????????? ??????????????: "+nams,listener);
        dialog.show(getSupportFragmentManager(), "dialogElAccCheckBarcode");
    }

    private void checkAccData(String mess){
        boolean finish=true;
        for (AccDataAdapter.AccData a : adapter.getItemList()){
            if (!a.ok){
                finish=false;
                break;
            }
        }
        if (finish){
            Toast.makeText(AccElDataActivity.this,"?????? ?????????????? ????????????????????????????.", Toast.LENGTH_LONG).show();
            return;
        }
        DialogBarcode.acceptListener listener = new DialogBarcode.acceptListener() {
            @Override
            public void accept(String barcode) {
                BarcodDecoder.Barcod b = BarcodDecoder.decode(barcode);
                if (b.ok && b.id_part>0){
                    List<AccDataAdapter.AccData> namsCont = new ArrayList<>();
                    for (AccDataAdapter.AccData a : adapter.getItemList()){
                        if (a.id_part==b.id_part && (b.kvo==0 || b.kvo==a.kvo)){
                            namsCont.add(a);
                        }
                    }
                    if (namsCont.size()>0){
                        scanCont(namsCont);
                    } else {
                        Toast.makeText(AccElDataActivity.this,"???? ?????????????? ???????????????????? ???????????????? ?????? ??????????????", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AccElDataActivity.this,"???? ?????????????? ?????????????????? ????????????????", Toast.LENGTH_LONG).show();
                }
            }
        };

        DialogBarcode dialog = new DialogBarcode(mess,listener);
        dialog.show(getSupportFragmentManager(), "dialogElAccCheckBarcode");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), intentResult.getContents()+"\n"+intentResult.getFormatName(), Toast.LENGTH_SHORT).show();
                // if the intentResult is not null we'll set
                // the content and format of scan message
                //messageText.setText(intentResult.getContents());
                //messageFormat.setText(intentResult.getFormatName());
            }
        }
    }
}
package com.example.tsd;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class AccWireActivity extends AppCompatActivity {

    private TextView tvDateBeg;
    private TextView tvDateEnd;
    private RecyclerView rv;
    private DateEdit dateEditBeg, dateEditEnd;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AccAdapter adapter;
    private boolean addFlag;
    private int newId;
    private final String queryType="wire_way_bill_type?en=eq.true&select=id,nam&order=nam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc);

        this.setTitle("Отправить проволоку");

        addFlag = false;
        newId=-1;

        tvDateBeg = (TextView) findViewById(R.id.dateBeg);
        tvDateEnd = (TextView)  findViewById(R.id.dateEnd);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutAcc);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        DateEdit.changedListener dateListener = new DateEdit.changedListener() {
            @Override
            public void onChanged(Calendar d) {
                refresh();
            }
        };

        dateEditBeg = new DateEdit(tvDateBeg, dateListener);
        dateEditEnd = new DateEdit(tvDateEnd, dateListener);

        Calendar bDate = Calendar.getInstance();
        bDate.add(Calendar.DAY_OF_YEAR,-30);

        dateEditBeg.setDate(bDate);
        dateEditEnd.setDate(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.DECEMBER , 31));

        rv = (RecyclerView) findViewById(R.id.rvList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));
        registerForContextMenu(rv);

        AccAdapter.OnStateClickListener stateClickListener = new AccAdapter.OnStateClickListener() {
            @Override
            public void onStateClick(AccAdapter.Acc a, int position) {
                openAccData(a);
            }
        };
        adapter = new AccAdapter(stateClickListener);
        rv.setAdapter(adapter);

        refresh();
    }

    private void openAccData(AccAdapter.Acc a){
        Intent intent = new Intent(AccWireActivity.this, AccWireDataActivity.class);
        intent.putExtra("id",a.id);
        intent.putExtra("id_type",a.id_type);
        intent.putExtra("num",a.num);
        intent.putExtra("type",a.type);
        intent.putExtra("date",DateFormat.format("yyyy-MM-dd", a.dat).toString());
        startActivity(intent);
    }

    private void refresh() {

        String sdBeg=DateFormat.format("yyyy-MM-dd", dateEditBeg.getDate()).toString();
        String sdEnd=DateFormat.format("yyyy-MM-dd", dateEditEnd.getDate()).toString();
        String query="wire_whs_waybill?dat=gte.'"+sdBeg+"'&dat=lte.'"+sdEnd+"'&select=id,dat,num,id_type,wire_way_bill_type(nam,en)&order=dat.desc,num.desc";

        HttpReq.onPostExecuteListener getAccListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                swipeRefreshLayout.setRefreshing(false);
                if (!err.isEmpty()){
                    Toast.makeText(AccWireActivity.this,err, Toast.LENGTH_LONG).show();
                } else {
                    updList(resp);
                    if (addFlag){
                        addFlag=false;
                        newAccEl();
                    } else if (newId>0){
                        for (AccAdapter.Acc a : adapter.getItemList()){
                            if (a.id==newId){
                                openAccData(a);
                                break;
                            }
                        }
                        newId=-1;
                    }
                }
            }
        };

        HttpReq getAcc = new HttpReq(getAccListener);
        getAcc.execute(query);
    }

    private void updList(String jsonResp){
        List<AccAdapter.Acc> accs = new ArrayList<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(AccWireActivity.this,"Не удалось разобрать ответ от сервера: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            adapter.refresh(accs);
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                int id=obj.getInt("id");
                int id_type=obj.getInt("id_type");
                String num=obj.getString("num");
                JSONObject objType = obj.getJSONObject("wire_way_bill_type");
                String type=objType.getString("nam");
                boolean en = objType.getBoolean("en");
                String sDate = obj.getString("dat");
                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                ParsePosition pos = new ParsePosition(0);
                Date stringDate = simpledateformat.parse(sDate,pos);
                if (en){
                    AccAdapter.Acc a = new AccAdapter.Acc(num,type,stringDate,id,id_type);
                    accs.add(a);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AccWireActivity.this,"Не удалось разобрать ответ от сервера: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                adapter.refresh(accs);
                return;
            }
        }
        adapter.refresh(accs);
    }

    private int getNewId(String resp){
        int id=-1;
        JSONArray arr = null;
        try {
            arr = new JSONArray(resp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (arr!=null){
            if (arr.length()>0){
                JSONObject obj = null;
                try {
                    obj = arr.getJSONObject(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (obj!=null){
                    if (obj.has("id")){
                        try {
                            id=obj.getInt("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return id;
    }

    private void newAccEl(){

        Calendar date = Calendar.getInstance();
        Bundle bundle = new Bundle();
        int n=0;
        if (adapter.getItemCount()>0){
            String num=adapter.getItem(0).num;
            n = Integer.parseInt(num);
        }
        n++;
        bundle.putString("num", String.format("%04d",n));
        bundle.putString("dat",DateFormat.format("dd.MM.yyyy", date).toString());
        bundle.putInt("id_type",3);
        bundle.putString("querytype",queryType);

        DialogAccNew.acceptListener a = new DialogAccNew.acceptListener() {
            @Override
            public void accept(String num, Calendar dat, int id_type) {
                //Toast.makeText(getApplicationContext(),"OK: "+num+" "+String.valueOf(id_type), Toast.LENGTH_SHORT).show();
                HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
                    @Override
                    public void postExecute(String resp, String err) {
                        if (err.isEmpty()){
                            //Toast.makeText(AccElActivity.this,resp, Toast.LENGTH_LONG).show();
                            newId=getNewId(resp);
                            if (dat.after(dateEditEnd.getDate())){
                                dateEditEnd.setDate(dat);
                            }
                            if (dateEditBeg.getDate().after(dat)){
                                dateEditBeg.setDate(dat);
                            }
                            refresh();
                        } else {
                            Toast.makeText(AccWireActivity.this,err, Toast.LENGTH_LONG).show();
                        }
                    }
                };

                JSONObject obj = new JSONObject();
                try {
                    obj.put("num", num);
                    obj.put("id_type",id_type);
                    obj.put("dat",DateFormat.format("yyyy-MM-dd", dat).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String[] par = new String[3];
                par[0]="wire_whs_waybill";
                par[1]="POST";
                par[2]= obj.toString();
                new HttpReq(listener).execute(par);
            }
        };

        DialogAccNew dialog = new DialogAccNew(a);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogWireAccNew");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3){
            addFlag=true;
            refresh();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_acc, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_acc_new:
                addFlag=true;
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int pos = item.getGroupId();
        if (pos>=0 && pos<adapter.getItemCount()){
            switch (item.getItemId()) {
                case AccAdapter.MENU_ACC_EDT:
                    edtAcc(pos);
                    break;
                case AccAdapter.MENU_ACC_DEL:
                    delAcc(pos);
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void delAcc(int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AccWireActivity.this);
        builder.setTitle("Подтвердите удаление")
                .setMessage("Удалить "+adapter.getItem(pos).num+"?")
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
                                            Toast.makeText(AccWireActivity.this,err, Toast.LENGTH_LONG).show();
                                        }
                                        //Toast.makeText(getApplicationContext(),resp, Toast.LENGTH_LONG).show();
                                    }
                                };

                                String[] par = new String[3];
                                par[0]="wire_whs_waybill?id=eq."+String.valueOf(adapter.getItem(pos).id);
                                par[1]="DELETE";
                                par[2]= "";
                                new HttpReq(listener).execute(par);

                            }
                        }).setNegativeButton("Нет",null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void edtAcc(int pos) {

        AccAdapter.Acc item = adapter.getItem(pos);
        Bundle bundle = new Bundle();
        bundle.putString("num", item.num);
        bundle.putString("dat",DateFormat.format("dd.MM.yyyy", item.dat).toString());
        bundle.putInt("id_type",item.id_type);
        bundle.putString("querytype",queryType);

        DialogAccNew.acceptListener a = new DialogAccNew.acceptListener() {
            @Override
            public void accept(String num, Calendar dat, int id_type) {
                HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
                    @Override
                    public void postExecute(String resp, String err) {
                        if (err.isEmpty()) {
                            refresh();
                        } else {
                            Toast.makeText(AccWireActivity.this,err, Toast.LENGTH_LONG).show();
                        }
                    }
                };

                JSONObject obj = new JSONObject();
                try {
                    obj.put("num", num);
                    obj.put("id_type",id_type);
                    obj.put("dat",DateFormat.format("yyyy-MM-dd", dat).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String[] par = new String[3];
                par[0]="wire_whs_waybill?id=eq."+String.valueOf(item.id);
                par[1]="PATCH";
                par[2]= obj.toString();
                new HttpReq(listener).execute(par);
            }
        };

        DialogAccNew dialog = new DialogAccNew(a);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogWireAccEdt");
    }
}
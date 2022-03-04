package com.example.tsd;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

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

public class AccElActivity extends AppCompatActivity {

    private Button btnUpd;
    private TextView tvDateBeg;
    private TextView tvDateEnd;
    private RecyclerView rv;
    private Calendar dateBeg, dateEnd;

    private List<RVAdapter.Acc> accs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_el);

        this.setTitle("Отправить электроды");

        btnUpd = (Button) findViewById(R.id.btnUpd);
        tvDateBeg = (TextView) findViewById(R.id.dateBeg);
        tvDateEnd = (TextView)  findViewById(R.id.dateEnd);

        Calendar bDate = Calendar.getInstance();
        bDate.add(Calendar.DAY_OF_YEAR,-14);
        dateBeg = bDate;
        dateEnd = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.DECEMBER , 31);
        setLblDate();

        DatePickerDialog.OnDateSetListener dateBegSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                dateBeg.set(Calendar.YEAR,year);
                dateBeg.set(Calendar.MONTH,monthOfYear);
                dateBeg.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                setLblDate();
            }
        };

        DatePickerDialog.OnDateSetListener dateEndSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                dateEnd.set(Calendar.YEAR,year);
                dateEnd.set(Calendar.MONTH,monthOfYear);
                dateEnd.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                setLblDate();
            }
        };

        tvDateBeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AccElActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        dateBegSetListener, dateBeg.get(Calendar.YEAR), dateBeg.get(Calendar.MONTH), dateBeg.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        tvDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AccElActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        dateEndSetListener, dateEnd.get(Calendar.YEAR), dateEnd.get(Calendar.MONTH), dateEnd.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });


        accs = new ArrayList<>();

        rv = (RecyclerView) findViewById(R.id.rvList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));

        registerForContextMenu(rv);

        btnUpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });
        refresh();
    }

    private void setLblDate(){
        tvDateBeg.setText(DateFormat.format("dd.MM.yy", dateBeg).toString());
        tvDateEnd.setText(DateFormat.format("dd.MM.yy", dateEnd).toString());
    }

    private void refresh() {

        String sdBeg=DateFormat.format("yyyy-MM-dd", dateBeg).toString();
        String sdEnd=DateFormat.format("yyyy-MM-dd", dateEnd).toString();
        String query="prod_nakl?dat=gte.'"+sdBeg+"'&dat=lte.'"+sdEnd+"'&select=id,dat,num,id_ist,prod_nakl_tip(nam,en)&order=dat.desc,num.desc";

        HttpReq.onPostExecuteListener getAccListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (!err.isEmpty()){
                    Toast.makeText(AccElActivity.this,err, Toast.LENGTH_LONG).show();
                } else {
                    updList(resp);
                    RVAdapter.OnStateClickListener stateClickListener = new RVAdapter.OnStateClickListener() {
                        @Override
                        public void onStateClick(RVAdapter.Acc a, int position) {
                            //Toast.makeText(getApplicationContext(), "Был выбран пункт " + a.id, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AccElActivity.this, AccElDataActivity.class);
                            intent.putExtra("id",a.id);
                            intent.putExtra("id_type",a.id_type);
                            intent.putExtra("num",a.num);
                            intent.putExtra("type",a.type);
                            intent.putExtra("date",DateFormat.format("yyyy-MM-dd", a.dat).toString());
                            startActivity(intent);
                        }
                    };
                    RVAdapter adapter = new RVAdapter(accs,stateClickListener);
                    rv.setAdapter(adapter);
                }
            }
        };

        HttpReq getAcc = new HttpReq(getAccListener);
        getAcc.execute(query);

    }

    private void updList(String jsonResp){
        accs.clear();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(AccElActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                int id=obj.getInt("id");
                int id_type=obj.getInt("id_ist");
                String num=obj.getString("num");
                JSONObject objType = obj.getJSONObject("prod_nakl_tip");
                String type=objType.getString("nam");
                boolean en = objType.getBoolean("en");
                String sDate = obj.getString("dat");
                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                ParsePosition pos = new ParsePosition(0);
                Date stringDate = simpledateformat.parse(sDate,pos);
                if (en){
                    RVAdapter.Acc a = new RVAdapter.Acc(num,type,stringDate,id,id_type);
                    accs.add(a);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AccElActivity.this,"Не удалось разобрать ответ от сервера", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void newAccEl(){

        Calendar date = Calendar.getInstance();
        Bundle bundle = new Bundle();
        int n=0;
        if (accs.size()>0){
            String num=accs.get(0).num;
            n = Integer.parseInt(num);
        }
        n++;
        bundle.putString("num", String.format("%04d",n));
        bundle.putString("dat",DateFormat.format("dd.MM.yyyy", date).toString());
        bundle.putInt("id_type",1);
        bundle.putString("querytype","prod_nakl_tip?en=eq.true&select=id,nam&order=nam");

        DialogAccNew.acceptListener a = new DialogAccNew.acceptListener() {
            @Override
            public void accept(String num, Calendar dat, int id_type) {
                //Toast.makeText(getApplicationContext(),"OK: "+num+" "+String.valueOf(id_type), Toast.LENGTH_SHORT).show();
                HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
                    @Override
                    public void postExecute(String resp, String err) {
                        if (err.isEmpty()){
                            Toast.makeText(AccElActivity.this,resp, Toast.LENGTH_LONG).show();
                            if (dat.after(dateEnd)){
                                dateEnd=dat;
                                setLblDate();
                            }
                            refresh();
                        } else {
                            Toast.makeText(AccElActivity.this,err, Toast.LENGTH_LONG).show();
                        }
                    }
                };

                JSONObject obj = new JSONObject();
                try {
                    obj.put("num", num);
                    obj.put("id_ist",id_type);
                    obj.put("dat",DateFormat.format("yyyy-MM-dd", dat).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String[] par = new String[3];
                par[0]="prod_nakl";
                par[1]="POST";
                par[2]= obj.toString();
                new HttpReq(listener).execute(par);
            }
        };

        DialogAccNew dialog = new DialogAccNew(a);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogElAccNew");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3){
            newAccEl();
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
                newAccEl();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int pos = item.getGroupId();
        if (pos>=0 && pos<accs.size()){
            switch (item.getItemId()) {
                case RVAdapter.MENU_ACC_EDT:
                    edtAcc(pos);
                    break;
                case RVAdapter.MENU_ACC_DEL:
                    delAcc(pos);
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void delAcc(int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AccElActivity.this);
        builder.setTitle("Подтвердите удаление")
                .setMessage("Удалить "+accs.get(pos).num+"?")
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
                                            Toast.makeText(AccElActivity.this,err, Toast.LENGTH_LONG).show();
                                        }
                                        //Toast.makeText(getApplicationContext(),resp, Toast.LENGTH_LONG).show();
                                    }
                                };

                                String[] par = new String[3];
                                par[0]="prod_nakl?id=eq."+String.valueOf(accs.get(pos).id);
                                par[1]="DELETE";
                                par[2]= "";
                                new HttpReq(listener).execute(par);

                            }
                        }).setNegativeButton("Нет",null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void edtAcc(int pos) {

        Bundle bundle = new Bundle();
        bundle.putString("num", accs.get(pos).num);
        bundle.putString("dat",DateFormat.format("dd.MM.yyyy", accs.get(pos).dat).toString());
        bundle.putInt("id_type",accs.get(pos).id_type);
        bundle.putString("querytype","prod_nakl_tip?en=eq.true&select=id,nam&order=nam");

        DialogAccNew.acceptListener a = new DialogAccNew.acceptListener() {
            @Override
            public void accept(String num, Calendar dat, int id_type) {
                //Toast.makeText(getApplicationContext(),"OK: "+num+" "+String.valueOf(id_type), Toast.LENGTH_SHORT).show();
                HttpReq.onPostExecuteListener listener = new HttpReq.onPostExecuteListener() {
                    @Override
                    public void postExecute(String resp, String err) {
                        //Toast.makeText(getApplicationContext(),resp, Toast.LENGTH_LONG).show();
                        if (err.isEmpty()) {
                            refresh();
                        } else {
                            Toast.makeText(AccElActivity.this,err, Toast.LENGTH_LONG).show();
                        }
                    }
                };

                JSONObject obj = new JSONObject();
                try {
                    obj.put("num", num);
                    obj.put("id_ist",id_type);
                    obj.put("dat",DateFormat.format("yyyy-MM-dd", dat).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String[] par = new String[3];
                par[0]="prod_nakl?id=eq."+String.valueOf(accs.get(pos).id);
                par[1]="PATCH";
                par[2]= obj.toString();
                new HttpReq(listener).execute(par);
            }
        };

        DialogAccNew dialog = new DialogAccNew(a);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogElAccEdt");
    }
}
package com.example.tsd;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
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

    Button btnUpd;
    TextView tvDateBeg;
    TextView tvDateEnd;
    RecyclerView rv;
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

        btnUpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeGetRequest();
            }
        });
        makeGetRequest();
    }

    private void setLblDate(){
        tvDateBeg.setText(DateFormat.format("dd.MM.yy", dateBeg).toString());
        tvDateEnd.setText(DateFormat.format("dd.MM.yy", dateEnd).toString());
    }

    private void makeGetRequest() {
        String sdBeg=DateFormat.format("yyyy-MM-dd", dateBeg).toString();
        String sdEnd=DateFormat.format("yyyy-MM-dd", dateEnd).toString();
        String query="prod_nakl?dat=gte.'"+sdBeg+"'&dat=lte.'"+sdEnd+"'&select=id,dat,num,id_ist,prod_nakl_tip(nam,en)&order=dat.desc,num.desc";
        new HttpReqGet().execute(query);
    }

    private void updList(String jsonResp){
        accs.clear();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private class HttpReqGet extends HttpReq{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(),"resp: "+server_response, Toast.LENGTH_SHORT).show();

            updList(server_response);

            RVAdapter.OnStateClickListener stateClickListener = new RVAdapter.OnStateClickListener() {
                @Override
                public void onStateClick(RVAdapter.Acc a, int position) {
                    //Toast.makeText(getApplicationContext(), "Был выбран пункт " + a.id, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AccElActivity.this, AccElDataActivity.class);
                    intent.putExtra("id",a.id);
                    intent.putExtra("id",a.id_type);
                    intent.putExtra("num",a.num);
                    intent.putExtra("type",a.type);
                    intent.putExtra("date",DateFormat.format("dd.MM.yy", a.dat).toString());
                    startActivity(intent);
                }
            };

            RVAdapter adapter = new RVAdapter(accs,stateClickListener);
            rv.setAdapter(adapter);
        }
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
                Toast.makeText(getApplicationContext(),"Новое отправление", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
package com.example.tsd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccElDataActivity extends AppCompatActivity {
    
    RecyclerView rvData;
    private List<AccDataAdapter.AccData> accsd;
    Button btnUpdData;
    int id_acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_el_data);
        Bundle arguments = getIntent().getExtras();
        String num = arguments.get("num").toString();
        String date = arguments.get("date").toString();
        String type = arguments.get("type").toString();
        id_acc = arguments.getInt("id");
        this.setTitle("№ "+num+" от "+date);

        TextView lblType = (TextView) findViewById(R.id.lblElAccType);
        lblType.setText(type);

        btnUpdData = (Button) findViewById(R.id.btnUpdAccDataEl);
        
        accsd = new ArrayList<>();

        rvData = (RecyclerView) findViewById(R.id.rvListAccDataEl);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvData.setLayoutManager(llm);
        rvData.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));

        btnUpdData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeGetRequest();
            }
        });
        makeGetRequest();
    }

    private void makeGetRequest() {
        
        String id=String.valueOf(id_acc);
        String query="prod?id_nakl=eq."+id+"&select=id,id_part,kvo,numcont,parti!prod_id_p_fkey(n_s,dat_part,elrtr(marka),diam,el_pack(pack_ed,pack_group)),prod_nakl(num,dat,prod_nakl_tip(prefix,nam))&order=id";
        new AccElDataActivity.HttpReqGet().execute(query);
    }

    private void updList(String jsonResp){
        accsd.clear();
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
                /*int id=obj.getInt("id");
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
                    //AccDataAdapter.AccData a = new AccDataAdapter.AccData(num,type,stringDate,id,id_type);
                    //accsd.add(a);
                }*/
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

            AccDataAdapter.OnStateClickListener stateClickListener = new AccDataAdapter.OnStateClickListener() {
                @Override
                public void onStateClick(AccDataAdapter.AccData a, int position) {
                    //Toast.makeText(getApplicationContext(), "Был выбран пункт " + a.id, Toast.LENGTH_SHORT).show();
                    /*Intent intent = new Intent(AccElActivity.this, AccElDataActivity.class);
                    intent.putExtra("id",a.id);
                    intent.putExtra("id",a.id_type);
                    intent.putExtra("num",a.num);
                    intent.putExtra("type",a.type);
                    intent.putExtra("date",DateFormat.format("dd.MM.yy", a.dat).toString());
                    startActivity(intent);*/
                }
            };

            AccDataAdapter adapter = new AccDataAdapter(accsd,stateClickListener);
            rvData.setAdapter(adapter);
        }
    }
}
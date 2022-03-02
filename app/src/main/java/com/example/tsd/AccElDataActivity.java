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

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AccElDataActivity extends AppCompatActivity {
    
    RecyclerView rvData;
    private List<AccDataAdapter.AccData> accsd;
    Button btnUpdData;
    int id_acc;
    TextView lblTotal;

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

        lblTotal = (TextView) findViewById(R.id.lblElAccItogo);

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

        HttpReq.onPostExecuteListener getAccDataListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                //Toast.makeText(getApplicationContext(),"resp: "+server_response, Toast.LENGTH_SHORT).show();

                updList(resp);
                AccDataAdapter.OnStateClickListener stateClickListener = new AccDataAdapter.OnStateClickListener() {
                    @Override
                    public void onStateClick(AccDataAdapter.AccData a, int position) {
                        Toast.makeText(getApplicationContext(), "Был выбран пункт " + a.id, Toast.LENGTH_SHORT).show();
                    }
                };
                AccDataAdapter adapter = new AccDataAdapter(accsd,stateClickListener);
                rvData.setAdapter(adapter);
            }
        };

        new HttpReq(getAccDataListener).execute(query);
    }

    private void updList(String jsonResp){
        accsd.clear();
        double total=0;
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
            lblTotal.setText("");
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                int id=obj.getInt("id");
                int id_part=obj.getInt("id_part");
                int numcont=obj.getInt("numcont");
                double kvo=obj.getDouble("kvo");
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
                String prefix=objNakl.getJSONObject("prod_nakl_tip").getString("prefix");
                //String typeNam=objNakl.getJSONObject("prod_nakl_tip").getString("nam");

                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                ParsePosition pos = new ParsePosition(0);
                Calendar cal=Calendar.getInstance();
                cal.setTime(simpledateformat.parse(datNakl,pos));

                String mnom=marka+" ф "+String.valueOf(diam)+"\n("+packEd+"/"+pack_group+")";
                String part=npart+" от "+datPart;
                String namcont="EUR-"+prefix+cal.get(Calendar.YEAR)+"-"+numNakl+"-"+String.valueOf(numcont);
                total+=kvo;

                AccDataAdapter.AccData a = new AccDataAdapter.AccData(mnom,part+"\n"+namcont,namcont,numcont,id,id_part,kvo);
                accsd.add(a);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
                lblTotal.setText("");
                return;
            }
        }
        DecimalFormat ourForm = new DecimalFormat("###,##0.00");
        lblTotal.setText("Итого: "+ourForm.format(total)+" кг");
    }
}
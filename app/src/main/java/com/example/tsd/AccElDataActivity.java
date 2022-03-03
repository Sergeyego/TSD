package com.example.tsd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private void newAccDataDialog(int id_p, double kvo, int kvom){
        Bundle bundle = new Bundle();
        int n=1;
        if (accsd.size()>0){
            n+=accsd.get(accsd.size()-1).numcont;
        }
        bundle.putInt("idpart",id_p);
        bundle.putDouble("kvo",kvo);
        bundle.putInt("kvom",kvom);
        bundle.putInt("numcont",n);
        bundle.putString("querypart","parti?id=eq."+String.valueOf(id_p)+"&select=n_s,dat_part,diam,elrtr(marka),el_pack(pack_ed,pack_group),istoch(nam)");

        DialogAccDataNew.acceptListener listener = new DialogAccDataNew.acceptListener() {
            @Override
            public void accept(int id_part, double kvo, int kvom, int numcont) {
                Toast.makeText(getApplicationContext(),String.valueOf(id_part), Toast.LENGTH_SHORT).show();
            }
        };

        DialogAccDataNew dialog = new DialogAccDataNew(listener);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialogElAccDataNew");
    }

    private void newAccDataEl(){

        DialogBarcode.acceptListener listener = new DialogBarcode.acceptListener() {
            @Override
            public void accept(String barcode) {
                //Toast.makeText(AccElDataActivity.this,barcode, Toast.LENGTH_SHORT).show();
                if (barcode.length()==40){
                    String id_part=barcode.substring(14,21);
                    id_part=id_part.replace("_","");
                    int id_p=Integer.parseInt(id_part);
                    int kvo=Integer.parseInt(barcode.substring(30,36));
                    int kvop=Integer.parseInt(barcode.substring(36));
                    newAccDataDialog(id_p,kvo/100.0,kvop);
                    //Toast.makeText(AccElDataActivity.this,id_p+" "+kvo+" "+kvop, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AccElDataActivity.this,"Не удалось разобрать штрихкод", Toast.LENGTH_LONG).show();
                }
            }
        };

        DialogBarcode dialog = new DialogBarcode(listener);
        dialog.show(getSupportFragmentManager(), "dialogElAccBarcode");
    }
}
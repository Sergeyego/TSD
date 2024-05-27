package com.example.tsd;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DialogPackEdt extends DialogFragment {

    public static class ListItem {
        String nam;
        int id;
        ListItem(String nam, int id) {
            this.nam = nam;
            this.id = id;
        }

        @Override
        public String toString() {
            return this.nam;
        }
    }

    public static class MasterItem {
        String nam;
        String id;
        MasterItem(String nam, String id) {
            this.nam = nam;
            this.id = id;
        }

        @Override
        public String toString() {
            return this.nam;
        }
    }

    public DialogPackEdt(acceptListener aListener) {
        this.aListener = aListener;
    }

    interface acceptListener {
        void accept(int id_part, double kvo, int kvom, int id_src, String id_master);
    }
    private final DialogPackEdt.acceptListener aListener;

    private int id_part;
    private String pallet;
    private int cl_op;
    private TextView lblMarka;
    private TextView lblPart;
    private TextView lblBarcodeCont;
    private EditText edtKvo;
    private EditText edtKvoM;
    private double mas_ed;
    private int currentIdSrc;
    private String currentIdMaster;

    private Spinner srcSpinner;
    private Spinner masterSpinner;
    private Button btnSave, btnCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle("Данные поддона");
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.dialog_pack_edt, null);

        mas_ed=0;

        lblMarka = v.findViewById(R.id.lblPackMarka);
        lblPart = v.findViewById(R.id.lblPackPart);
        lblBarcodeCont = v.findViewById(R.id.lblPackBarcodeCont);
        edtKvo = v.findViewById(R.id.editTextPackKvo);
        edtKvoM = v.findViewById(R.id.editTextPackKvoM);

        srcSpinner = (Spinner) v.findViewById(R.id.spinnerPackOp);
        masterSpinner = (Spinner) v.findViewById(R.id.spinnerPackMaster);

        btnSave = v.findViewById(R.id.btnPackOk);
        btnCancel = v.findViewById(R.id.btnPackCancel);

        id_part=-1;
        currentIdSrc=-1;
        currentIdMaster="";

        Bundle args = getArguments();
        if (args != null) {
            id_part=args.getInt("id_part");
            cl_op=args.getInt("cl_op");
            pallet = args.getString("pallet");
            if  (!pallet.isEmpty()){
                lblBarcodeCont.setText("Доупаковка "+pallet);
            } else {
                lblBarcodeCont.setText(pallet);
            }
        }

        srcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DialogPackEdt.ListItem item = (DialogPackEdt.ListItem) adapterView.getItemAtPosition(i);
                currentIdSrc=item.id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        masterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DialogPackEdt.MasterItem item = (DialogPackEdt.MasterItem) adapterView.getItemAtPosition(i);
                currentIdMaster=item.id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commit();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        View.OnKeyListener keyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                //Toast.makeText(getContext(),keyEvent.toString(), Toast.LENGTH_SHORT).show();
                if (keyEvent.getKeyCode()==KeyEvent.KEYCODE_F1){
                    commit();
                    return true;
                } else if (keyEvent.getKeyCode()==KeyEvent.KEYCODE_F2) {
                    dismiss();
                    return true;
                }
                return false;
            }
        };

        edtKvo.setOnKeyListener(keyListener);
        edtKvoM.setOnKeyListener(keyListener);

        edtKvo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                double d=0;
                if (mas_ed!=0){
                    d = getKvo() / mas_ed;
                    int kvom= (int) d;
                    edtKvoM.setText(String.valueOf(kvom));
                }
                if (d % 1 !=0){
                    edtKvoM.setTextColor(Color.rgb(255,0,0));
                } else {
                    edtKvoM.setTextColor(Color.rgb(0,0,0));
                }
            }
        });


        startUpdPart();

        return v;
    }

    private void startUpdMaster(){
        HttpReq.onPostExecuteListener getSrcListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (err.isEmpty()){
                    updMaster(resp);
                } else {
                    Toast.makeText(getContext(),err, Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        } ;
        HttpReq reqGetType = new HttpReq(getSrcListener);
        reqGetType.execute("pack/e/master");
    }

    private void startUpdSpinner(String query, Spinner spinner, int current_id){
        HttpReq.onPostExecuteListener getSrcListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (err.isEmpty()){
                    updSpinner(resp,spinner,current_id);
                } else {
                    Toast.makeText(getContext(),err, Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        } ;
        HttpReq reqGetType = new HttpReq(getSrcListener);
        reqGetType.execute(query);
    }

    private void startUpdPart(){
        String queryPart = "pack/e/parti/"+String.valueOf(cl_op)+"/"+String.valueOf(id_part);
        if (pallet.length()==10){
            queryPart+="?pallet="+pallet;
        }
        HttpReq.onPostExecuteListener getPartListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (err.isEmpty()){
                    updDataPart(resp);
                } else {
                    Toast.makeText(getContext(),err, Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        } ;
        HttpReq reqPart = new HttpReq(getPartListener);
        reqPart.execute(queryPart);
    }

    private double getKvo() {
        double kvo=0;
        String txt=edtKvo.getText().toString();
        if (!txt.isEmpty()){
            kvo=Double.valueOf(txt);
        }
        return kvo;
    }

    private int getKvom(){
        return edtKvoM.getText().toString().isEmpty()? 0 : Integer.valueOf(edtKvoM.getText().toString());
    }


    private void commit(){
        aListener.accept(id_part,getKvo(),getKvom(),currentIdSrc,currentIdMaster);
        dismiss();
    }

    void updDataPart(String jsonResp){
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                String numpart=obj.getString("n_s");
                String datpart=obj.getString("dat_part");
                String marka=obj.getString("marka");

                String packEd=obj.getString("pack_ed");
                mas_ed=obj.getDouble("mass_ed");
                double kvo=obj.getDouble("mass_pallet");
                edtKvo.setText(String.format(Locale.ENGLISH,"%.1f",kvo));

                if (cl_op!=2){
                    currentIdSrc=obj.getInt("id_src");
                } else {
                    currentIdSrc=0;
                }
                currentIdMaster=obj.getString("id_master");

                String src=obj.getString("src");

                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                ParsePosition pos = new ParsePosition(0);
                Date stringDate = simpledateformat.parse(datpart,pos);

                lblMarka.setText(marka+" ("+packEd+")");
                lblPart.setText("п. "+numpart+" от "+ DateFormat.format("dd.MM.yy", stringDate).toString()+" ("+src+")");

                String querySrc="pack/e/src/"+String.valueOf(cl_op);
                startUpdSpinner(querySrc,srcSpinner,currentIdSrc);

                startUpdMaster();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void updSpinner(String jsonResp, Spinner spinner, int current_id){
        JSONArray jsonArray = null;
        List<DialogPackEdt.ListItem> list;
        list = new ArrayList<>();
        list.clear();
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            Toast.makeText(getContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                int id=obj.getInt("id");
                String nam = obj.getString("nam");
                DialogPackEdt.ListItem a = new DialogPackEdt.ListItem(nam,id);
                list.add(a);
            } catch (JSONException e) {
                Toast.makeText(getContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        for (int i=0; i<list.size();i++){
            if (list.get(i).id==current_id){
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void updMaster(String jsonResp){
        JSONArray jsonArray = null;
        List<DialogPackEdt.MasterItem> list;
        list = new ArrayList<>();
        list.clear();
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            Toast.makeText(getContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                String id=obj.getString("id");
                String nam = obj.getString("nam");
                DialogPackEdt.MasterItem a = new DialogPackEdt.MasterItem(nam,id);
                list.add(a);
            } catch (JSONException e) {
                Toast.makeText(getContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        masterSpinner.setAdapter(adapter);

        for (int i=0; i<list.size();i++){
            if (list.get(i).id==currentIdMaster){
                masterSpinner.setSelection(i);
                break;
            }
        }
    }
}

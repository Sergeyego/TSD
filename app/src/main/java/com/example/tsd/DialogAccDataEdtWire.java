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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DialogAccDataEdtWire extends DialogFragment {

    public DialogAccDataEdtWire(acceptListener aListener) {
        this.aListener = aListener;
    }

    interface acceptListener {
        void accept(int id_part, double kvo, int kvom, int numcont);
    }
    private final DialogAccDataEdtWire.acceptListener aListener;

    private int id_part;

    private TextView lblMarka;
    private TextView lblPack;
    private TextView lblPart;
    private EditText edtKvo;
    private EditText edtKvoM;
    private EditText edtNumCont;
    private double mas_ed, mas_group;

    private Button btnSave, btnCancel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle("Данные поддона");
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.dialog_new_acc_data, null);

        mas_ed=0;
        mas_group=0;

        lblMarka = v.findViewById(R.id.lblAccDataNewMarka);
        lblPack = v.findViewById(R.id.lblAccDataNewPack);
        lblPart = v.findViewById(R.id.lblAccDataNewPart);
        edtKvo = v.findViewById(R.id.editTextAccNewKvo);
        edtKvoM = v.findViewById(R.id.editTextAccNewKvoM);
        edtNumCont = v.findViewById(R.id.editTextAccNewNumPal);

        btnSave = v.findViewById(R.id.btnAccDataNewOk);
        btnCancel = v.findViewById(R.id.btnAccDataNewCancel);

        id_part=-1;


        Bundle args = getArguments();
        if (args != null) {
            id_part=args.getInt("idpart");
            double kvo=args.getDouble("kvo");
            if (kvo!=0){
                edtKvo.setText(String.format(Locale.ENGLISH,"%.2f",kvo));
            }
            edtKvoM.setText(String.valueOf(args.getInt("kvom")));
            edtNumCont.setText(String.valueOf(args.getInt("numcont")));

        }

        String queryPart="wire_parti?id=eq."+String.valueOf(id_part)+"&select=wire_parti_m!wire_parti_id_m_fkey(n_s,dat,wire_source!wire_parti_m_id_source_fkey(nam),provol!wire_parti_m_id_provol_fkey(nam),diam!wire_parti_m_id_diam_fkey(diam)),wire_pack_kind(short),wire_pack(pack_ed,pack_group,mas_ed,mas_group)";

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
        HttpReq reqGetType = new HttpReq(getPartListener);
        reqGetType.execute(queryPart);

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
        edtNumCont.setOnKeyListener(keyListener);

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

        return v;
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

    private int getNumCont(){
        return edtNumCont.getText().toString().isEmpty()? 0 : Integer.valueOf(edtNumCont.getText().toString());
    }

    private void commit(){
        aListener.accept(id_part,getKvo(),getKvom(),getNumCont());
        dismiss();
    }

    void updDataPart(String jsonResp){
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(),e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        for (int i=0; i<jsonArray.length();i++){
            try {
                JSONObject obj=jsonArray.getJSONObject(i);
                JSONObject objParti = obj.getJSONObject("wire_parti_m");
                String numpart=objParti.getString("n_s");
                String datpart=objParti.getString("dat");
                double diam=objParti.getJSONObject("diam").getDouble("diam");
                String marka=objParti.getJSONObject("provol").getString("nam");
                String spool =obj.getJSONObject("wire_pack_kind").getString("short");
                JSONObject objPack = obj.getJSONObject("wire_pack");
                String packEd=objPack.getString("pack_ed");
                String packGroup=objPack.getString("pack_group");
                mas_ed=objPack.getDouble("mas_ed");
                mas_group=objPack.getDouble("mas_group");

                String src=objParti.getJSONObject("wire_source").getString("nam");

                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                Date stringDate = new Date();
                try {
                    stringDate = simpledateformat.parse(datpart);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String pack=packEd;
                if (!packGroup.equals("-")){
                    pack+="/"+packGroup;
                }

                lblMarka.setText(marka+" ф "+String.format("%.1f",diam)+" "+spool);
                lblPack.setText(pack);
                lblPart.setText("п. "+numpart+" от "+ DateFormat.format("dd.MM.yy", stringDate).toString()+" ("+src+")");

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }
}

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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DialogAccDataEdtEl extends DialogFragment {

    public DialogAccDataEdtEl(acceptListener aListener) {
        this.aListener = aListener;
    }

    interface acceptListener {
        void accept(int id_part, double kvo, int kvom, int numcont, String barcodecont);
    }
    private final DialogAccDataEdtEl.acceptListener aListener;

    private int id_part;
    private String barcodecont;

    private TextView lblMarka;
    private TextView lblPack;
    private TextView lblPart;
    private TextView lblBarcodeCont;
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
        lblBarcodeCont = v.findViewById(R.id.lblAccNewBarcodeCont);
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
            barcodecont = args.getString("barcodecont");
            if (kvo!=0){
                edtKvo.setText(String.format(Locale.ENGLISH,"%.2f",kvo));
            }
            edtKvoM.setText(String.valueOf(args.getInt("kvom")));
            edtNumCont.setText(String.valueOf(args.getInt("numcont")));
            lblBarcodeCont.setText(barcodecont);
        }

        String queryPart="parti?id=eq."+String.valueOf(id_part)+"&select=n_s,dat_part,diam,elrtr(marka),el_pack(pack_ed,pack_group,mass_ed,mass_group),istoch(nam)";

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
        aListener.accept(id_part,getKvo(),getKvom(),getNumCont(),barcodecont);
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
                double diam=obj.getDouble("diam");
                String marka=obj.getJSONObject("elrtr").getString("marka");

                JSONObject objPack=obj.getJSONObject("el_pack");
                String packEd=objPack.getString("pack_ed");
                String packGroup=objPack.getString("pack_group");
                mas_ed=objPack.getDouble("mass_ed");
                mas_group=objPack.getDouble("mass_group");

                String src=obj.getJSONObject("istoch").getString("nam");

                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                ParsePosition pos = new ParsePosition(0);
                Date stringDate = simpledateformat.parse(datpart,pos);

                lblMarka.setText(marka+" ф "+String.format("%.1f",diam));
                lblPack.setText(packEd+"/"+packGroup);
                lblPart.setText("п. "+numpart+" от "+ DateFormat.format("dd.MM.yy", stringDate).toString()+" ("+src+")");

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }
}

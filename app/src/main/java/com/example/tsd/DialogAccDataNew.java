package com.example.tsd;

import android.os.Bundle;
import android.text.format.DateFormat;
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

public class DialogAccDataNew extends DialogFragment {

    public DialogAccDataNew(acceptListener aListener) {
        this.aListener = aListener;
    }

    interface acceptListener {
        void accept(int id_part, double kvo, int kvom, int numcont);
    }
    private final DialogAccDataNew.acceptListener aListener;

    private int id_part;

    private TextView lblMarka;
    private TextView lblPack;
    private TextView lblPart;
    private EditText edtKvo;
    private EditText edtKvoM;
    private EditText edtNumCont;

    private Button btnSave, btnCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle("Данные поддона");
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.dialog_new_acc_data, null);

        lblMarka = v.findViewById(R.id.lblAccDataNewMarka);
        lblPack = v.findViewById(R.id.lblAccDataNewPack);
        lblPart = v.findViewById(R.id.lblAccDataNewPart);
        edtKvo = v.findViewById(R.id.editTextAccNewKvo);
        edtKvoM = v.findViewById(R.id.editTextAccNewKvoM);
        edtNumCont = v.findViewById(R.id.editTextAccNewNumPal);

        btnSave = v.findViewById(R.id.btnAccDataNewOk);
        btnCancel = v.findViewById(R.id.btnAccDataNewCancel);

        String queryPart="";
        id_part=-1;


        Bundle args = getArguments();
        if (args != null) {
            id_part=args.getInt("idpart");
            edtKvo.setText(String.format(Locale.ENGLISH,"%.2f",args.getDouble("kvo")));
            edtKvoM.setText(String.valueOf(args.getInt("kvom")));
            edtNumCont.setText(String.valueOf(args.getInt("numcont")));
            queryPart=args.getString("querypart");
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
        HttpReq reqGetType = new HttpReq(getPartListener);
        reqGetType.execute(queryPart);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aListener.accept(id_part,Double.valueOf(edtKvo.getText().toString()),Integer.valueOf(edtKvoM.getText().toString()),Integer.valueOf(edtNumCont.getText().toString()));
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return v;
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
                String packEd=obj.getJSONObject("el_pack").getString("pack_ed");
                String packGroup=obj.getJSONObject("el_pack").getString("pack_group");
                String src=obj.getJSONObject("istoch").getString("nam");

                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                ParsePosition pos = new ParsePosition(0);
                Date stringDate = simpledateformat.parse(datpart,pos);

                lblMarka.setText(marka+" ф "+String.format("%.1f",diam));
                lblPack.setText(packEd+"/"+packGroup);
                lblPart.setText("п. "+numpart+" от "+ DateFormat.format("dd.MM.yyyy", stringDate).toString()+" ("+src+")");

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"Не удалось разобрать ответ от сервера", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }
}
package com.example.tsd;

import android.os.Bundle;
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
import androidx.fragment.app.DialogFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DialogAccNew extends DialogFragment  {

    private Button btnSave, btnCancel;
    private EditText edtNum;
    private TextView dateView;
    private Spinner spinner;
    private DateEdit dateEdit;

    public DialogAccNew(acceptListener aListener) {
        this.aListener = aListener;
    }

    public static class AccType {
        String nam;
        int id;
        AccType(String nam, int id) {
            this.nam = nam;
            this.id = id;
        }

        @Override
        public String toString() {
            return this.nam;
        }
    }

    interface acceptListener {
        void accept(String num, Calendar dat, int id_type);
    }
    private final DialogAccNew.acceptListener aListener;

    private List<AccType> list;
    private int currentIdType;
    private String queryType;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);

        list = new ArrayList<>();

        Calendar dat = Calendar.getInstance();
        String num = "";
        currentIdType = -1;
        queryType = "";

        View v = inflater.inflate(R.layout.dialog_new_acc, null);
        btnSave = (Button) v.findViewById(R.id.btnAccNewOk);
        btnCancel = (Button) v.findViewById(R.id.btnAccNewCancel);
        edtNum = (EditText) v.findViewById(R.id.editTextAccNum);
        dateView = (TextView) v.findViewById(R.id.textViewAccDate);
        spinner = (Spinner) v.findViewById(R.id.spinnerAccType);

        dateEdit = new DateEdit(dateView,null);

        Bundle args = getArguments();
        if (args != null) {
            num = args.getString("num");

            String sdat = args.getString("dat");
            SimpleDateFormat simpledateformat = new SimpleDateFormat("dd.MM.yyyy");
            ParsePosition pos = new ParsePosition(0);
            Date stringDate = simpledateformat.parse(sdat,pos);
            dat.setTime(stringDate);

            currentIdType=args.getInt("id_type");

            queryType=args.getString("querytype");
        }

        dateEdit.setDate(dat);

        HttpReq.onPostExecuteListener getTypeListener = new HttpReq.onPostExecuteListener() {
            @Override
            public void postExecute(String resp, String err) {
                if (err.isEmpty()){
                    updList(resp);
                } else {
                    Toast.makeText(getContext(),err, Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        } ;
        HttpReq reqGetType = new HttpReq(getTypeListener);
        reqGetType.execute(queryType);

        edtNum.setText(num);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                AccType item = (AccType) adapterView.getItemAtPosition(i);
                currentIdType=item.id;
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

        edtNum.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode()==KeyEvent.KEYCODE_F1){
                    commit();
                    return true;
                } else if (keyEvent.getKeyCode()==KeyEvent.KEYCODE_F2) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        return v;
    }

    private void commit(){
        aListener.accept(edtNum.getText().toString(),dateEdit.getDate(),currentIdType);
        dismiss();
    }

    private void updList(String jsonResp){
        JSONArray jsonArray = null;
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
                AccType a = new AccType(nam,id);
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
            if (list.get(i).id==currentIdType){
                spinner.setSelection(i);
                break;
            }
        }
        if (currentIdType<0 && list.size()>0){
            AccType item = (AccType) spinner.getSelectedItem();
            currentIdType=item.id;
        }
    }
}

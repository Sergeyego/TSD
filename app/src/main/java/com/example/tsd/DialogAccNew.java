package com.example.tsd;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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

    Button btnSave, btnCancel;
    EditText edtNum;
    TextView dateView;
    Spinner spinner;
    private Calendar date;

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

    private List<AccType> list;
    private int currentIdType;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Title!");
        getDialog().setCanceledOnTouchOutside(false);

        list = new ArrayList<>();

        date = Calendar.getInstance();

        View v = inflater.inflate(R.layout.dialog_new_acc, null);
        btnSave = (Button) v.findViewById(R.id.btnAccNewOk);
        btnCancel = (Button) v.findViewById(R.id.btnAccNewCancel);
        edtNum = (EditText) v.findViewById(R.id.editTextAccNum);
        dateView = (TextView) v.findViewById(R.id.textViewAccDate);
        spinner = (Spinner) v.findViewById(R.id.spinnerAccType);

        String num = "";
        Bundle args = getArguments();
        if (args != null) {
            num = args.getString("num");

            String sdat = args.getString("dat");
            SimpleDateFormat simpledateformat = new SimpleDateFormat("dd.MM.yyyy");
            ParsePosition pos = new ParsePosition(0);
            Date stringDate = simpledateformat.parse(sdat,pos);
            date.setTime(stringDate);

            currentIdType=args.getInt("id_type");
        }

        String query="prod_nakl_tip?en=eq.true&select=id,nam&order=nam";
        new DialogAccNew.HttpTypesGet().execute(query);

        edtNum.setText(num);
        setLblDate();

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                date.set(Calendar.YEAR,year);
                date.set(Calendar.MONTH,monthOfYear);
                date.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                setLblDate();
            }
        };

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        dateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void setLblDate(){
        dateView.setText(DateFormat.format("dd.MM.yyyy", date).toString());
    }

    private void updList(String jsonResp){
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonResp);
        } catch (JSONException e) {
            e.printStackTrace();
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
                e.printStackTrace();
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
    }

    private class HttpTypesGet extends HttpReq{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(getContext(),"resp: "+server_response, Toast.LENGTH_SHORT).show();
            updList(server_response);
        }
    }
}

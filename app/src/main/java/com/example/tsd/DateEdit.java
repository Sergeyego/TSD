package com.example.tsd;

import android.app.DatePickerDialog;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class DateEdit {
    private TextView label;
    private Calendar date;
    public DateEdit(TextView v) {
        label=v;

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

        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(),
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        dateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }

    private void setLblDate(){
        label.setText(DateFormat.format("dd.MM.yy", date).toString());
    }

    public void setDate(Calendar d){
        date=d;
        setLblDate();
    }

    public Calendar getDate() {
        return date;
    }
}

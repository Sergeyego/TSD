package com.example.tsd;

import android.os.Bundle;
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

public class DialogBarcode extends DialogFragment {

    private Button btnCancel;
    private EditText edtBarcode;
    private String message;
    private TextView label;

    public DialogBarcode(String mes, DialogBarcode.acceptListener aListener) {
        this.aListener = aListener;
        this.message=mes;
    }

    interface acceptListener {
        void accept(String barcode);
    }
    private final DialogBarcode.acceptListener aListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setTitle("Отсканируйте штрихкод");
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.dialog_scan, null);

        btnCancel = (Button) v.findViewById(R.id.btnBarcodeCancel);
        label = (TextView) v.findViewById(R.id.lblBarcode);

        label.setText(message);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        edtBarcode = (EditText) v.findViewById(R.id.editTextBarcode);
        edtBarcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                //Toast.makeText(getContext(),keyEvent.toString(), Toast.LENGTH_SHORT).show();
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    aListener.accept(edtBarcode.getText().toString());
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        edtBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode()==KeyEvent.KEYCODE_F2) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        return v;
    }
}

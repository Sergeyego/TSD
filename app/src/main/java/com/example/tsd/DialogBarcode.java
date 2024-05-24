package com.example.tsd;

import android.content.Intent;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class DialogBarcode extends AppCompatActivity {

    private Button btnCancel;
    private Button btnOk;
    private Button btnCamera;
    private EditText edtBarcode;
    private String message;
    private TextView label;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    edtBarcode.setText(result.getContents());
                    accept();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scan);

        Bundle arguments = getIntent().getExtras();
        message = arguments.get("title").toString();

        btnCancel = (Button) findViewById(R.id.btnBarcodeCancel);
        btnCamera = (Button) findViewById(R.id.btnBarcodeCam);
        btnOk = findViewById(R.id.btnBarcodeOk);
        label = (TextView) findViewById(R.id.lblBarcode);

        label.setText(message);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanOptions options = new ScanOptions();
                options.setCaptureActivity(CaptureActivityPortrait.class);
                options.setOrientationLocked(true);
                barcodeLauncher.launch(options);
            }
        });

        edtBarcode = (EditText) findViewById(R.id.editTextBarcode);
        edtBarcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                //Toast.makeText(DialogBarcode.this,keyEvent.toString(), Toast.LENGTH_SHORT).show();
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    accept();
                    return true;
                }
                return false;
            }
        });

        edtBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                //Toast.makeText(getApplicationContext(),keyEvent.toString(), Toast.LENGTH_SHORT).show();
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_F2) {
                    finish();
                    return true;
                } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_F1){
                    accept();
                    return true;
                }
                return false;
            }
        });

    }

    private void accept(){
        Intent intent = new Intent();
        intent.putExtra("barcode", edtBarcode.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

}



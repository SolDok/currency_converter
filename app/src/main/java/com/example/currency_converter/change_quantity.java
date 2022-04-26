package com.example.currency_converter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;

import java.util.Dictionary;

public class change_quantity extends AppCompatActivity {

    TextView quantityValutaName = null;
    TextView quantityValuteInput = null;
    TextView quantityValuteOutput = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_quantity);
        quantityValutaName = findViewById(R.id.quantityValutaName);
        quantityValuteInput = findViewById(R.id.quantityValutaInput);
        quantityValuteOutput = findViewById(R.id.quantityValutaOutput);
        Bundle argument = getIntent().getExtras();
        String valuteName = argument.get("Name").toString();
        Double valuteValue = (Double)  argument.get("Value");
        quantityValutaName.setText(valuteName + " = ");
        quantityValuteOutput.setText(valuteValue.toString());
        quantityValuteInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (quantityValuteInput.getText().toString().isEmpty()) return;
                Float input = Float.parseFloat(charSequence.toString());
                Double result =  input * valuteValue;
                quantityValuteOutput.setText(String.format("%.2f", result) + "RUB");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
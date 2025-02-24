package com.hepta.gdbinject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("entry");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.btn_debug);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGdbServer();
            }
        });
    }

    native public void startGdbServer();
}
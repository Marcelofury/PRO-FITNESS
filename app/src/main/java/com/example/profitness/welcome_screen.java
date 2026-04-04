package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class welcome_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnLogin = findViewById(R.id.btnLogin);

        if (btnGetStarted == null || btnLogin == null) {
            Toast.makeText(this, "Screen setup error", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGetStarted.setEnabled(true);
        btnLogin.setEnabled(true);
        btnGetStarted.setClickable(true);
        btnLogin.setClickable(true);

        btnGetStarted.setOnClickListener(v -> openLogin());
        btnLogin.setOnClickListener(v -> openLogin());
    }

    private void openLogin() {
        startActivity(new Intent(this, login.class));
    }
}

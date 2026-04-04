package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class welcome_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnGetStarted.setOnClickListener(v -> startActivity(new Intent(this, login.class)));
        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, login.class)));
    }
}

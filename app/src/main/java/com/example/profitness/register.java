package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.gson.JsonObject;

public class register extends AppCompatActivity {

    private EditText etFullName;
    private EditText etEmail;
    private EditText etPass;
    private EditText etConfirm;
    private Button btnCreate;
    private ProFitnessApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        api = new ProFitnessApi(new TokenStore(this));

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        etConfirm = findViewById(R.id.etConfirm);
        btnCreate = findViewById(R.id.btnCreate);
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        btnBack.setOnClickListener(v -> finish());
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(register.this, login.class));
            finish();
        });
        btnCreate.setOnClickListener(v -> performRegister());
    }

    private void performRegister() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString();
        String confirmPassword = etConfirm.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter your full name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        api.register(name, email, password, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(register.this, register2.class));
                    finish();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(register.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnCreate.setEnabled(!loading);
        btnCreate.setText(loading ? "Creating..." : "Create Account →");
    }
}

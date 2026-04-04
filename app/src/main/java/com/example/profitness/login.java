package com.example.profitness;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.profitness.network.ApiCallback;
import com.example.profitness.network.ProFitnessApi;
import com.example.profitness.network.TokenStore;
import com.google.gson.JsonObject;

public class login extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private ProFitnessApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        api = new ProFitnessApi(new TokenStore(this));

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> showRegisterDialog());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        api.login(email, password, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(login.this, "Login successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(login.this, Home_dashboard.class));
                    finish();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(login.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showRegisterDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etName = new EditText(this);
        etName.setHint("Name");

        EditText etEmailDialog = new EditText(this);
        etEmailDialog.setHint("Email");
        etEmailDialog.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        EditText etPasswordDialog = new EditText(this);
        etPasswordDialog.setHint("Password (min 6 chars)");
        etPasswordDialog.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        container.addView(etName);
        container.addView(etEmailDialog);
        container.addView(etPasswordDialog);

        new AlertDialog.Builder(this)
                .setTitle("Create Account")
                .setView(container)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Register", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmailDialog.getText().toString().trim();
                    String password = etPasswordDialog.getText().toString();

                    if (name.isEmpty() || email.isEmpty() || password.length() < 6) {
                        Toast.makeText(this, "Enter valid registration details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setLoading(true);
                    api.register(name, email, password, new ApiCallback<JsonObject>() {
                        @Override
                        public void onSuccess(JsonObject result) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(login.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(login.this, register2.class));
                                finish();
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(login.this, errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .show();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Loading..." : "Login");
    }
}

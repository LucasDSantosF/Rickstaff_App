package com.rickstaff.app.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rickstaff.app.R;
import com.rickstaff.app.data.local.SessionManager;
import com.rickstaff.app.ui.menu.MenuActivity;

public class RegisterActivity extends AppCompatActivity {
    private RegisterViewModel viewModel;
    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        viewModel.getIsLoading().observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnRegister.setEnabled(!loading);
        });

        viewModel.getRegisterError().observe(this, error -> {
            tvError.setText(error);
            tvError.setVisibility(View.VISIBLE);
        });

        viewModel.getRegisterSuccess().observe(this, response -> {
            SessionManager.getInstance(this).saveSession(
                    response.getUser().getId(),
                    response.getToken(),
                    response.getUser().getNome()
            );
            startActivity(new Intent(this, MenuActivity.class));

            Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            viewModel.registerUser(
                etName.getText().toString(),
                etEmail.getText().toString(),
                etPassword.getText().toString()
            );
        });
    }
}

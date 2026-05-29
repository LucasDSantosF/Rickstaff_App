package com.rickstaff.app.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.rickstaff.app.R;
import com.rickstaff.app.data.local.SessionManager;
import com.rickstaff.app.ui.menu.MenuActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private TextInputEditText etEmail, etSenha;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getIsLoading().observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!loading);
        });

        viewModel.getLoginSuccess().observe(this, response -> {
            SessionManager.getInstance(this).saveSession(
                response.getUser().getId(),
                response.getToken(),
                response.getUser().getNome()
            );
            startActivity(new Intent(this, MenuActivity.class));
            finish();
        });

        viewModel.getLoginError().observe(this, error -> {
            tvError.setText(error);
            tvError.setVisibility(View.VISIBLE);
        });

        btnLogin.setOnClickListener(v -> {
            tvError.setVisibility(View.GONE);
            viewModel.login(
                Objects.requireNonNull(etEmail.getText()).toString().trim(),
                Objects.requireNonNull(etSenha.getText()).toString().trim()
            );
        });
    }
}

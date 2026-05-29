package com.rickstaff.app.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.rickstaff.app.R;
import com.rickstaff.app.data.local.SessionManager;
import com.rickstaff.app.ui.characters.CharacterListActivity;
import com.rickstaff.app.ui.employees.EmployeeListActivity;
import com.rickstaff.app.ui.login.LoginActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        MaterialCardView btnPersonagens = findViewById(R.id.btnPersonagens);
        MaterialCardView btnFuncionarios = findViewById(R.id.btnFuncionarios);
        Button btnLogout = findViewById(R.id.btnLogout);

        String userName = SessionManager.getInstance(this).getUserName();
        String menu_hello = getString(R.string.menu_hello);
        String menu_user = getString(R.string.menu_user);
        String welcome = menu_hello + (userName != null ? userName : menu_user) + "!";

        tvWelcome.setText(welcome);

        btnPersonagens.setOnClickListener(v ->
            startActivity(new Intent(this, CharacterListActivity.class)));

        btnFuncionarios.setOnClickListener(v ->
            startActivity(new Intent(this, EmployeeListActivity.class)));

        btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance(this).clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
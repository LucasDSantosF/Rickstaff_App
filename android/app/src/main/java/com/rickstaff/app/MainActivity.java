package com.rickstaff.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.rickstaff.app.ui.login.LoginActivity;
import com.rickstaff.app.ui.register.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEntrar = findViewById(R.id.btnEntrar);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        btnEntrar.setOnClickListener(v ->
            startActivity(new Intent(this, LoginActivity.class))
        );

        btnRegistrar.setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
package com.rickstaff.app.ui.employees;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.rickstaff.app.R;
import com.rickstaff.app.data.model.Employee;

import java.util.Objects;

public class EmployeeFormActivity extends AppCompatActivity {

    private EmployeeViewModel viewModel;
    private TextInputEditText etNome, etEmail, etCargo, etSalario;
    private Switch switchAtivo;
    private Employee employeeToEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_form);

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etCargo = findViewById(R.id.etCargo);
        etSalario = findViewById(R.id.etSalario);
        switchAtivo = findViewById(R.id.switchAtivo);
        Button btnSalvar = findViewById(R.id.btnSalvar);

        viewModel = new ViewModelProvider(this).get(EmployeeViewModel.class);

        String employeeJson = getIntent().getStringExtra("employeeJson");
        if (employeeJson != null) {
            employeeToEdit = new Gson().fromJson(employeeJson, Employee.class);
            populateFields(employeeToEdit);
            setTitle(getString(R.string.employee_editar));
        } else {
            setTitle(getString(R.string.employee_novo));
        }

        viewModel.getActionSuccess().observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            finish();
        });

        viewModel.getError().observe(this, err ->
            Toast.makeText(this, err, Toast.LENGTH_LONG).show());

        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void populateFields(Employee emp) {
        etNome.setText(emp.getNome());
        etEmail.setText(emp.getEmail());
        etCargo.setText(emp.getCargo());
        etSalario.setText(String.valueOf(emp.getSalario()));
        switchAtivo.setChecked(emp.isAtivo());
    }

    private void salvar() {
        String nome = Objects.requireNonNull(etNome.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String cargo = Objects.requireNonNull(etCargo.getText()).toString().trim();
        String salarioStr = Objects.requireNonNull(etSalario.getText()).toString().trim();

        if (nome.isEmpty() || email.isEmpty() || cargo.isEmpty() || salarioStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.employee_dados_faltando), Toast.LENGTH_SHORT).show();
            return;
        }

        Employee emp = employeeToEdit != null ? employeeToEdit : new Employee();
        emp.setNome(nome);
        emp.setEmail(email);
        emp.setCargo(cargo);
        emp.setSalario(Double.parseDouble(salarioStr));
        emp.setAtivo(switchAtivo.isChecked());

        viewModel.save(emp);
    }
}
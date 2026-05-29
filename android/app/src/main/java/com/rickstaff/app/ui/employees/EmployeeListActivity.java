package com.rickstaff.app.ui.employees;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.rickstaff.app.R;
import com.rickstaff.app.data.model.Employee;

public class EmployeeListActivity extends AppCompatActivity {

    private EmployeeViewModel viewModel;
    private EmployeeAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvError = findViewById(R.id.tvError);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        adapter = new EmployeeAdapter(new EmployeeAdapter.OnActionListener() {
            @Override
            public void onEdit(Employee employee) {
                Intent intent = new Intent(EmployeeListActivity.this, EmployeeFormActivity.class);
                intent.putExtra("employeeJson", new Gson().toJson(employee));
                startActivity(intent);
            }

            @Override
            public void onDelete(Employee employee) {
                new AlertDialog.Builder(EmployeeListActivity.this)
                        .setTitle(getString(R.string.employee_Excluir))
                        .setMessage(getString(R.string.employee_pergunta_excluir) + employee.getNome() + "?")
                        .setPositiveButton(getString(R.string.employee_btn_sim), (d, w) -> viewModel.delete(employee.getId()))
                        .setNegativeButton(getString(R.string.employee_btn_nao), null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(EmployeeViewModel.class);

        viewModel.getEmployees().observe(this, list -> {
            adapter.updateList(list);
            tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsLoading().observe(this, loading ->
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getError().observe(this, err -> {
            tvError.setText(err);
            tvError.setVisibility(View.VISIBLE);
        });

        viewModel.getActionSuccess().observe(this, msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        fabAdd.setOnClickListener(v ->
            startActivity(new Intent(this, EmployeeFormActivity.class)));

        viewModel.loadAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadAll();
    }
}
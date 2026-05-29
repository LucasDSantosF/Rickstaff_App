package com.rickstaff.app.ui.characters;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rickstaff.app.R;
import com.rickstaff.app.ui.profile.ProfileActivity;
import com.google.gson.Gson;

public class CharacterListActivity extends AppCompatActivity {

    private CharacterListViewModel viewModel;
    private CharacterAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvError;
    private Spinner spinStatus, spinGender;
    private EditText etSpecies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_list);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvError = findViewById(R.id.tvError);
        spinStatus = findViewById(R.id.spinStatus);
        spinGender = findViewById(R.id.spinGender);
        etSpecies = findViewById(R.id.etSpecies);

        setupSpinners();

        adapter = new CharacterAdapter(character -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("characterJson", new Gson().toJson(character));
            startActivity(intent);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        NestedScrollView scrollView = findViewById(R.id.nestedScrollView);
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                        viewModel.loadNextPage(this.getApplicationContext());
                    }
                });

        viewModel = new ViewModelProvider(this).get(CharacterListViewModel.class);

        viewModel.getCharacters().observe(this, list -> adapter.updateList(list));
        viewModel.getIsLoading().observe(this, loading ->
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(this, err -> {
            tvError.setText(err);
            tvError.setVisibility(View.VISIBLE);
        });
        viewModel.getIsEmpty().observe(this, empty ->
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE));

        findViewById(R.id.btnFilter).setOnClickListener(v -> applyFilters());

        viewModel.loadFirstPage(this.getApplicationContext());
    }

    private void setupSpinners() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
            R.layout.item_spinner,
            new String[]{"Todos", "Alive", "Dead", "unknown"});
        statusAdapter.setDropDownViewResource(R.layout.item_spinner);
        spinStatus.setAdapter(statusAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
            R.layout.item_spinner,
            new String[]{"Todos", "Female", "Male", "Genderless", "unknown"});
        genderAdapter.setDropDownViewResource(R.layout.item_spinner);
        spinGender.setAdapter(genderAdapter);
    }

    private void applyFilters() {
        String filter_all = getString(R.string.character_filter_all);
        String status = spinStatus.getSelectedItem().toString().equals(filter_all) ? ""
            : spinStatus.getSelectedItem().toString();
        String gender = spinGender.getSelectedItem().toString().equals(filter_all) ? ""
            : spinGender.getSelectedItem().toString();
        String species = etSpecies.getText().toString().trim();
        tvError.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        viewModel.applyFilters(this.getApplicationContext(), status, gender, species);
    }
}
package com.rickstaff.app.ui.profile;

import static com.rickstaff.app.util.Constants.POST_SIMULATE_URL;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.rickstaff.app.R;
import com.rickstaff.app.data.local.SessionManager;
import com.rickstaff.app.data.model.Character;
import com.rickstaff.app.data.remote.ApiClient;
import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivPhoto;
    private Uri photoUri;
    private Character character;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
        registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && photoUri != null && character != null) {
                SessionManager.getInstance(this)
                    .saveCapturedImageUri(character.getId(), photoUri.toString());

                Glide.with(this)
                        .load(photoUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .into(ivPhoto);

                Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show();
                simulatePost();
            }
        });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) launchCamera();
            else Toast.makeText(this, "Permissão de câmera necessária", Toast.LENGTH_SHORT).show();
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivPhoto = findViewById(R.id.ivPhoto);
        ImageButton btnCamera = findViewById(R.id.btnCamera);

        String characterJson = getIntent().getStringExtra("characterJson");
        if (characterJson != null) {
            character = new Gson().fromJson(characterJson, Character.class);
            bindCharacter(character);

            String savedUri = SessionManager.getInstance(this)
                    .getCapturedImageUri(character.getId());
            if (savedUri != null) {
                photoUri = Uri.parse(savedUri);

                Glide.with(this)
                        .load(photoUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .into(ivPhoto);
            }
        }

        btnCamera.setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        File photoFile = new File(getCacheDir(),
        "captured_" + System.currentTimeMillis() + ".jpg");
        photoUri = FileProvider.getUriForFile(this,
        getPackageName() + ".fileprovider", photoFile);
        takePictureLauncher.launch(photoUri);
    }

    private void simulatePost() {
        if (character == null) return;
        new Thread(() -> {
            try {
                MediaType JSON = MediaType.get("application/json");
                String body = "{"
                        + "\"characterId\":" + character.getId() + ","
                        + "\"characterName\":\"" + character.getName() + "\","
                        + "\"capturedImageUri\":\"" + photoUri.toString() + "\","
                        + "\"capturedAt\":\"" + new java.util.Date() + "\","
                        + "\"source\":\"camera\""
                        + "}";

                Request request = new Request.Builder()
                        .url(POST_SIMULATE_URL)
                        .post(RequestBody.create(body, JSON))
                        .build();

                Response response = ApiClient.getClient().newCall(request).execute();

                String msg_sucesso = getString(R.string.profile_sucesso);
                String msg_erro = getString(R.string.profile_erro);

                runOnUiThread(() -> Toast.makeText(this,
                    response.isSuccessful() ? msg_sucesso : msg_erro,
                    Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this,
                "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void bindCharacter(Character c) {
        String id = getString(R.string.profile_id) + c.getId();
        String status = getString(R.string.profile_status) + c.getStatus();
        String especie = getString(R.string.profile_especie) + c.getSpecies();
        String tipo = getString(R.string.profile_tipo) + (c.getType().isEmpty() ? "-" : c.getType());
        String genero = getString(R.string.profile_genero) + c.getGender();
        String origem = getString(R.string.profile_origem)
                + (c.getOrigin() != null ? c.getOrigin().getName() : "-");
        String localizacao = getString(R.string.profile_localizacao)
                + (c.getLocation() != null ? c.getLocation().getName() : "-");
        String episodios = getString(R.string.profile_episodios) + c.getEpisodeCount();
        String url = getString(R.string.profile_url) + c.getUrl();
        String criacao = getString(R.string.profile_criacao) + c.getCreated();

        Glide.with(this).load(c.getImage()).into(ivPhoto);
        ((TextView) findViewById(R.id.tvId)).setText(id);
        ((TextView) findViewById(R.id.tvName)).setText(c.getName());
        ((TextView) findViewById(R.id.tvStatus)).setText(status);
        ((TextView) findViewById(R.id.tvSpecies)).setText(especie);
        ((TextView) findViewById(R.id.tvType)).setText(tipo);
        ((TextView) findViewById(R.id.tvGender)).setText(genero);
        ((TextView) findViewById(R.id.tvOrigin)).setText(origem);
        ((TextView) findViewById(R.id.tvLocation)).setText(localizacao);
        ((TextView) findViewById(R.id.tvEpisodes)).setText(episodios);
        ((TextView) findViewById(R.id.tvUrl)).setText(url);
        ((TextView) findViewById(R.id.tvCreated)).setText(criacao);
    }
}
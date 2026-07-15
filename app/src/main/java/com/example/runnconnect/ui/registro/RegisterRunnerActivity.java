package com.example.runnconnect.ui.registro;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterRunnerActivity extends AppCompatActivity {
  private RegisterRunnerViewModel viewModel;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaPicker;

  // UI
  private EditText etNombre, etApellido, etEmail, etPassword, etConfirm;
  private ImageView ivAvatar;
  private TextView tvError, tvVolver;
  private Button btnRegistrar;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register); // Asegúrate que este es el XML del Runner

    viewModel = new ViewModelProvider(this).get(RegisterRunnerViewModel.class);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle("Cuenta Runner");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    initViews();
    setupPickMedia();
    setupObservers();
    setupListeners();
  }

  private void initViews() {
    etNombre = findViewById(R.id.etNombre);
    etApellido = findViewById(R.id.etApellido);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    etConfirm = findViewById(R.id.etConfirmPassword);

    ivAvatar = findViewById(R.id.ivAvatar);
    tvError = findViewById(R.id.tvErrorRegister);
    tvVolver = findViewById(R.id.tvVolverLogin);
    btnRegistrar = findViewById(R.id.btnRegistrar);
    progressBar = findViewById(R.id.progressBar);
  }

  private void setupPickMedia() {
    mediaPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      if (uri != null) viewModel.onAvatarSelected(uri);
    });
  }

  private void setupObservers() {
    // Avatar seleccionado
    viewModel.getAvatarUri().observe(this, uri -> {
      Glide.with(this).load(uri).circleCrop().into(ivAvatar);
    });

    // Errores
    viewModel.getErrorMessage().observe(this, msg -> {
      if (msg != null && !msg.isEmpty()) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        tvError.setAlpha(0f);
        tvError.animate().alpha(1f).setDuration(300).start();
      } else {
        tvError.setVisibility(View.GONE);
      }
    });

    // Loading
    viewModel.getIsLoading().observe(this, loading -> {
      progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      btnRegistrar.setEnabled(!loading);
      if(loading) tvError.setVisibility(View.GONE);
    });

    // Navegación (Éxito)
    viewModel.getNavigateToMain().observe(this, intent -> {
      startActivity(intent);
      finish();
    });
  }

  private void setupListeners() {
    ivAvatar.setOnClickListener(v ->
      mediaPicker.launch(new PickVisualMediaRequest.Builder()
        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
        .build()));

    btnRegistrar.setOnClickListener(v -> {
      viewModel.registrar(
        etNombre.getText().toString().trim(),
        etApellido.getText().toString().trim(),
        etEmail.getText().toString().trim(),
        etPassword.getText().toString().trim(),
        etConfirm.getText().toString().trim()
      );
    });

    tvVolver.setOnClickListener(v -> finish());
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}

package com.example.runnconnect.ui.registro;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.runnconnect.MainActivity;
import com.example.runnconnect.databinding.ActivityRegisterOrganizadorBinding;

public class RegisterOrganizadorActivity extends AppCompatActivity {
  private ActivityRegisterOrganizadorBinding binding;
  private RegisterOrganizadorViewModel viewModel;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaPicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRegisterOrganizadorBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    viewModel = new ViewModelProvider(this).get(RegisterOrganizadorViewModel.class);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle("Cuenta Organizador");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    abriGaleria();
    setupObservers();
    setupListeners();
  }

  private void abriGaleria() {
    mediaPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      if (uri != null) viewModel.onAvatarSelected(uri);
    });
  }

  private void setupObservers() {
    // Avatar seleccionado
    viewModel.getAvatarUri().observe(this, uri ->
      Glide.with(this).load(uri).circleCrop().into(binding.ivAvatar)
    );

    // Errores pre-procesados desde el ViewModel
    viewModel.getErrorMessage().observe(this, binding.tvErrorRegister::setText);
    viewModel.getErrorVisibility().observe(this, binding.tvErrorRegister::setVisibility);

    // Loading y disponibilidad del botón
    viewModel.getIsLoading().observe(this, loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnRegistrar.setEnabled(!loading);
    });

    // Navegación (Éxito)
    viewModel.getRegistroExitoso().observe(this, exitoso -> {
      if (Boolean.TRUE.equals(exitoso)) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
      }
    });
  }

  private void setupListeners() {
    binding.ivAvatar.setOnClickListener(v ->
      mediaPicker.launch(new PickVisualMediaRequest.Builder()
        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
        .build()));

    binding.btnRegistrar.setOnClickListener(v -> {
      viewModel.registrar(
        binding.etRazonSocial.getText().toString().trim(),
        binding.etNombreComercial.getText().toString().trim(),
        binding.etEmail.getText().toString().trim(),
        binding.etPassword.getText().toString().trim(),
        binding.etConfirmPassword.getText().toString().trim()
      );
    });

    binding.tvVolverLogin.setOnClickListener(v -> finish());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}

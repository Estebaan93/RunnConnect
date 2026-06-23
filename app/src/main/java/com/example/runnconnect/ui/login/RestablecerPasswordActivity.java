package com.example.runnconnect.ui.login;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.databinding.ActivityRestablecerPasswordBinding;

public class RestablecerPasswordActivity extends AppCompatActivity {
  private LoginViewModel viewModel;
  private ActivityRestablecerPasswordBinding binding;
  private String tokenRecuperacion; //guardamos el token que viene de la URL email


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRestablecerPasswordBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

    configurarActionBar();

    capturarToken(); //mandamo al vm el token
    setupObservers();
    setupListeners();
  }

  private void configurarActionBar() {
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Muestra la flecha de volver
      getSupportActionBar().setTitle("Nueva Contraseña");    // Titulo en la barra
    }
  }

  private void capturarToken() {
    Uri data = getIntent().getData();
    if (data != null) {
      tokenRecuperacion = data.getQueryParameter("token");
    }
    //enviamos al vm
    viewModel.verificarTokenRecuperacion(tokenRecuperacion);

  }

  private void setupObservers() {
    // Observar estado de carga
    viewModel.getIsLoading().observe(this, isLoading -> {
      binding.pbResetPassword.setVisibility(isLoading ? View.VISIBLE : View.GONE);
      binding.btnGuardarPassword.setEnabled(!isLoading);
    });

    // Conectar el exito a la UI
    viewModel.getExito().observe(this, binding.tvExitoReset::setText);
    viewModel.getExitoVisibility().observe(this, binding.tvExitoReset::setVisibility);

    // Conectar el Error a la UI
    viewModel.getErrorMessage().observe(this, binding.tvErrorReset::setText);
    viewModel.getErrorVisibility().observe(this, binding.tvErrorReset::setVisibility);


    //nuevo 21-06
    viewModel.getNavegacionEvento().observe(this, this::startActivity);
    viewModel.getFinalUser().observe(this, finish -> {
      if (Boolean.TRUE.equals(finish)) {
        finish();
      }
    });
  }

  private void setupListeners() {
    binding.btnGuardarPassword.setOnClickListener(v -> {
      String pass1 = binding.etNuevaPassword.getText().toString().trim();
      String pass2 = binding.etConfirmarPassword.getText().toString().trim();

      //delega al ViewModel
      viewModel.ejecutarRestablecerPassword(tokenRecuperacion, pass1, pass2);
    });
  }

  // METODOS DE NAVEGACION

  // Este atrapa la flecha de la barra morada
  @Override
  public boolean onSupportNavigateUp() {
    viewModel.volverAtrasClick();
    return true;
  }

  // Este atrapa el boton "atras" del sistema (navegacion del celular)
  @Override
  public void onBackPressed() {
    viewModel.volverAtrasClick();
  }


}





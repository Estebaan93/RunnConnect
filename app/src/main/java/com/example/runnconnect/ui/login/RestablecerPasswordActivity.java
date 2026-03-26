package com.example.runnconnect.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.R;

public class RestablecerPasswordActivity extends AppCompatActivity {
  private LoginViewModel viewModel;
  private String tokenRecuperacion; //guardamos el token que viene de la URL email

  private EditText etNuevaPassword, etConfirmarPassword;
  private Button btnGuardarPassword;
  private ProgressBar pbResetPassword;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_restablecer_password);

    viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

    configurarActionBar();

    initViews();
    capturarToken();
    setupObservers();
    setupListeners();
  }

  private void configurarActionBar() {
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Muestra la flecha de volver
      getSupportActionBar().setTitle("Nueva Contraseña");    // Título en la barra
    }
  }

  private void initViews() {
    etNuevaPassword = findViewById(R.id.etNuevaPassword);
    etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
    btnGuardarPassword = findViewById(R.id.btnGuardarPassword);
    pbResetPassword = findViewById(R.id.pbResetPassword);
  }

  private void capturarToken() {
    Uri data = getIntent().getData();
    if (data != null) {
      tokenRecuperacion = data.getQueryParameter("token");
    }

    // Si alguien abre la activity sin un token, la cerramos por seguridad
    if (tokenRecuperacion == null || tokenRecuperacion.isEmpty()) {
      Toast.makeText(this, "Enlace inválido o corrupto", Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private void setupObservers() {
    // Observar estado de carga
    viewModel.getIsLoading().observe(this, isLoading -> {
      pbResetPassword.setVisibility(isLoading ? View.VISIBLE : View.GONE);
      btnGuardarPassword.setEnabled(!isLoading);
    });

    // Observar exito (Si tod sale bien, cerramos esta pantalla y volvemos al Login)
    viewModel.getExito().observe(this, mensaje -> {
      if (mensaje != null && mensaje.contains("actualizada")) {
        Toast.makeText(this, "¡Contraseña actualizada con éxito!", Toast.LENGTH_LONG).show();

        // 1. Creamos el Intent hacia el Login
        Intent intent = new Intent(this, LoginActivity.class);

        // 2. Limpiamos el historial de navegación (Corta el lazo con Gmail)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 3. Iniciamos la actividad
        startActivity(intent);
      }
    });

    // Observar errores
    viewModel.getErrorMessage().observe(this, error -> {
      if (error != null) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });
  }

  private void setupListeners() {
    btnGuardarPassword.setOnClickListener(v -> {
      String pass1 = etNuevaPassword.getText().toString().trim();
      String pass2 = etConfirmarPassword.getText().toString().trim();

      // La vista no piensa, solo delega al ViewModel
      viewModel.ejecutarRestablecerPassword(tokenRecuperacion, pass1, pass2);
    });
  }

  // --- MÉTODOS DE NAVEGACIÓN ---

  // Este atrapa la flecha de la barra morada
  @Override
  public boolean onSupportNavigateUp() {
    volverAlLogin();
    return true;
  }

  // Este atrapa el botón "Atrás" del sistema (navegación del celular)
  @Override
  public void onBackPressed() {
    volverAlLogin();
  }

  // Rompe el vínculo con el email y lanza el Login limpio
  private void volverAlLogin() {
    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }




}





package com.example.runnconnect.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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

  //agregamos las vistas
  private TextView tvErrorReset, tvExitoReset;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_restablecer_password);

    viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

    configurarActionBar();

    initViews();
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

  private void initViews() {
    etNuevaPassword = findViewById(R.id.etNuevaPassword);
    etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
    btnGuardarPassword = findViewById(R.id.btnGuardarPassword);
    pbResetPassword = findViewById(R.id.pbResetPassword);

    //inicializamos los txtView
    tvErrorReset= findViewById(R.id.tvErrorReset);
    tvExitoReset= findViewById(R.id.tvExitoReset);
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
      pbResetPassword.setVisibility(isLoading ? View.VISIBLE : View.GONE);
      btnGuardarPassword.setEnabled(!isLoading);
    });

    // Conectar el exito a la UI
    viewModel.getExito().observe(this, tvExitoReset::setText);
    viewModel.getExitoVisibility().observe(this, tvExitoReset::setVisibility);

    // Conectar el Error a la UI
    viewModel.getErrorMessage().observe(this, tvErrorReset::setText);
    viewModel.getErrorVisibility().observe(this, tvErrorReset::setVisibility);



    // nuevo: observar instrucciones de navegacion del ViewModel
    viewModel.getNavegarAlLogin().observe(this, debeNavegar -> {
      if (debeNavegar != null && debeNavegar) {
        volverAlLogin();
        viewModel.navegacionALoginCompletada(); // Le avisamos que ya cumplimos
      }
    });
  }

  private void setupListeners() {
    btnGuardarPassword.setOnClickListener(v -> {
      String pass1 = etNuevaPassword.getText().toString().trim();
      String pass2 = etConfirmarPassword.getText().toString().trim();

      //delega al ViewModel
      viewModel.ejecutarRestablecerPassword(tokenRecuperacion, pass1, pass2);
    });
  }

  // METODOS DE NAVEGACION

  // Este atrapa la flecha de la barra morada
  @Override
  public boolean onSupportNavigateUp() {
    volverAlLogin();
    return true;
  }

  // Este atrapa el boton "atras" del sistema (navegacion del celular)
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





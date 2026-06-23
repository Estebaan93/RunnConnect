package com.example.runnconnect.ui.login;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.R;

public class ReactivarCuentaActivity extends AppCompatActivity {
  private LoginViewModel viewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reactivate_loading); // Un layout con un ProgressBar

    viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

    // capturar el Intent y extraer el token de forma directa
    String token = extraerTokenDelIntent();

    // le pasamos al ViewModel
    viewModel.confirmarReactivacionFinal(token);

    // observadores
    setupObservers();
  }

  private String extraerTokenDelIntent() {
    Uri data = getIntent().getData();
    if (data != null) {
      return data.getQueryParameter("token");
    }
    return null; // Si no hay link, devolvemos null y que el ViewModel se encargue
  }

  //nuevo 21-06
  private void setupObservers() {
    // Navegacion (por ejemplo, a MainActivity tras reactivar)
    viewModel.getNavegacionEvento().observe(this, this::startActivity);

    // Cerrar la Activity solo si el ViewModel lo indica
    viewModel.getFinalUser().observe(this, finish -> {
      if (Boolean.TRUE.equals(finish)) {
        finish();
      }
    });
  }

}
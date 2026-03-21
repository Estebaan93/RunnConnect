package com.example.runnconnect.ui.login;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

    // 1. Capturar el Intent y la Data (la URL)
    Uri data = getIntent().getData();
    if (data != null) {
      // Extraer el parametro "token" de la URL
      String token = data.getQueryParameter("token");
      if (token != null) {
        // 2. Llamar al ViewModel para procesar la reactivacion
        viewModel.confirmarReactivacionFinal(token);
      }

    }

    // 3. Observar la navegacion (si tiene exito, ira al MainActivity)
    viewModel.getNavegacionEvento().observe(this, intent -> {
      startActivity(intent);
      finish();
    });

    viewModel.getErrorMessage().observe(this, error -> {
      if (error != null) {
        // Si hay error, mostramos un Toast y cerramos para volver al Login
        Log.d("ErrorTokenReactivacion", "VER ERROR: "+error);
        finish();
      }
    });

  }



}

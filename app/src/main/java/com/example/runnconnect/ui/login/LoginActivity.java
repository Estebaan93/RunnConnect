//ui/login/LoginActivity
package com.example.runnconnect.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.MainActivity;
import com.example.runnconnect.R;
import com.example.runnconnect.ui.eventosPublicos.EventosPublicosActivity;
import com.google.android.material.textfield.TextInputEditText;


public class LoginActivity extends AppCompatActivity {

  private LoginViewModel viewModel;

  // UI
  private VideoView videoBackground;
  private TextInputEditText etEmail, etPassword;
  private Button btnLogin, btnVisitante;
  private TextView tvCrearCuenta;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Inicializar ViewModel
    viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

    // Vincular Vistas
    initViews();

    // Configurar Video de Fondo
    setupVideoBackground();

    // Observar al ViewModel
    setupObservers();

    // Listeners de Botones
    setupListeners();
  }

  private void initViews() {
    videoBackground = findViewById(R.id.videoBackground);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    btnLogin = findViewById(R.id.btnLogin);
    btnVisitante = findViewById(R.id.btnVisitante);
    tvCrearCuenta = findViewById(R.id.tvCrearCuenta);
    progressBar = findViewById(R.id.progressBar);
  }

  private void setupVideoBackground() {
    //El video en res/raw/background_video.mp4
    try {
      Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background_video_login);
      videoBackground.setVideoURI(uri);

      videoBackground.setOnPreparedListener(mp -> {
        mp.setLooping(true); // Loop infinito

        // Escalado para cubrir pantalla sin bordes negros
        float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
        float screenRatio = videoBackground.getWidth() / (float) videoBackground.getHeight();
        float scaleX = videoRatio / screenRatio;
        if (scaleX >= 1f) videoBackground.setScaleX(scaleX);
        else videoBackground.setScaleY(1f / scaleX);
      });

      videoBackground.start();
    } catch (Exception e) {
      e.printStackTrace(); // Si falla el video, la app sigue funcionando
    }
  }

  private void setupObservers() {
    // observar cargando
    viewModel.getIsLoading().observe(this, isLoading -> {
      progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
      btnLogin.setEnabled(!isLoading); // desactivar boton mientras carga
    });

    // Observar Mensajes de Error
    viewModel.getErrorMessage().observe(this, msg -> {
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    });

    // Observar navegar
    viewModel.getNavegacionEvento().observe(this, intent -> {
      startActivity(intent);
      //si es visitante y consulta eventos publicos
      if(!(intent.getComponent().getClassName().contains("EventosPublicosActivity"))){
        finish();
      }
    });
  }

  private void setupListeners() {
    btnLogin.setOnClickListener(v -> {
      String email = etEmail.getText().toString().trim();
      String password = etPassword.getText().toString().trim();
      viewModel.login(email, password);
    });

    btnVisitante.setOnClickListener(v -> {
      // Ir a pantalla publica
      viewModel.esVisitanteClicked();
    });

    //Crear cuenta
    tvCrearCuenta.setOnClickListener(v -> {
      Toast.makeText(this, "Próximamente: Registro", Toast.LENGTH_SHORT).show();
    });
  }


  @Override
  protected void onResume() {
    super.onResume();
    if (videoBackground != null) videoBackground.start();
  }
}
//ui/login/LoginActivity
package com.example.runnconnect.ui.login;

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

import com.example.runnconnect.R;
import com.google.android.material.textfield.TextInputEditText;


public class LoginActivity extends AppCompatActivity {

  private LoginViewModel viewModel;

  // UI
  private VideoView videoBackground;
  private TextInputEditText etEmail, etPassword;
  private Button btnLogin, btnVisitante;
  private TextView tvCrearCuenta, tvErrorLogin;
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
    tvErrorLogin= findViewById(R.id.tvErrorLogin);
  }

  private void setupVideoBackground() {
    try {
      Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background_video_login);
      videoBackground.setVideoURI(uri);

      // fijar tamaño de pantalla
      android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
      getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
      android.view.ViewGroup.LayoutParams params = videoBackground.getLayoutParams();
      params.width = metrics.widthPixels;
      params.height = metrics.heightPixels;
      videoBackground.setLayoutParams(params);

      videoBackground.setOnPreparedListener(mp -> {
        mp.setLooping(true);

        Runnable escalarVideo = () -> {
          int viewWidth = videoBackground.getWidth();
          int viewHeight = videoBackground.getHeight();

          if (viewWidth == 0 || viewHeight == 0) return;

          float videoWidth = mp.getVideoWidth();
          float videoHeight = mp.getVideoHeight();

          float videoRatio = videoWidth / videoHeight;
          float viewRatio = (float) viewWidth / viewHeight;

          float scale = 1f;

          if (videoRatio > viewRatio) {
            scale = videoRatio / viewRatio;
          } else {
            scale = viewRatio / videoRatio;
          }

          // multiplicamos por 1.02f (2% extra) para crear un borde y eliminar desborde de video

          scale = scale * 1.02f;

          videoBackground.setScaleX(scale);
          videoBackground.setScaleY(scale);
        };

        escalarVideo.run();
      });

      videoBackground.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setupObservers() {
    // observar cargando
    viewModel.getIsLoading().observe(this, isLoading -> {
      progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
      btnLogin.setEnabled(!isLoading); // desactivar boton mientras carga

      //si carga, ocultamos el error
      if(isLoading){
        tvErrorLogin.setVisibility(View.GONE);
      }
    });

    //observar Mensajes de Error
    viewModel.getErrorMessage().observe(this, msg -> {
      if (msg != null && !msg.isEmpty()) {
        tvErrorLogin.setText(msg);
        tvErrorLogin.setVisibility(View.VISIBLE);

        //animacion simple
        tvErrorLogin.setAlpha(0f);
        tvErrorLogin.animate().alpha(1f).setDuration(300).start();
      } else {
        tvErrorLogin.setVisibility(View.GONE);
      }
    });

    //observar navegar
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
      //Toast.makeText(this, "Próximamente: Registro", Toast.LENGTH_SHORT).show();
    });
  }


  @Override
  protected void onResume() {
    super.onResume();
    if (videoBackground != null) videoBackground.start();
  }
}
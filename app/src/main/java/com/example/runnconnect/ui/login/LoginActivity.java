package com.example.runnconnect.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.runnconnect.R;
import com.example.runnconnect.ui.registro.RegisterOrganizadorActivity;
import com.example.runnconnect.ui.registro.RegisterRunnerActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

  private LoginViewModel viewModel;
  private VideoView videoBackground;
  private TextInputEditText etEmail, etPassword;
  private Button btnLogin, btnVisitante;
  private TextView tvCrearCuenta, tvErrorLogin, tvOlvidePassword, tvSuccessLogin;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

    initViews();
    setupVideoBackground();
    setupObservers();
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
    tvErrorLogin = findViewById(R.id.tvErrorLogin);
    tvOlvidePassword = findViewById(R.id.tvOlvidePassword);
    tvSuccessLogin = findViewById(R.id.tvSuccessLogin);
  }

  private void setupObservers() {
    // Carga
    viewModel.getIsLoading().observe(this, isLoading -> {
      progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
      btnLogin.setEnabled(!isLoading);
    });

    // error (ojo)
    viewModel.getErrorMessage().observe(this, tvErrorLogin::setText);
    viewModel.getErrorVisibility().observe(this, tvErrorLogin::setVisibility);

    // exito (verde)
    viewModel.getExito().observe(this, tvSuccessLogin::setText);
    viewModel.getExitoVisibility().observe(this, tvSuccessLogin::setVisibility);

    // evento de cuenta desactivada
    viewModel.getPedirConfirmacionReactivacion().observe(this, debePreguntar -> {
      if (debePreguntar) {
        mostrarDialogoPreguntaReactivar();
        viewModel.confirmarReactivacionMostrada();
      }
    });

    // navegacion
    viewModel.getNavegacionEvento().observe(this, intent -> {
      startActivity(intent);
      if (!(intent.getComponent().getClassName().contains("EventosPublicosActivity"))) {
        finish();
      }
    });
  }

  private void mostrarDialogoPreguntaReactivar() {
    new AlertDialog.Builder(this)
      .setTitle("Cuenta Inhabilitada")
      .setMessage("Detectamos que tu cuenta está desactivada. ¿Quieres recibir un email para reactivarla ahora mismo?")
      .setPositiveButton("Enviar Email", (d, w) -> {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        viewModel.solicitarReactivacion(email, pass);
      })
      .setNegativeButton("Cancelar", null)
      .show();
  }

  private void setupListeners() {
    btnLogin.setOnClickListener(v -> {
      viewModel.login(etEmail.getText().toString().trim(), etPassword.getText().toString().trim());
    });

    btnVisitante.setOnClickListener(v -> viewModel.esVisitanteClicked());

    tvCrearCuenta.setOnClickListener(v -> mostrarDialogoSeleccionRol());

    tvOlvidePassword.setOnClickListener(v -> {
      EditText etEmailPopUp = new EditText(this);
      etEmailPopUp.setHint("Tu correo registrado");
      etEmailPopUp.setPadding(60, 40, 60, 40);

      new AlertDialog.Builder(this)
        .setTitle("Recuperar Contraseña")
        .setView(etEmailPopUp)
        .setPositiveButton("Enviar", (d, w) ->
          viewModel.recuperarPassword(etEmailPopUp.getText().toString().trim()))
        .setNegativeButton("Cancelar", null)
        .show();
    });
  }

  private void mostrarDialogoSeleccionRol() {
    String[] opciones = {"Soy Corredor (Runner)", "Soy Organizador"};
    new AlertDialog.Builder(this)
      .setTitle("Crear Cuenta")
      .setItems(opciones, (dialog, which) -> {
        Class<?> target = (which == 0) ? RegisterRunnerActivity.class : RegisterOrganizadorActivity.class;
        startActivity(new Intent(this, target));
      })
      .setNegativeButton("Cancelar", null).show();
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

  @Override
  protected void onResume() {
    super.onResume();
    if (videoBackground != null) videoBackground.start();
  }
}
package com.example.runnconnect.ui.login;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

  private LoginViewModel viewModel;
  private ActivityLoginBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityLoginBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

    setupObservers();
    setupListeners();
  }

  private void setupObservers() {
    // Carga
    viewModel.getIsLoading().observe(this, isLoading -> {
      binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
      binding.btnLogin.setEnabled(!isLoading);
    });

    // error (ojo)
    viewModel.getErrorMessage().observe(this, binding.tvErrorLogin::setText);
    viewModel.getErrorVisibility().observe(this, binding.tvErrorLogin::setVisibility);

    // exito (verde)
    viewModel.getExito().observe(this, binding.tvSuccessLogin::setText);
    viewModel.getExitoVisibility().observe(this, binding.tvSuccessLogin::setVisibility);

    // evento de cuenta desactivada
    viewModel.getPedirConfirmacionReactivacion().observe(this, debePreguntar -> {
       mostrarDialogoPreguntaReactivar();
     });


    //nuevo 20-06
    viewModel.getNavegacionEvento().observe(this, this::startActivity);

    viewModel.getFinalUser().observe(this, obs -> {
      if (Boolean.TRUE.equals(obs)) {
        finish();
      }
    });


  }

  private void mostrarDialogoPreguntaReactivar() {
    new AlertDialog.Builder(this)
      .setTitle("Cuenta Inhabilitada")
      .setMessage("Detectamos que tu cuenta está desactivada. ¿Quieres recibir un email para reactivarla ahora mismo?")
      .setPositiveButton("Enviar Email", (d, w) -> {
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();
        viewModel.solicitarReactivacion(email, pass);
      })
      .setNegativeButton("Cancelar", null)
      .show();
  }

  private void setupListeners() {
    binding.btnLogin.setOnClickListener(v -> {
      viewModel.login(binding.etEmail.getText().toString().trim(), binding.etPassword.getText().toString().trim());
    });

    binding.btnVisitante.setOnClickListener(v -> viewModel.esVisitanteClicked());

    binding.tvCrearCuenta.setOnClickListener(v -> mostrarDialogoSeleccionRol());

    binding.tvOlvidePassword.setOnClickListener(v -> {
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

  //nuevo 20-06
  private void mostrarDialogoSeleccionRol() {
    String[] opciones = {"Soy Corredor (Runner)", "Soy Organizador"};
    new AlertDialog.Builder(this)
      .setTitle("Crear Cuenta")
      .setItems(opciones, (dialog, which) -> {
        viewModel.onRolSeleccionadoParaRegistro(which);
      })
      .setNegativeButton("Cancelar", null)
      .show();
  }

  private void setupVideoBackground() {
    try {
      Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background_video_login);
      binding.videoBackground.setVideoURI(uri);

      // fijar tamaño de pantalla
      android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
      getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
      android.view.ViewGroup.LayoutParams params = binding.videoBackground.getLayoutParams();
      params.width = metrics.widthPixels;
      params.height = metrics.heightPixels;
      binding.videoBackground.setLayoutParams(params);

      binding.videoBackground.setOnPreparedListener(mp -> {
        mp.setLooping(true);

        Runnable escalarVideo = () -> {
          int viewWidth = binding.videoBackground.getWidth();
          int viewHeight = binding.videoBackground.getHeight();

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

          binding.videoBackground.setScaleX(scale);
          binding.videoBackground.setScaleY(scale);
        };

        escalarVideo.run();
      });

      binding.videoBackground.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (binding.videoBackground != null) {
      binding.videoBackground.setVisibility(View.VISIBLE);
    }
    setupVideoBackground();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (binding.videoBackground != null) {
      binding.videoBackground.stopPlayback();
      binding.videoBackground.setVisibility(View.GONE);
    }
  }
}
package com.example.runnconnect.ui.registro;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterRunnerActivity extends AppCompatActivity {
  private RegisterRunnerViewModel viewModel;
  private ActivityResultLauncher<PickVisualMediaRequest> mediaPicker;

  // UI
  private TextInputEditText etNombre, etApellido, etEmail, etPassword, etConfirm;
  private ImageView ivAvatar;
  private TextView tvError, tvVolver;
  private Button btnRegistrar;
  private ProgressBar progressBar;
  private VideoView videoBackground;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register); // Asegúrate que este es el XML del Runner

    viewModel = new ViewModelProvider(this).get(RegisterRunnerViewModel.class);

    initViews();
    setupVideo();
    setupPickMedia();
    setupObservers();
    setupListeners();
  }

  private void initViews() {
    etNombre = findViewById(R.id.etNombre);
    etApellido = findViewById(R.id.etApellido);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    etConfirm = findViewById(R.id.etConfirmPassword);

    ivAvatar = findViewById(R.id.ivAvatar);
    tvError = findViewById(R.id.tvErrorRegister);
    tvVolver = findViewById(R.id.tvVolverLogin);
    btnRegistrar = findViewById(R.id.btnRegistrar);
    progressBar = findViewById(R.id.progressBar);
    videoBackground = findViewById(R.id.videoBackground);
  }

  private void setupPickMedia() {
    mediaPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      if (uri != null) viewModel.onAvatarSelected(uri);
    });
  }

  private void setupObservers() {
    // Avatar seleccionado
    viewModel.getAvatarUri().observe(this, uri -> {
      Glide.with(this).load(uri).circleCrop().into(ivAvatar);
    });

    // Errores
    viewModel.getErrorMessage().observe(this, msg -> {
      if (msg != null && !msg.isEmpty()) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        tvError.setAlpha(0f);
        tvError.animate().alpha(1f).setDuration(300).start();
      } else {
        tvError.setVisibility(View.GONE);
      }
    });

    // Loading
    viewModel.getIsLoading().observe(this, loading -> {
      progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      btnRegistrar.setEnabled(!loading);
      if(loading) tvError.setVisibility(View.GONE);
    });

    // Navegación (Éxito)
    viewModel.getNavigateToMain().observe(this, intent -> {
      startActivity(intent);
      finish();
    });
  }

  private void setupListeners() {
    ivAvatar.setOnClickListener(v ->
      mediaPicker.launch(new PickVisualMediaRequest.Builder()
        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
        .build()));

    btnRegistrar.setOnClickListener(v -> {
      viewModel.registrar(
        etNombre.getText().toString().trim(),
        etApellido.getText().toString().trim(),
        etEmail.getText().toString().trim(),
        etPassword.getText().toString().trim(),
        etConfirm.getText().toString().trim()
      );
    });

    tvVolver.setOnClickListener(v -> finish());
  }

  private void setupVideo() {
    try {
      Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background_video_login);
      videoBackground.setVideoURI(uri);

      // Fijar tamaño real de pantalla (Solución bordes negros)
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

          if (videoRatio > viewRatio) scale = videoRatio / viewRatio;
          else scale = viewRatio / videoRatio;

          scale = scale * 1.02f; // Over-scale 2%
          videoBackground.setScaleX(scale);
          videoBackground.setScaleY(scale);
        };
        escalarVideo.run();
      });
      videoBackground.start();
    } catch (Exception e) { e.printStackTrace(); }
  }

}

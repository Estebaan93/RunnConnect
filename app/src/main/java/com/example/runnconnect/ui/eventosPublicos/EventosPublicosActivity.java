//ui/eventosPublicos/EventosPublicosActivity
package com.example.runnconnect.ui.eventosPublicos;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.R;


public class EventosPublicosActivity extends AppCompatActivity {

  private EventosPublicosViewModel viewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_eventos_publicos);

    // Inicializamos el ViewModel
    viewModel = new ViewModelProvider(this).get(EventosPublicosViewModel.class);

    // Configurar la Toolbar para volver atras
    androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Habilitar botón de "Atras" en la barra superior
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
  }

  // Manejar el clic en la flecha de volver atras
  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }




}
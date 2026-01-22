package com.example.runnconnect.ui.eventosPublicos;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; // <--- NO OLVIDES ESTE IMPORT
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.databinding.ActivityEventosPublicosBinding;
import com.example.runnconnect.ui.eventosPublicos.detalle.DetalleEventoPublicoActivity;

public class EventosPublicosActivity extends AppCompatActivity {

  private ActivityEventosPublicosBinding binding;
  private EventosPublicosViewModel viewModel;
  private EventosPublicosAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityEventosPublicosBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // --- CONFIGURACIÓN DE LA BARRA VIOLETA ---
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle("Próximos Eventos"); // Título
      getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Flecha activada
    }
    // -----------------------------------------

    viewModel = new ViewModelProvider(this).get(EventosPublicosViewModel.class);

    setupRecyclerView();
    setupObservers();

    // YA NO NECESITAMOS ESTO (Bórralo si lo tienes):
    // binding.btnVolverAtras.setOnClickListener(...)

    viewModel.cargarEventos();
  }

  // --- ESTE MÉTODO CONTROLA EL CLIC EN LA FLECHA DE LA BARRA ---
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish(); // Cierra esta pantalla y vuelve al Login
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
  // -------------------------------------------------------------

  private void setupRecyclerView() {
    adapter = new EventosPublicosAdapter(idEvento -> viewModel.seleccionarEvento(idEvento));
    binding.recyclerEventos.setLayoutManager(new LinearLayoutManager(this));
    binding.recyclerEventos.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(this, loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    viewModel.getListaEventos().observe(this, lista -> {
      if (lista != null) adapter.setLista(lista);
    });

    viewModel.getIsVacio().observe(this, vacio ->
      binding.tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE));

    viewModel.getMostrarToast().observe(this, msg -> {
      if (msg != null) {
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        viewModel.resetToast();
      }
    });

    viewModel.getNavegarADetalle().observe(this, idEvento -> {
      if (idEvento != null) {
        Intent intent = new Intent(EventosPublicosActivity.this, DetalleEventoPublicoActivity.class);
        intent.putExtra("idEvento", idEvento);
        startActivity(intent);
        viewModel.resetNavegacion();
      }
    });
  }
}
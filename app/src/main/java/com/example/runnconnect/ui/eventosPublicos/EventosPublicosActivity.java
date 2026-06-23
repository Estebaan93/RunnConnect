package com.example.runnconnect.ui.eventosPublicos;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; //
import android.view.View;

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

    // --- CONFIGURACION DE LA BARRA VIOLETA ---
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle("Próximos Eventos"); // Título
      getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Flecha activada
    }

    viewModel = new ViewModelProvider(this).get(EventosPublicosViewModel.class);

    setupRecyclerView();
    setupObservers();


    viewModel.cargarEventos();
  }

  // ---CONTROLA EL CLIC EN LA FLECHA DE LA BARRA ---
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish(); // Cierra esta pantalla y vuelve al Login
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setupRecyclerView() {
    adapter = new EventosPublicosAdapter(idEvento -> viewModel.seleccionarEvento(idEvento));
    binding.recyclerEventos.setLayoutManager(new LinearLayoutManager(this));
    binding.recyclerEventos.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(this, loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    /*viewModel.getListaEventos().observe(this, lista -> {
      if (lista != null) adapter.setLista(lista);
    });*/

    viewModel.getListaEventos().observe(this, lista -> adapter.setLista(lista));

    viewModel.getIsVacio().observe(this, vacio ->
      binding.tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE));

    viewModel.getMostrarMensaje().observe(this, msg -> {
      binding.tvError.setText(msg);
      binding.tvError.setVisibility(View.VISIBLE);

      binding.recyclerEventos.setVisibility(View.GONE);
      binding.tvVacio.setVisibility(View.GONE);
    });

    viewModel.getNavegarADetalle().observe(this, idEvento -> {
      Intent intent = new Intent(EventosPublicosActivity.this, DetalleEventoPublicoActivity.class);
      intent.putExtra("idEvento", idEvento);
      startActivity(intent);
      //viewModel.resetNavegacion();
    });
  }
}
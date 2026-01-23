package com.example.runnconnect.ui.eventosPublicos.detalle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.databinding.ActivityDetalleEventoPublicoBinding;
import com.example.runnconnect.ui.eventosPublicos.mapa.MapaPublicoActivity; // Importar nueva activity

public class DetalleEventoPublicoActivity extends AppCompatActivity {

  private ActivityDetalleEventoPublicoBinding binding;
  private DetalleEventoPublicoViewModel viewModel;
  private CategoriasDetalleAdapter adapter;
  private int idEvento = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityDetalleEventoPublicoBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    idEvento = getIntent().getIntExtra("idEvento", 0);

    viewModel = new ViewModelProvider(this).get(DetalleEventoPublicoViewModel.class);

    setupRecyclerView();
    setupObservers();
    setupListeners();

    if (idEvento != 0) {
      viewModel.cargarDetalle(idEvento);
    } else {
      finish();
    }
  }

  private void setupRecyclerView() {
    adapter = new CategoriasDetalleAdapter();
    binding.recyclerCategorias.setLayoutManager(new LinearLayoutManager(this));
    binding.recyclerCategorias.setNestedScrollingEnabled(false);
    binding.recyclerCategorias.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(this, loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    viewModel.getMensajeError().observe(this, msg -> {
      if (msg != null) {
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        viewModel.resetMensajeError();
      }
    });

    viewModel.getEvento().observe(this, evento -> {
      if (evento != null) {
        binding.tvTituloDetalle.setText(evento.getNombre());

        String fecha = (evento.getFechaHora() != null) ? evento.getFechaHora().replace("T", " ") : "-";
        binding.tvFechaHora.setText("Fecha: " + fecha);

        binding.tvLugar.setText("Lugar: " + evento.getLugar());
        binding.tvDescripcion.setText(evento.getDescripcion());

        String estado = (evento.getEstado() != null) ? evento.getEstado().toUpperCase() : "";
        binding.tvEstado.setText("Estado: " + estado);

        int disponibles = evento.getCuposDisponibles();
        int total = (evento.getCupoTotal() != null) ? evento.getCupoTotal() : 0;
        binding.tvCupos.setText("Cupos: " + disponibles + " / " + total);

        if (evento.getOrganizador() != null) {
          binding.tvNombreOrganizador.setText(evento.getOrganizador().getNombre());
        }

        if (evento.getCategorias() != null) {
          adapter.setLista(evento.getCategorias());
        }
      }
    });
  }

  private void setupListeners() {
    // 1. Botón "Iniciar Sesion para Inscribirme"
    binding.btnIrALogin.setOnClickListener(v -> {
      Intent intent = new Intent(this, com.example.runnconnect.ui.login.LoginActivity.class);
      // FLAGS IMPORTANTES: Borran la pila para que al dar "Atrás" no vuelva aquí
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
      finish();
    });

    // 2. Boton "Ver Recorrido" (NUEVO)
    binding.btnVerMapa.setOnClickListener(v -> {
      Intent i = new Intent(this, MapaPublicoActivity.class);
      i.putExtra("idEvento", idEvento);
      startActivity(i);
    });
  }
}
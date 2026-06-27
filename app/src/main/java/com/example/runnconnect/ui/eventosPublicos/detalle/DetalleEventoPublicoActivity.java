package com.example.runnconnect.ui.eventosPublicos.detalle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.databinding.ActivityDetalleEventoPublicoBinding;

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

    //
    viewModel.inicializar(idEvento);

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

    //error
    viewModel.getErrorText().observe(this, binding.tvError::setText);
    viewModel.getErrorVisibility().observe(this, binding.tvError::setVisibility);

    viewModel.getNombre().observe(this, binding.tvTituloDetalle::setText);
    viewModel.getFechaHora().observe(this, binding.tvFechaHora::setText);
    viewModel.getLugar().observe(this, binding.tvLugar::setText);
    viewModel.getDescripcion().observe(this, binding.tvDescripcion::setText);
    viewModel.getEstado().observe(this, binding.tvEstado::setText);
    viewModel.getCupos().observe(this, binding.tvCupos::setText);
    viewModel.getNombreOrganizador().observe(this, binding.tvNombreOrganizador::setText);

    // categorias
    viewModel.getCategorias().observe(this, adapter::setLista);

    // navegacion
    viewModel.getNavegacionEvento().observe(this, this::startActivity);
    viewModel.getFinalUser().observe(this, finish -> {
      if (Boolean.TRUE.equals(finish)) {
        finish();
      }
    });

  }

  private void setupListeners() {
    binding.btnIrALogin.setOnClickListener(v -> viewModel.onLoginClicked());
    binding.btnVerMapa.setOnClickListener(v -> viewModel.onVerMapaClicked(
      getIntent().getIntExtra("idEvento", 0)
    ));
  }

}
package com.example.runnconnect.ui.organizador.misEventos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.R;

public class ResultadosEventoFragment extends Fragment {

  private ResultadosEventoViewModel viewModel;
  private ResultadosAdapter adapter;
  private int idEvento = 0;

  // Referencias UI directas (si no usas ViewBinding en este fragment)
  private RecyclerView rvResultados;
  private ProgressBar progressBar;
  private TextView tvSinResultados;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // inflamos fragment_lista_resultados.xml
    View root = inflater.inflate(R.layout.fragment_lista_resultados, container, false);
    viewModel = new ViewModelProvider(this).get(ResultadosEventoViewModel.class);

    // Vincular Vistas
    rvResultados = root.findViewById(R.id.rvResultados);
    progressBar = root.findViewById(R.id.progressBar); // O progressBarResultados segun el xml
    tvSinResultados = root.findViewById(R.id.tvSinResultados);

    setupRecyclerView();
    setupObservers();

    if (idEvento != 0) {
      viewModel.cargarResultados(idEvento);
    }

    return root;
  }

  private void setupRecyclerView() {
    adapter = new ResultadosAdapter();
    rvResultados.setLayoutManager(new LinearLayoutManager(getContext()));
    rvResultados.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getListaResultados().observe(getViewLifecycleOwner(), lista -> {
      adapter.setLista(lista);

      if (lista == null || lista.isEmpty()) {
        if(tvSinResultados != null) tvSinResultados.setVisibility(View.VISIBLE);
      } else {
        if(tvSinResultados != null) tvSinResultados.setVisibility(View.GONE);
      }
    });

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    });

    viewModel.getMensajeError().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    });
  }
}
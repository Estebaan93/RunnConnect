package com.example.runnconnect.ui.organizador.misEventos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.databinding.FragmentListaResultadosBinding;

public class ResultadosEventoFragment extends Fragment {

  private FragmentListaResultadosBinding binding;
  private ResultadosEventoViewModel viewModel;
  private ResultadosAdapter adapter;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentListaResultadosBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(ResultadosEventoViewModel.class);

    setupRecyclerView();
    setupObservers();

    int idEvento = requireArguments().getInt("idEvento");
    viewModel.cargarResultados(idEvento);

    return binding.getRoot();
  }

  private void setupRecyclerView() {
    adapter = new ResultadosAdapter();
    binding.rvResultados.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvResultados.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getListaResultados().observe(getViewLifecycleOwner(), adapter::setLista);
    viewModel.getUiVisibilidadSinResultados().observe(getViewLifecycleOwner(), binding.tvSinResultados::setVisibility);
    viewModel.getUiVisibilidadRecycler().observe(getViewLifecycleOwner(), binding.rvResultados::setVisibility);

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
    );

    viewModel.getErrorTexto().observe(getViewLifecycleOwner(), binding.tvErrorResultados::setText);
    viewModel.getUiVisibilidadError().observe(getViewLifecycleOwner(), binding.tvErrorResultados::setVisibility);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
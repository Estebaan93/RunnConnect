package com.example.runnconnect.ui.runner.misResultados;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentMisResultadosBinding;

public class MisResultadosFragment extends Fragment {

  private FragmentMisResultadosBinding binding;
  private MisResultadosViewModel viewModel;
  private MisResultadosAdapter adapter;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentMisResultadosBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    viewModel = new ViewModelProvider(this).get(MisResultadosViewModel.class);

    setupRecyclerView();
    setupObservers();
  }

  private void setupRecyclerView() {
    adapter = new MisResultadosAdapter(idEvento -> {
      Bundle bundle = new Bundle();
      bundle.putInt("idEvento", idEvento);
      Navigation.findNavController(requireView())
          .navigate(R.id.action_misResultados_to_detalleEventoRunner, bundle);
    });

    binding.rvMisResultados.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvMisResultados.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getListaResultados().observe(getViewLifecycleOwner(), adapter::setLista);

    viewModel.getEstadisticas().observe(getViewLifecycleOwner(), stats -> {
      binding.tvCarrerasCount.setText(stats.carrerasCount);
      binding.tvMejorPuestoCount.setText(stats.mejorPuesto);
    });

    viewModel.getProgressBarVisibility().observe(getViewLifecycleOwner(), binding.progressBarResultados::setVisibility);
    viewModel.getVacioVisibility().observe(getViewLifecycleOwner(), binding.tvVacioResultados::setVisibility);
    viewModel.getVacioTexto().observe(getViewLifecycleOwner(), binding.tvVacioResultados::setText);
    viewModel.getErrorVisibility().observe(getViewLifecycleOwner(), binding.tvErrorResultados::setVisibility);
    viewModel.getErrorTexto().observe(getViewLifecycleOwner(), binding.tvErrorResultados::setText);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}

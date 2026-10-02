package com.example.runnconnect.ui.runner.buscarEventos;

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
import com.example.runnconnect.databinding.FragmentBuscarEventosBinding;

public class BuscarEventosFragment extends Fragment {

  private FragmentBuscarEventosBinding binding;
  private BuscarEventosViewModel viewModel;
  private BuscarEventosAdapter adapter;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentBuscarEventosBinding.inflate(inflater, container, false);

    viewModel = new ViewModelProvider(this).get(BuscarEventosViewModel.class);

    setupRecyclerView();
    setupObservers();

    return binding.getRoot();
  }

  private void setupRecyclerView() {
    adapter = new BuscarEventosAdapter(idEvento -> {
      Bundle bundle = new Bundle();
      bundle.putInt("idEvento", idEvento);
      Navigation.findNavController(requireView())
          .navigate(R.id.action_buscar_to_detalleEventoRunner, bundle);
    });

    binding.rvEventosBuscar.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvEventosBuscar.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getListaEventos().observe(getViewLifecycleOwner(), adapter::setLista);
    viewModel.getProgressVisibility().observe(getViewLifecycleOwner(), binding.progressBarBuscar::setVisibility);
    viewModel.getVacioVisibility().observe(getViewLifecycleOwner(), binding.tvVacioBuscar::setVisibility);
    viewModel.getErrorVisibility().observe(getViewLifecycleOwner(), binding.tvErrorBuscar::setVisibility);
    viewModel.getErrorText().observe(getViewLifecycleOwner(), binding.tvErrorBuscar::setText);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}

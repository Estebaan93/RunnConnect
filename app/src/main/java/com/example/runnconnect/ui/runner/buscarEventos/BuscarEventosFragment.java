package com.example.runnconnect.ui.runner.buscarEventos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
    setupSearchView();
    setupSpinners();
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

  private void setupSearchView() {
    binding.searchViewEventos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        viewModel.onBusquedaTextoCambiado(query);
        binding.searchViewEventos.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        viewModel.onBusquedaTextoCambiado(newText);
        return true;
      }
    });
  }

  private void setupSpinners() {
    binding.spFiltroGenero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        viewModel.setFiltroGenero(parent.getItemAtPosition(position).toString());
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    });

    binding.spFiltroDistancia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        viewModel.setFiltroDistancia(parent.getItemAtPosition(position).toString());
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    });
  }

  private void setupObservers() {
    viewModel.getListaOpcionesGenero().observe(getViewLifecycleOwner(), lista -> {
      ArrayAdapter<String> adapter = new ArrayAdapter<>(
          requireContext(),
          android.R.layout.simple_spinner_dropdown_item,
          lista
      );
      binding.spFiltroGenero.setAdapter(adapter);
    });

    viewModel.getListaOpcionesDistancia().observe(getViewLifecycleOwner(), lista -> {
      ArrayAdapter<String> adapter = new ArrayAdapter<>(
          requireContext(),
          android.R.layout.simple_spinner_dropdown_item,
          lista
      );
      binding.spFiltroDistancia.setAdapter(adapter);
    });

    viewModel.getListaEventos().observe(getViewLifecycleOwner(), adapter::setLista);
    viewModel.getProgressVisibility().observe(getViewLifecycleOwner(), binding.progressBarBuscar::setVisibility);
    viewModel.getVacioVisibility().observe(getViewLifecycleOwner(), binding.tvVacioBuscar::setVisibility);
    viewModel.getVacioText().observe(getViewLifecycleOwner(), binding.tvVacioBuscar::setText);
    viewModel.getErrorVisibility().observe(getViewLifecycleOwner(), binding.tvErrorBuscar::setVisibility);
    viewModel.getErrorText().observe(getViewLifecycleOwner(), binding.tvErrorBuscar::setText);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}

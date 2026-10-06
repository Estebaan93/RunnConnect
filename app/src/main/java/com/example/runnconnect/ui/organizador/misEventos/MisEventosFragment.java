package com.example.runnconnect.ui.organizador.misEventos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentMisEventosBinding;

public class MisEventosFragment extends Fragment {

  private FragmentMisEventosBinding binding;
  private MisEventosViewModel viewModel;
  private EventoAdapter adapter;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentMisEventosBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(MisEventosViewModel.class);

    setupRecyclerView();
    setupSearchView();
    setupSpinner();
    setupObservers();

    // Cargar datos al iniciar
    viewModel.cargarEventos(true);

    binding.fabNuevoEvento.setOnClickListener(v ->
      Navigation.findNavController(v).navigate(R.id.nav_nuevo_evento_fab)
    );

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getParentFragmentManager().setFragmentResultListener("requestKeyMapa", getViewLifecycleOwner(), (requestKey, bundle) -> {
      String msg = bundle.getString("nuevoEvento_misEventos");
      viewModel.mostrarMensaje(msg, false);
    });
  }

  private void setupRecyclerView() {
    adapter = new EventoAdapter();

    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

    binding.recyclerEventos.setLayoutManager(layoutManager);
    binding.recyclerEventos.setAdapter(adapter);

    // LISTENER DE SCROLL INFINITO
    binding.recyclerEventos.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy > 0) {
          int itemsVisibles = layoutManager.getChildCount();
          int totalItems = layoutManager.getItemCount();
          int primerItemVisible = layoutManager.findFirstVisibleItemPosition();

          viewModel.verificarScroll(itemsVisibles, totalItems, primerItemVisible);
        }
      }
    });

    // Implementacion de accion al hacer click
    adapter.setOnEventoClickListener(idEvento -> {
      android.util.Log.d("DEBUG_EVENTO", "Click en MisEventosFragment para idEvento: " + idEvento);
      Bundle bundle = new Bundle();
      bundle.putInt("idEvento", idEvento);

      Navigation.findNavController(requireView())
        .navigate(R.id.action_misEventos_to_detalleEvento, bundle);
    });
    binding.recyclerEventos.setAdapter(adapter);
  }

  private void setupSearchView() {
    binding.searchViewMisEventos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        viewModel.onBusquedaTextoCambiado(query);
        binding.searchViewMisEventos.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        viewModel.onBusquedaTextoCambiado(newText);
        return true;
      }
    });
  }

  private void setupSpinner() {
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
    viewModel.getUiMensajeTexto().observe(getViewLifecycleOwner(), binding.tvMensajeExito::setText);
    viewModel.getUiMensajeVisibilidad().observe(getViewLifecycleOwner(), binding.tvMensajeExito::setVisibility);
    viewModel.getUiMensajeColorTexto().observe(getViewLifecycleOwner(), binding.tvMensajeExito::setTextColor);
    viewModel.getUiMensajeColorFondo().observe(getViewLifecycleOwner(), binding.tvMensajeExito::setBackgroundColor);

    viewModel.getListaOpcionesDistancia().observe(getViewLifecycleOwner(), opciones -> {
      ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
          requireContext(),
          android.R.layout.simple_spinner_dropdown_item,
          opciones
      );
      binding.spFiltroDistancia.setAdapter(spinnerAdapter);
    });

    viewModel.getListaEventos().observe(getViewLifecycleOwner(), adapter::setEventos);

    viewModel.getUiTextoVacio().observe(getViewLifecycleOwner(), binding.tvVacio::setText);
    viewModel.getUiVisibilidadVacio().observe(getViewLifecycleOwner(), binding.tvVacio::setVisibility);
    viewModel.getUiVisibilidadRecycler().observe(getViewLifecycleOwner(), binding.recyclerEventos::setVisibility);

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
    );
  }
}
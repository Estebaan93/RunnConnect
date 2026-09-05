package com.example.runnconnect.ui.organizador.misEventos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
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
    setupObservers();

    // Cargar datos al iniciar
    viewModel.cargarEventos(true);

    //
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
      //if (msg != null) {
        viewModel.mostrarMensaje(msg, false); //
      //}
    });
  }

  private void setupRecyclerView() {
    adapter = new EventoAdapter();

    // creamos el layoutManager y guardamos en una var local
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

    //asignamos al recycler
    binding.recyclerEventos.setLayoutManager(layoutManager); //usamos la misma
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

          //
          viewModel.verificarScroll(itemsVisibles, totalItems, primerItemVisible);

        }
      }
    });

    //implementacion de accion al hacer click
    adapter.setOnEventoClickListener(idEvento -> {
      android.util.Log.d("DEBUG_EVENTO", "Click en MisEventosFragment para idEvento: " + idEvento);
      //navegamos al detalle pasando el ID
      Bundle bundle= new Bundle();
      bundle.putInt("idEvento", idEvento);

      //fragment_mis_eventos ->fragment_detalle_evento
      Navigation.findNavController(requireView())
        .navigate(R.id.action_misEventos_to_detalleEvento, bundle);
    });
    binding.recyclerEventos.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getUiMensajeTexto().observe(getViewLifecycleOwner(), binding.tvMensajeExito::setText);
    viewModel.getUiMensajeVisibilidad().observe(getViewLifecycleOwner(), binding.tvMensajeExito::setVisibility);
    viewModel.getUiMensajeColorTexto().observe(getViewLifecycleOwner(), binding.tvMensajeExito::setTextColor);
    viewModel.getUiMensajeColorFondo().observe(getViewLifecycleOwner(), binding.tvMensajeExito::setBackgroundColor);

    viewModel.getListaEventos().observe(getViewLifecycleOwner(), adapter::setEventos);

    viewModel.getUiVisibilidadVacio().observe(getViewLifecycleOwner(), binding.tvVacio::setVisibility);
    viewModel.getUiVisibilidadRecycler().observe(getViewLifecycleOwner(), binding.recyclerEventos::setVisibility);

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
    );
  }
}
package com.example.runnconnect.ui.organizador.misEventos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    // Botón "+" para crear (Navega al Paso 1)
    binding.fabNuevoEvento.setOnClickListener(v ->
      Navigation.findNavController(v).navigate(R.id.nav_crear_evento)
    );

    return binding.getRoot();
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

        if (dy > 0) { // Solo si scrollea hacia abajo
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
      //navegamos al detalle pasando el ID
      Bundle bundle= new Bundle();
      bundle.putInt("idEvento", idEvento);

      //fragment_mis_eventos ->fragment_detalle_evento
      Navigation.findNavController(binding.getRoot())
        .navigate(R.id.action_misEventos_to_detalleEvento, bundle);

    });
    binding.recyclerEventos.setAdapter(adapter);
  }

  private void setupObservers() {
    viewModel.getListaEventos().observe(getViewLifecycleOwner(), eventos -> {
      if (eventos == null) return;

      if (viewModel.getPaginaActual() == 1) {
        // Es la primera pagina, reemplazamos tod
        if (eventos.isEmpty()) {
          binding.tvVacio.setVisibility(View.VISIBLE);
          binding.recyclerEventos.setVisibility(View.GONE);
        } else {
          binding.tvVacio.setVisibility(View.GONE);
          binding.recyclerEventos.setVisibility(View.VISIBLE);
          adapter.setEventos(eventos);
        }
      } else {
        // Son páginas siguientes, agregamos
        adapter.agregarEventos(eventos);
      }
    });

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
    );

    viewModel.getErrorMsg().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    });
  }
}
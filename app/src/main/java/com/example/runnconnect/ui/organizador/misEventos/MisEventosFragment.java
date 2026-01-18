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

    // Botón "+" para crear (Navega al Paso 1)
    binding.fabNuevoEvento.setOnClickListener(v ->
      Navigation.findNavController(v).navigate(R.id.nav_crear_evento)
    );

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // CONFIGURACION DE RECEPCION DE MENSAJES (Inter-Fragment Communication)
    detectarMensajesEntrantes(view);
  }

  // Detecta si MapaEditor nos envio algo al volver
  private void detectarMensajesEntrantes(View view) {
    NavController navController = Navigation.findNavController(view);

    // 1. Caso: Volver atras
    if (navController.getCurrentBackStackEntry() != null) {
      navController.getCurrentBackStackEntry().getSavedStateHandle()
        .getLiveData("mensaje_exito", "") // Observamos cambios en esta clave
        .observe(getViewLifecycleOwner(), mensaje -> {
          if (!mensaje.isEmpty()) {
            viewModel.mostrarMensajeExito(mensaje);
            // Limpiamos el estado para que no se repita al rotar
            navController.getCurrentBackStackEntry().getSavedStateHandle().set("mensaje_exito", "");
          }
        });
    }

    // 2. Caso: Navegacion directa (navigate) -> Usamos Arguments
    if (getArguments() != null && getArguments().containsKey("mensaje_arg")) {
      String msg = getArguments().getString("mensaje_arg");
      viewModel.mostrarMensajeExito(msg);
      getArguments().remove("mensaje_arg"); // Limpiamos argumento
    }
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
    // Observer para el banner de exito
    viewModel.getMensajeExito().observe(getViewLifecycleOwner(), mensaje -> {
      if (mensaje != null && !mensaje.isEmpty()) {
        binding.tvMensajeExito.setText(mensaje);
        binding.tvMensajeExito.setVisibility(View.VISIBLE);
      } else {
        binding.tvMensajeExito.setVisibility(View.GONE);
      }
    });
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
        // Son paginas siguientes, agregamos
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
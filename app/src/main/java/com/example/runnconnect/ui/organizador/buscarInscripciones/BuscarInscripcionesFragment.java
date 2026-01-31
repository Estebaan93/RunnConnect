package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runnconnect.R;
import com.example.runnconnect.data.response.BusquedaInscripcionResponse;
import com.example.runnconnect.databinding.FragmentBuscarInscripcionesBinding;

import java.util.ArrayList;

public class BuscarInscripcionesFragment extends Fragment {

  private BuscarInscripcionesViewModel mViewModel;
  private FragmentBuscarInscripcionesBinding binding;
  private BuscarAdapter adapter;

  // Referencia global al diálogo para que el Observer pueda cerrarlo
  private Dialog dialogDetalleActual;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    binding = FragmentBuscarInscripcionesBinding.inflate(inflater, container, false);
    mViewModel = new ViewModelProvider(this).get(BuscarInscripcionesViewModel.class);

    setupRecyclerView();
    setupObservers();
    setupSearchView();

    return binding.getRoot();
  }

  @Override
  public void onResume() {
    super.onResume();
    // 1. Limpiamos datos en el ViewModel
    mViewModel.limpiarBusqueda();

    // 2. Limpiamos la caja visualmente
    if (binding != null) {
      binding.searchViewBusqueda.setQuery("", false);
      binding.searchViewBusqueda.clearFocus();
    }
  }

  private void setupRecyclerView() {
    // Al hacer click en un item, abrimos el diálogo detallado
    adapter = new BuscarAdapter(new ArrayList<>(), item -> {
      mostrarDetalleRunner(item);
    });

    binding.recyclerViewBusqueda.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerViewBusqueda.setAdapter(adapter);
  }

  private void setupObservers() {
// 1. Resultados de búsqueda
    mViewModel.getResultados().observe(getViewLifecycleOwner(), resultados -> {
      if (resultados != null) {
        adapter.setResultados(resultados);
      }
    });

    // 2. Mensajes de Error (Feedback en Dialog o Toast)
    mViewModel.getMensajeError().observe(getViewLifecycleOwner(), error -> {
      if (error != null && !error.isEmpty()) {
        if (dialogDetalleActual != null && dialogDetalleActual.isShowing()) {
          TextView tvMensaje = dialogDetalleActual.findViewById(R.id.tvMensajeBaja);
          if (tvMensaje != null) {
            tvMensaje.setText(error);
            tvMensaje.setTextColor(Color.RED);
            tvMensaje.setVisibility(View.VISIBLE);
          }
        } else {
          Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
      }
    });

    // 3. Mensajes de Éxito (Baja confirmada)
    mViewModel.getMensajeExito().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null && !msg.isEmpty()) {
        // Verificamos si el diálogo está abierto para mostrar el feedback ahí
        if (dialogDetalleActual != null && dialogDetalleActual.isShowing()) {

          TextView tvMensaje = dialogDetalleActual.findViewById(R.id.tvMensajeBaja);
          Button btnBaja = dialogDetalleActual.findViewById(R.id.btnDarDeBaja);

          if (tvMensaje != null) {
            // Feedback visual VERDE dentro del diálogo
            tvMensaje.setText("Dado de baja correctamente");
            tvMensaje.setTextColor(Color.parseColor("#2E7D32"));
            tvMensaje.setVisibility(View.VISIBLE);

            // Bloquear botón para evitar doble click
            if (btnBaja != null) btnBaja.setEnabled(false);

            // Cerrar automáticamente después de 1.5 segundos
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
              if (dialogDetalleActual != null && dialogDetalleActual.isShowing()) {
                dialogDetalleActual.dismiss();
                dialogDetalleActual = null;
              }
            }, 1500);
          }
        } else {
          // Fallback si el diálogo se cerró antes
          Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  private void setupSearchView() {
    binding.searchViewBusqueda.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        if (query != null && !query.trim().isEmpty()) {
          mViewModel.buscar(query);
        }
        binding.searchViewBusqueda.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        if (newText == null || newText.trim().isEmpty()) {
          adapter.setResultados(new ArrayList<>());
        }
        return false;
      }
    });
  }

  // --- METODO PARA MOSTRAR LA FICHA (DIALOGO) ---
  private void mostrarDetalleRunner(BusquedaInscripcionResponse item) {
    dialogDetalleActual = new Dialog(requireContext());
    dialogDetalleActual.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialogDetalleActual.setContentView(R.layout.dialog_detalle_runner);
    dialogDetalleActual.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // Bindings
    TextView tvNombre = dialogDetalleActual.findViewById(R.id.tvNombreCompleto);
    TextView tvDniSexo = dialogDetalleActual.findViewById(R.id.tvDniSexoEdad);
    TextView tvLocalidad = dialogDetalleActual.findViewById(R.id.tvLocalidad);
    TextView tvCatTalle = dialogDetalleActual.findViewById(R.id.tvCategoriaTalle);
    TextView tvEmail = dialogDetalleActual.findViewById(R.id.tvEmail);
    TextView tvTel = dialogDetalleActual.findViewById(R.id.tvTelefono);
    TextView tvEmergencia = dialogDetalleActual.findViewById(R.id.tvContactoEmergencia);
    TextView tvTelEmergencia = dialogDetalleActual.findViewById(R.id.tvTelEmergencia);
    Button btnCerrar = dialogDetalleActual.findViewById(R.id.btnCerrarDetalle);
    Button btnDarDeBaja = dialogDetalleActual.findViewById(R.id.btnDarDeBaja);

    // Ocultar mensaje interno
    TextView tvMensaje = dialogDetalleActual.findViewById(R.id.tvMensajeBaja);
    if(tvMensaje != null) tvMensaje.setVisibility(View.GONE);

    BusquedaInscripcionResponse.RunnerSimpleInfo r = item.getRunner();

    if (r != null) {
      tvNombre.setText(r.getNombreCompleto());

      // DNI y Sexo
      String dni = r.getDni() != null ? r.getDni() : "-";
      String genero = r.getGenero() != null ? r.getGenero() : "-";
      tvDniSexo.setText(String.format("DNI: %s | Sexo: %s", dni, genero));

      // Localidad
      tvLocalidad.setText(r.getLocalidad() != null ? r.getLocalidad() : "Localidad no especificada");

      // Contacto
      tvEmail.setText(r.getEmail());
      tvTel.setText(r.getTelefono() != null ? r.getTelefono() : "-");

      // Emergencia
      String contacto = r.getNombreContactoEmergencia() != null ? r.getNombreContactoEmergencia() : "No informado";
      tvEmergencia.setText("Contacto: " + contacto);

      String telEmerg = r.getTelefonoEmergencia() != null ? r.getTelefonoEmergencia() : "-";
      tvTelEmergencia.setText("Tel: " + telEmerg);
    } else {
      tvNombre.setText("Usuario Desconocido");
    }

    // Datos de Inscripción
    String talle = item.getTalleRemera() != null ? item.getTalleRemera() : "-";
    String categoria = item.getNombreCategoria() != null ? item.getNombreCategoria() : "Sin Cat.";
    String evento = item.getNombreEvento();

    // Agregamos el nombre del evento para dar contexto en búsqueda global
    tvCatTalle.setText("Evento: " + evento + "\nCat: " + categoria + " | Talle: " + talle);

    // Botón Baja
    btnDarDeBaja.setOnClickListener(v -> {
      new androidx.appcompat.app.AlertDialog.Builder(requireContext())
        .setTitle("Confirmar baja")
        .setMessage("¿Estás seguro de eliminar a " + (r != null ? r.getNombreCompleto() : "este usuario") + "?")
        .setPositiveButton("Sí, eliminar", (d, w) -> {
          String textoBusqueda = binding.searchViewBusqueda.getQuery().toString();
          mViewModel.darDeBaja(item.getIdInscripcion(), "Cancelado desde Búsqueda", textoBusqueda);
        })
        .setNegativeButton("Cancelar", null)
        .show();
    });

    btnCerrar.setOnClickListener(v -> dialogDetalleActual.dismiss());
    dialogDetalleActual.show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
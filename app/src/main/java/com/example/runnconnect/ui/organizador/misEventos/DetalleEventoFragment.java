package com.example.runnconnect.ui.organizador.misEventos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentDetalleEventoBinding;

public class DetalleEventoFragment extends Fragment {
  private FragmentDetalleEventoBinding binding;
  private DetalleEventoViewModel viewModel;
  private int idEvento = 0;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentDetalleEventoBinding.inflate(inflater, container, false);
    binding.btnCambiarEstado.setOnClickListener(v-> mostrarDialogoEstado());
    viewModel = new ViewModelProvider(this).get(DetalleEventoViewModel.class);

    // 1. Recuperar el ID enviado desde la lista
    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }

    // 2. Cargar datos
    if (idEvento != 0) {
      viewModel.cargarDetalle(idEvento);
    } else {
      Toast.makeText(getContext(), "Error: ID de evento inválido", Toast.LENGTH_SHORT).show();
    }

    setupObservers();

    // btn para ir al editor de mapa (reutilizamos tu fragmento existente)
    binding.btnVerMapa.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      // Navegar al fragmento que ya tienes creado en ui/organizador/mapa
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_mapaEditor, args);
    });

    //btn para habilitar info
    binding.btnEditarInfo.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento); // Pasamos el ID para activar modo edición

      // Navegamos al fragmento que antes solo era para crear
      try {
        Navigation.findNavController(v).navigate(R.id.action_detalle_to_editarEvento, args);
      } catch (Exception e) {
        // Fallback: intentar navegar directo por ID de destino si la acción no existe
        Navigation.findNavController(v).navigate(R.id.nav_editar_evento, args);
      }
    });


    return binding.getRoot();
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
    );

    viewModel.getEvento().observe(getViewLifecycleOwner(), evento -> {
      if (evento == null) return;

      binding.tvTituloDetalle.setText(evento.getNombre());
      binding.tvFechaDetalle.setText(evento.getFechaHora().replace("T", " "));
      binding.tvLugarDetalle.setText(evento.getLugar());
      binding.tvDescripcion.setText(evento.getDescripcion());
      binding.tvInscriptosCount.setText(String.valueOf(evento.getInscriptosActuales()));
      binding.tvCupoTotal.setText(String.valueOf(evento.getCupoTotal()));
      binding.tvEstadoDetalle.setText(evento.getEstado().toUpperCase());

      // --- LOGICA DE COLORES ---
      String estado = evento.getEstado().toUpperCase();

      switch (estado) {
        case "PUBLICADO":
          binding.tvEstadoDetalle.setTextColor(android.graphics.Color.parseColor("#2E7D32")); // Verde
          break;
        case "SUSPENDIDO":
          binding.tvEstadoDetalle.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Naranja
          break;
        case "FINALIZADO":
          binding.tvEstadoDetalle.setTextColor(android.graphics.Color.GRAY); // Gris
          break;
        case "CANCELADO":
          binding.tvEstadoDetalle.setTextColor(android.graphics.Color.RED); // Rojo
          break;
        default:
          binding.tvEstadoDetalle.setTextColor(android.graphics.Color.BLACK);
      }
    });

    viewModel.getErrorMsg().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
      viewModel.limpiarMensaje();
    });
  }

  private void mostrarDialogoEstado() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle("Administrar Estado");

    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado, null);
    builder.setView(view);

    // Referencias
    android.widget.RadioGroup rgEstado = view.findViewById(R.id.rgEstado);
    android.widget.EditText etMotivo = view.findViewById(R.id.etMotivo);

    // IMPORTANTE: Ponemos NULL aquí para que no tenga comportamiento por defecto
    builder.setPositiveButton("Guardar", null);
    builder.setNegativeButton("Cerrar", null);

    // Creamos el dialog pero no lo mostramos aun
    AlertDialog dialog = builder.create();
    dialog.show(); // Lo mostramos para poder acceder a sus botones

    // AHORA SI: Sobreescribimos el botón para tener control manual
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

      // 1. Validaciones (Tu lógica original)
      int selectedId = rgEstado.getCheckedRadioButtonId();
      String nuevoEstadoTemp = "";

      if (selectedId == R.id.rbPublicado) nuevoEstadoTemp = "publicado";
      else if (selectedId == R.id.rbFinalizado) nuevoEstadoTemp = "finalizado";
      else if (selectedId == R.id.rbCancelado) nuevoEstadoTemp = "cancelado";
      else if(selectedId == R.id.rbSuspendido) nuevoEstadoTemp = "suspendido";
      else {
        Toast.makeText(getContext(), "Debes seleccionar un estado", Toast.LENGTH_SHORT).show();
        return;
      }

      String motivo = etMotivo.getText().toString();

      if ((nuevoEstadoTemp.equals("cancelado") || nuevoEstadoTemp.equals("suspendido")) && motivo.trim().isEmpty()) {
        Toast.makeText(getContext(), "Para cancelar o suspender, el motivo es obligatorio.", Toast.LENGTH_LONG).show();
        return;
      }

      // 2. MAGIA ANTI-ANR:

      // A. Forzar cierre de teclado
      try {
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
          requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etMotivo.getWindowToken(), 0);
      } catch (Exception e) { e.printStackTrace(); }

      // B. Deshabilitar botón para que no le den click 2 veces
      v.setEnabled(false);

      // C. Variables finales para el Handler
      final String estadoDefinitivo = nuevoEstadoTemp;

      // D. Esperar a que el teclado se vaya y luego actuar
      new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {

        // Llamamos a la API (Esto ya no colgará la app)
        viewModel.cambiarEstado(idEvento, estadoDefinitivo, motivo);

        // CERRAMOS EL DIALOGO MANUALMENTE AQUI
        dialog.dismiss();

      }, 500); // 500ms de seguridad
    });
  }

}

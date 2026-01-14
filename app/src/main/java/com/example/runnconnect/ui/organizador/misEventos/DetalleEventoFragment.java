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

    // Botón para ir al editor de mapa (reutilizamos tu fragmento existente)
    binding.btnVerMapa.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      // Navegar al fragmento que ya tienes creado en ui/organizador/mapa
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_mapaEditor, args);
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
    });
  }

  private void mostrarDialogoEstado() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle("Administrar Estado");

    // 1. Inflar el XML personalizado
    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado, null);
    builder.setView(view);

    // 2. Obtener referencias
    android.widget.RadioGroup rgEstado = view.findViewById(R.id.rgEstado);
    android.widget.EditText etMotivo = view.findViewById(R.id.etMotivo);
    android.widget.RadioButton rbPublicado = view.findViewById(R.id.rbPublicado);
    android.widget.RadioButton rbFinalizado = view.findViewById(R.id.rbFinalizado);
    android.widget.RadioButton rbCancelado = view.findViewById(R.id.rbCancelado);

    // Pre-seleccionar según estado actual (Opcional, mejora UX)
    // String estadoActual = binding.tvEstadoDetalle.getText().toString();
    // if (estadoActual.equalsIgnoreCase("PUBLICADO")) rbPublicado.setChecked(true);

    builder.setPositiveButton("Guardar", (dialog, which) -> {
      // 3. Verificar que opcion se eligio
      int selectedId = rgEstado.getCheckedRadioButtonId();
      String nuevoEstado = "";

      if (selectedId == R.id.rbPublicado) nuevoEstado = "publicado";
      else if (selectedId == R.id.rbFinalizado) nuevoEstado = "finalizado";
      else if (selectedId == R.id.rbCancelado) nuevoEstado = "cancelado";
      else if(selectedId==R.id.rbSuspendido) nuevoEstado="suspendido";
      else {
        Toast.makeText(getContext(), "Debes seleccionar un estado", Toast.LENGTH_SHORT).show();
        return;
      }

      String motivo = etMotivo.getText().toString();

      // validacion: cancelado y suspendido requieren motivo
      if ((nuevoEstado.equals("cancelado") || nuevoEstado.equals("suspendido"))&& motivo.trim().isEmpty()) {
        Toast.makeText(getContext(), "Para cancelar o suspender, el motivo es obligatorio.", Toast.LENGTH_LONG).show();
        return; // Nota: En un dialog estándar, esto cerrará el dialog igual.
        // Para evitar que se cierre al fallar validación, se requiere un manejo más avanzado del botón Positive.
      }

      // Llamar al ViewModel
      viewModel.cambiarEstado(idEvento, nuevoEstado, motivo);
    });

    builder.setNegativeButton("Cerrar", null);
    builder.show();
  }

}

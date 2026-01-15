package com.example.runnconnect.ui.organizador.misEventos;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    viewModel = new ViewModelProvider(this).get(DetalleEventoViewModel.class);

    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }

    if (idEvento != 0) {
      viewModel.cargarDetalle(idEvento);
    } else {
      Toast.makeText(getContext(), "Error: ID de evento inválido", Toast.LENGTH_SHORT).show();
    }

    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  private void setupListeners() {
    binding.btnCambiarEstado.setOnClickListener(v -> mostrarDialogoEstado());

    binding.btnVerMapa.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      Navigation.findNavController(v).navigate(R.id.action_detalle_to_mapaEditor, args);
    });

    binding.btnEditarInfo.setOnClickListener(v -> {
      Bundle args = new Bundle();
      args.putInt("idEvento", idEvento);
      try {
        Navigation.findNavController(v).navigate(R.id.action_detalle_to_editarEvento, args);
      } catch (Exception e) {
        try {
          Navigation.findNavController(v).navigate(R.id.nav_crear_evento, args);
        } catch (Exception ex) {
          Toast.makeText(getContext(), "Error navegación", Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
    );

    viewModel.getEvento().observe(getViewLifecycleOwner(), evento -> {
      if (evento == null) return;

      binding.tvTituloDetalle.setText(evento.getNombre());
      binding.tvFechaDetalle.setText(evento.getFechaHora() != null ? evento.getFechaHora().replace("T", " ") : "");
      binding.tvLugarDetalle.setText(evento.getLugar());
      binding.tvDescripcion.setText(evento.getDescripcion());
      binding.tvInscriptosCount.setText(String.valueOf(evento.getInscriptosActuales()));
      binding.tvCupoTotal.setText(String.valueOf(evento.getCupoTotal()));
      binding.tvEstadoDetalle.setText(evento.getEstado().toUpperCase());

      String estado = evento.getEstado().toUpperCase();
      switch (estado) {
        case "PUBLICADO":
          binding.tvEstadoDetalle.setTextColor(Color.parseColor("#2E7D32"));
          break;
        case "SUSPENDIDO":
          binding.tvEstadoDetalle.setTextColor(Color.parseColor("#FF9800"));
          break;
        case "FINALIZADO":
          binding.tvEstadoDetalle.setTextColor(Color.GRAY);
          break;
        case "CANCELADO":
          binding.tvEstadoDetalle.setTextColor(Color.RED);
          break;
        default:
          binding.tvEstadoDetalle.setTextColor(Color.BLACK);
      }
    });

    viewModel.getErrorMsg().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null && !msg.isEmpty()) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        viewModel.limpiarMensaje();
      }
    });
  }

  private void mostrarDialogoEstado() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado, null);
    builder.setView(view);

    android.widget.RadioGroup rgEstado = view.findViewById(R.id.rgEstado);
    android.widget.EditText etMotivo = view.findViewById(R.id.etMotivo);

    builder.setPositiveButton("Guardar", null);
    builder.setNegativeButton("Cerrar", null);

    AlertDialog dialog = builder.create();
    dialog.show();

    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      // 1. Validaciones
      int selectedId = rgEstado.getCheckedRadioButtonId();
      String nuevoEstadoTemp = "";

      if (selectedId == R.id.rbPublicado) nuevoEstadoTemp = "publicado";
      else if (selectedId == R.id.rbSuspendido) nuevoEstadoTemp = "suspendido";
      else if (selectedId == R.id.rbFinalizado) nuevoEstadoTemp = "finalizado";
      else if (selectedId == R.id.rbCancelado) nuevoEstadoTemp = "cancelado";
      else {
        Toast.makeText(getContext(), "Selecciona un estado", Toast.LENGTH_SHORT).show();
        return;
      }

      String motivo = etMotivo.getText().toString().trim();
      if ((nuevoEstadoTemp.equals("cancelado") || nuevoEstadoTemp.equals("suspendido")) && motivo.isEmpty()) {
        etMotivo.setError("Motivo requerido");
        return;
      }

      // 2. Prevenir doble click
      v.setEnabled(false);

      // 3. Ocultar teclado (Usando el foco del diálogo para asegurar referencia)
      try {
        View focus = dialog.getCurrentFocus();
        if (focus != null) {
          InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
      } catch (Exception e) { e.printStackTrace(); }

      // 4. CERRAR EL DIALOGO PRIMERO (Importante para evitar freeze)
      dialog.dismiss();

      final String estadoFinal = nuevoEstadoTemp;
      final String motivoFinal = motivo;
      // 5. INICIAR CARGA CON RETARDO (Desacople de hilos UI)
      // Esto permite que la ventana del diálogo se destruya completamente antes de estresar la UI con el loading
      new Handler(Looper.getMainLooper()).postDelayed(() -> {
        viewModel.cambiarEstado(idEvento, estadoFinal, motivoFinal);
      }, 300); // 300ms de pausa técnica
    });
  }
}
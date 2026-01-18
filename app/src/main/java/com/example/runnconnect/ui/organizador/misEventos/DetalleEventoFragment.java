package com.example.runnconnect.ui.organizador.misEventos;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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

  private AlertDialog dialogEstado;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentDetalleEventoBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(DetalleEventoViewModel.class);

    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }

    if (idEvento != 0) {
      viewModel.cargarDetalle(idEvento);
    } else {
      // Manejo de error crítico sin Toast: Usamos el mensaje global si existe, o un textview de error
      // Como fallback rápido si no hay layout cargado aún, un Toast es inevitable, pero aquí ya hay binding.
      binding.tvMensajeGlobal.setText("Error: ID de evento inválido");
      binding.tvMensajeGlobal.setVisibility(View.VISIBLE);
    }

    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  private void setupObservers() {
    // 1. Visibilidad y Carga
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
    );

    // 2. MENSAJES EN PANTALLA (Reemplazo de Toast)
    viewModel.getMensajeGlobal().observe(getViewLifecycleOwner(), msg -> {
      // Asumimos que agregaste un TextView con id 'tvMensajeGlobal' en tu XML
      if (binding.tvMensajeGlobal != null) {
        binding.tvMensajeGlobal.setText(msg);
        binding.tvMensajeGlobal.setVisibility(msg != null && !msg.isEmpty() ? View.VISIBLE : View.GONE);
      }
      // Opcional: Podrías usar un Handler para ocultarlo después de 3 segundos si quieres efecto "Toast" pero en view
    });

    // 3. Data Binding (Textos y Colores)
    viewModel.getUiTitulo().observe(getViewLifecycleOwner(), binding.tvTituloDetalle::setText);
    viewModel.getUiFecha().observe(getViewLifecycleOwner(), binding.tvFechaDetalle::setText);
    viewModel.getUiLugar().observe(getViewLifecycleOwner(), binding.tvLugarDetalle::setText);
    viewModel.getUiDescripcion().observe(getViewLifecycleOwner(), binding.tvDescripcion::setText);
    viewModel.getUiInscriptos().observe(getViewLifecycleOwner(), binding.tvInscriptosCount::setText);
    viewModel.getUiCupo().observe(getViewLifecycleOwner(), binding.tvCupoTotal::setText);

    viewModel.getUiEstadoTexto().observe(getViewLifecycleOwner(), binding.tvEstadoDetalle::setText);
    viewModel.getUiEstadoColor().observe(getViewLifecycleOwner(), color -> binding.tvEstadoDetalle.setTextColor(color));

    viewModel.getUiDistanciaTipo().observe(getViewLifecycleOwner(), binding.tvDistanciaTipo::setText);
    viewModel.getUiGeneroPrecio().observe(getViewLifecycleOwner(), binding.tvGeneroPrecio::setText);

    viewModel.getUiVisibilidadDatosCategoria().observe(getViewLifecycleOwner(), visibility -> {
      binding.tvDistanciaTipo.setVisibility(visibility);
      binding.tvGeneroPrecio.setVisibility(visibility);
    });

    // 4. Control del Diálogo
    viewModel.getDialogDismiss().observe(getViewLifecycleOwner(), shouldDismiss -> {
      if (shouldDismiss && dialogEstado != null && dialogEstado.isShowing()) {
        dialogEstado.dismiss();
      }
    });
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
        try { Navigation.findNavController(v).navigate(R.id.nav_crear_evento, args); } catch (Exception ex) {}
      }
    });
  }

  private void mostrarDialogoEstado() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View view = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado, null);
    builder.setView(view);

    android.widget.RadioGroup rgEstado = view.findViewById(R.id.rgEstado);
    android.widget.EditText etMotivo = view.findViewById(R.id.etMotivo);

    // Preselección
    if (viewModel.getEventoRaw().getValue() != null) {
      String estadoActual = viewModel.getEventoRaw().getValue().getEstado();
      int idParaMarcar = viewModel.calcularPreseleccionRadio(estadoActual);
      if (idParaMarcar != -1) rgEstado.check(idParaMarcar);
    }

    builder.setPositiveButton("Guardar", null);
    builder.setNegativeButton("Cerrar", null);

    dialogEstado = builder.create();
    dialogEstado.show();

    dialogEstado.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      int selectedId = rgEstado.getCheckedRadioButtonId();
      String motivo = etMotivo.getText().toString();

      try {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etMotivo.getWindowToken(), 0);
      } catch (Exception e) {}

      new Handler(Looper.getMainLooper()).postDelayed(() -> {
        viewModel.procesarCambioEstado(idEvento, selectedId, motivo);
      }, 100);
    });

    // Error en el diálogo (Sin Toast)
    viewModel.getDialogError().observe(getViewLifecycleOwner(), error -> {
      if (error != null && dialogEstado != null && dialogEstado.isShowing()) {
        // Usamos el setError del EditText para mostrar el error ahí mismo
        etMotivo.setError(error);
        etMotivo.requestFocus();
      }
    });
  }
}
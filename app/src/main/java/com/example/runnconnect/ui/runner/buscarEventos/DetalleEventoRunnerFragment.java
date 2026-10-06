package com.example.runnconnect.ui.runner.buscarEventos;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.runnconnect.databinding.DialogInscripcionRunnerBinding;
import com.example.runnconnect.databinding.DialogVerImagenBinding;
import com.example.runnconnect.databinding.FragmentDetalleEventoRunnerBinding;
import com.example.runnconnect.ui.eventosPublicos.mapa.MapaPublicoActivity;
import com.example.runnconnect.ui.runner.buscarEventos.CategoriasRunnerAdapter.CategoriaCompatibilidadUI;

public class DetalleEventoRunnerFragment extends Fragment {

  private FragmentDetalleEventoRunnerBinding binding;
  private DetalleEventoRunnerViewModel viewModel;
  private CategoriasRunnerAdapter adapter;
  private int idEvento = 0;
  private AlertDialog currentDialog = null;

  private int idInscripcionParaComprobante = 0;

  private final ActivityResultLauncher<String> selectorComprobante = registerForActivityResult(
      new ActivityResultContracts.GetContent(),
      uri -> viewModel.procesarYSubirComprobante(idInscripcionParaComprobante, uri)
  );

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentDetalleEventoRunnerBinding.inflate(inflater, container, false);

    idEvento = requireArguments().getInt("idEvento", 0);

    viewModel = new ViewModelProvider(this).get(DetalleEventoRunnerViewModel.class);

    setupRecyclerView();
    setupListeners();
    setupObservers();

    viewModel.cargarDetalle(idEvento);

    return binding.getRoot();
  }

  private void setupRecyclerView() {
    adapter = new CategoriasRunnerAdapter(new CategoriasRunnerAdapter.OnCategoriaAccionListener() {
      @Override
      public void onInscribirmeClick(CategoriaCompatibilidadUI item) {
        mostrarDialogoInscripcion(item);
      }

      @Override
      public void onSubirComprobanteClick(CategoriaCompatibilidadUI item) {
        idInscripcionParaComprobante = item.getIdInscripcion();
        selectorComprobante.launch("image/*");
      }

      @Override
      public void onVerComprobanteClick(CategoriaCompatibilidadUI item) {
        mostrarDialogoVerComprobante(item.getUrlComprobante());
      }
    });
    binding.rvCategoriasRunner.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.rvCategoriasRunner.setAdapter(adapter);
  }

  private void setupListeners() {
    binding.btnVerRecorrido.setOnClickListener(v -> {
      Intent intent = new Intent(requireContext(), MapaPublicoActivity.class);
      intent.putExtra("idEvento", idEvento);
      startActivity(intent);
    });
  }

  private void setupObservers() {
    viewModel.getUiTitulo().observe(getViewLifecycleOwner(), binding.tvTituloDetalle::setText);
    viewModel.getUiEstado().observe(getViewLifecycleOwner(), binding.tvEstadoDetalle::setText);
    viewModel.getUiFecha().observe(getViewLifecycleOwner(), binding.tvFechaDetalle::setText);
    viewModel.getUiLugar().observe(getViewLifecycleOwner(), binding.tvLugarDetalle::setText);
    viewModel.getUiCupos().observe(getViewLifecycleOwner(), binding.tvCuposDetalle::setText);
    viewModel.getUiDescripcion().observe(getViewLifecycleOwner(), binding.tvDescripcionDetalle::setText);
    viewModel.getUiOrganizador().observe(getViewLifecycleOwner(), binding.tvOrganizadorDetalle::setText);
    viewModel.getUiDatosPago().observe(getViewLifecycleOwner(), binding.tvDatosPagoDetalle::setText);

    viewModel.getAvisoPerfilVisibility().observe(getViewLifecycleOwner(), binding.cardAvisoPerfil::setVisibility);
    viewModel.getListaCategoriasUI().observe(getViewLifecycleOwner(), adapter::setLista);

    viewModel.getProgressVisibility().observe(getViewLifecycleOwner(), binding.progressBarDetalle::setVisibility);
    viewModel.getMensajeTexto().observe(getViewLifecycleOwner(), binding.tvMensajeDetalle::setText);
    viewModel.getMensajeVisibility().observe(getViewLifecycleOwner(), binding.tvMensajeDetalle::setVisibility);
    viewModel.getMensajeColorFondo().observe(getViewLifecycleOwner(), binding.tvMensajeDetalle::setBackgroundColor);
    viewModel.getMensajeColorTexto().observe(getViewLifecycleOwner(), binding.tvMensajeDetalle::setTextColor);

    viewModel.getDialogCerrarEvento().observe(getViewLifecycleOwner(), this::gestionarCierreDialogo);
  }

  private void gestionarCierreDialogo(Boolean cerrar) {
    if (Boolean.TRUE.equals(cerrar) && currentDialog != null && currentDialog.isShowing()) {
      currentDialog.dismiss();
      viewModel.resetDialogCerrarEvento();
    }
  }

  private void mostrarDialogoInscripcion(CategoriaCompatibilidadUI categoria) {
    DialogInscripcionRunnerBinding dialogBinding = DialogInscripcionRunnerBinding.inflate(getLayoutInflater());

    dialogBinding.tvDialogTitulo.setText("Inscripción: " + categoria.getNombre());
    dialogBinding.tvDialogPrecio.setText("Costo: " + categoria.getCostoTexto());

    viewModel.getListaTallesRemera().observe(getViewLifecycleOwner(), lista -> {
      ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
          requireContext(),
          android.R.layout.simple_spinner_dropdown_item,
          lista
      );
      dialogBinding.spDialogTalleRemera.setAdapter(spinnerAdapter);
    });

    AlertDialog dialog = new AlertDialog.Builder(requireContext())
      .setView(dialogBinding.getRoot())
      .setCancelable(true)
      .create();

    currentDialog = dialog;

    viewModel.getDialogErrorTexto().observe(getViewLifecycleOwner(), dialogBinding.tvDialogError::setText);
    viewModel.getDialogErrorVisibility().observe(getViewLifecycleOwner(), dialogBinding.tvDialogError::setVisibility);

    dialogBinding.btnDialogCancelar.setOnClickListener(v -> dialog.dismiss());

    dialogBinding.btnDialogConfirmar.setOnClickListener(v -> {
      String talleSeleccionado = (String) dialogBinding.spDialogTalleRemera.getSelectedItem();
      boolean aceptoDeslinde = dialogBinding.cbDialogDeslinde.isChecked();

      viewModel.ejecutarInscripcion(
        categoria.getIdCategoria(),
        talleSeleccionado,
        aceptoDeslinde
      );
    });

    dialog.show();
  }

  private void mostrarDialogoVerComprobante(String url) {
    Dialog dialog = new Dialog(requireContext());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    DialogVerImagenBinding zoomBinding = DialogVerImagenBinding.inflate(getLayoutInflater());
    dialog.setContentView(zoomBinding.getRoot());
    if (dialog.getWindow() != null) {
      dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    zoomBinding.tvTituloComprobante.setText("Comprobante de Pago");
    Glide.with(this)
        .load(url)
        .into(zoomBinding.ivZoom);

    zoomBinding.btnCerrarImagen.setOnClickListener(v -> dialog.dismiss());
    dialog.show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (currentDialog != null && currentDialog.isShowing()) {
      currentDialog.dismiss();
    }
    binding = null;
  }
}

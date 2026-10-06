package com.example.runnconnect.ui.runner.misInscripciones;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.runnconnect.R;
import com.example.runnconnect.databinding.DialogVerImagenBinding;
import com.example.runnconnect.databinding.FragmentMisInscripcionesBinding;

public class MisInscripcionesFragment extends Fragment {

  private FragmentMisInscripcionesBinding binding;
  private MisInscripcionesViewModel viewModel;
  private MisInscripcionesAdapter adapter;

  private int idInscripcionSeleccionada = 0;
  private ActivityResultLauncher<String> imagePickerLauncher;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
          if (uri != null) {
            viewModel.procesarYSubirComprobante(idInscripcionSeleccionada, uri);
          }
        }
    );
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    binding = FragmentMisInscripcionesBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(MisInscripcionesViewModel.class);

    setupRecyclerView();
    setupSpinner();
    setupObservers();

    viewModel.cargarInscripciones();

    return binding.getRoot();
  }

  private void setupRecyclerView() {
    adapter = new MisInscripcionesAdapter(
        this::navegarADetalleEvento,
        this::iniciarSubidaComprobante,
        this::mostrarDialogoVerComprobante
    );

    binding.recyclerInscripciones.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.recyclerInscripciones.setAdapter(adapter);
  }

  private void setupSpinner() {
    binding.spFiltroEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        viewModel.setFiltroEstado(parent.getItemAtPosition(position).toString());
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    });
  }

  private void setupObservers() {
    viewModel.getListaOpcionesEstado().observe(getViewLifecycleOwner(), opciones -> {
      ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
          requireContext(),
          android.R.layout.simple_spinner_dropdown_item,
          opciones
      );
      binding.spFiltroEstado.setAdapter(spinnerAdapter);
    });

    viewModel.getListaInscripcionesUI().observe(getViewLifecycleOwner(), adapter::setLista);
    viewModel.getUiVisibilidadVacio().observe(getViewLifecycleOwner(), binding.tvVacio::setVisibility);
    viewModel.getUiVisibilidadRecycler().observe(getViewLifecycleOwner(), binding.recyclerInscripciones::setVisibility);
    viewModel.getUiTextoVacio().observe(getViewLifecycleOwner(), binding.tvVacio::setText);

    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
        binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
    );

    viewModel.getUiMensajeTexto().observe(getViewLifecycleOwner(), binding.tvMensaje::setText);
    viewModel.getUiMensajeVisibilidad().observe(getViewLifecycleOwner(), binding.tvMensaje::setVisibility);
    viewModel.getUiMensajeColorTexto().observe(getViewLifecycleOwner(), binding.tvMensaje::setTextColor);
    viewModel.getUiMensajeColorFondo().observe(getViewLifecycleOwner(), binding.tvMensaje::setBackgroundColor);
  }

  private void navegarADetalleEvento(int idEvento) {
    Bundle bundle = new Bundle();
    bundle.putInt("idEvento", idEvento);
    Navigation.findNavController(requireView())
        .navigate(R.id.action_misInscripciones_to_detalleEventoRunner, bundle);
  }

  private void iniciarSubidaComprobante(int idInscripcion) {
    this.idInscripcionSeleccionada = idInscripcion;
    imagePickerLauncher.launch("image/*");
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
    binding = null;
  }
}

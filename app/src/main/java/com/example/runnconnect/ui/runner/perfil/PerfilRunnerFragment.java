//ui/runner/PerfilRunnerFragment
package com.example.runnconnect.ui.runner.perfil;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.runnconnect.databinding.FragmentPerfilRunnerBinding;

public class PerfilRunnerFragment extends Fragment {

  private FragmentPerfilRunnerBinding binding;
  private PerfilRunnerViewModel mv;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflamos el binding (automáticamente detecta los IDs del XML)
    binding = FragmentPerfilRunnerBinding.inflate(inflater, container, false);
    mv = new ViewModelProvider(this).get(PerfilRunnerViewModel.class);

    setupObservers();

    // Listener del boton
    binding.btnAccion.setOnClickListener(v -> recolectarYEnviar());

    // Carga inicial de datos
    mv.cargarPerfil();
    return binding.getRoot();
  }

  private void setupObservers() {
    // 1. Datos del Perfil
    mv.getPerfilData().observe(getViewLifecycleOwner(), p -> {

      // --- Campos de texto ---
      binding.etEmail.setText(p.getEmail());
      binding.etNombre.setText(p.getNombre());
      binding.etApellido.setText(p.getApellido());
      binding.etTelefono.setText(p.getTelefono());

      binding.etDni.setText(p.getDni() != null ? String.valueOf(p.getDni()) : "");

      String fecha = p.getFechaNacimiento();
      if (fecha != null && fecha.contains("T")) fecha = fecha.split("T")[0];
      binding.etFechaNac.setText(fecha);

      binding.etGenero.setText(p.getGenero());
      binding.etLocalidad.setText(p.getLocalidad());
      binding.etAgrupacion.setText(p.getAgrupacion());
      binding.etNombreContacto.setText(p.getNombreContactoEmergencia());
      binding.etTelContacto.setText(p.getTelefonoEmergencia());
    });
    //2. obs para el imgAvatar
    mv.getAvatarUrl().observe(getViewLifecycleOwner(), url->{
      Glide.with(this)
              .load(url)
              .placeholder(android.R.drawable.ic_menu_camera)
              .error(android.R.drawable.ic_menu_camera)
              .circleCrop()
              .into(binding.ivAvatar);

    });

    // 3. Habilitar/Deshabilitar Edicion
    mv.getIsEditable().observe(getViewLifecycleOwner(), enabled -> {
      binding.etNombre.setEnabled(enabled);
      binding.etApellido.setEnabled(enabled);
      binding.etTelefono.setEnabled(enabled);
      binding.etDni.setEnabled(enabled);
      binding.etFechaNac.setEnabled(enabled);
      binding.etGenero.setEnabled(enabled);
      binding.etLocalidad.setEnabled(enabled);
      binding.etAgrupacion.setEnabled(enabled);
      binding.etNombreContacto.setEnabled(enabled);
      binding.etTelContacto.setEnabled(enabled);

      binding.etEmail.setEnabled(false); // Siempre bloqueado
    });

    // 3. Feedback visual (Boton y Toast)
    mv.getBtnText().observe(getViewLifecycleOwner(), binding.btnAccion::setText);
    mv.getIsLoading().observe(getViewLifecycleOwner(), loading ->
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
    mv.getMensajeToast().observe(getViewLifecycleOwner(), msg ->
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
  }

  private void recolectarYEnviar() {
    // Recolectamos el texto plano
    PerfilRunnerViewModel.RunnerInput input = new PerfilRunnerViewModel.RunnerInput(
            binding.etNombre.getText().toString(),
            binding.etApellido.getText().toString(),
            binding.etTelefono.getText().toString(),
            binding.etDni.getText().toString(),
            binding.etFechaNac.getText().toString(),
            binding.etGenero.getText().toString(),
            binding.etLocalidad.getText().toString(),
            binding.etAgrupacion.getText().toString(),
            binding.etNombreContacto.getText().toString(),
            binding.etTelContacto.getText().toString()
    );

    mv.onBotonClick(input);
  }

}
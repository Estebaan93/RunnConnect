//ui/cerrarSesion/LogoutFragment
package com.example.runnconnect.ui.cerrarSesion;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.runnconnect.R;
import com.example.runnconnect.ui.login.LoginActivity;

public class LogoutFragment extends Fragment {
  private LogoutViewModel vm;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // Inflamos un layout vacío o simple, ya que el protagonismo es del Dialog
    return inflater.inflate(R.layout.fragment_logout, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    vm = new ViewModelProvider(this).get(LogoutViewModel.class);

    vm.getNavegarAlLogin().observe(getViewLifecycleOwner(), ir->{
      navegarAlLogin();
    });
    vm.getOrdenCerrarDialogo().observe(getViewLifecycleOwner(), cerrar->{
      Navigation.findNavController(requireView()).navigateUp();
    });


    mostrarDialogoConfirmacion();
  }


  private void mostrarDialogoConfirmacion() {
    new AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro que deseas salir de RunnConnect?")
            // Evento al VM: Usuario quiere salir
            .setPositiveButton("Sí, salir", (dialog, which) -> vm.cerrarSesion())
            // Evento al VM: Usuario canceló
            .setNegativeButton("Cancelar", (dialog, which) -> {
              dialog.dismiss();
              vm.volverAtras();
            })
            .setCancelable(false)
            .show();
  }

  private void navegarAlLogin() {
    Intent intent = new Intent(requireActivity(), LoginActivity.class);
    // Flags para limpiar la pila de actividades (que no pueda volver atrás con botón físico)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    requireActivity().finish();
  }

}

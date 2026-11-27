package com.example.runnconnect.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

import com.example.runnconnect.databinding.DialogMotivoBajaBinding;

/*Helper reutilizable para mostrar el dialogo de baja con motivo obligatorio.*/
public class DialogBajaHelper {

  public interface OnConfirmarListener {
    void onConfirmar(String motivo);
  }

  private Dialog dialog;
  private DialogMotivoBajaBinding binding;

  public void mostrar(Context context, LayoutInflater inflater, String titulo, String mensaje,
                      OnConfirmarListener onConfirmar, Runnable onCancelar) {
    dismiss();

    dialog = new Dialog(context);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    binding = DialogMotivoBajaBinding.inflate(inflater);
    dialog.setContentView(binding.getRoot());

    if (dialog.getWindow() != null) {
      dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    binding.tvTituloBaja.setText(titulo);
    binding.tvMensajeBajaConfirmacion.setText(mensaje);

    binding.btnCancelarBaja.setOnClickListener(v -> {
      dismiss();
      if (onCancelar != null) onCancelar.run();
    });

    binding.btnConfirmarBaja.setOnClickListener(v -> {
      if (onConfirmar != null) {
        onConfirmar.onConfirmar(binding.etMotivoBaja.getText().toString());
      }
    });

    dialog.setOnCancelListener(d -> {
      dialog = null;
      binding = null;
      if (onCancelar != null) onCancelar.run();
    });

    dialog.show();
  }

  public static boolean esEstadoTerminal(String estadoPago) {
    if (estadoPago == null) return false;
    String st = estadoPago.toLowerCase().trim();
    return "cancelado".equals(st) || "cancelada".equals(st) || "baja".equals(st)
        || "rechazado".equals(st) || "rechazada".equals(st) || "reembolsado".equals(st);
  }

  public static boolean debeMostrarMotivo(String estadoPago, String observacion) {
    return esEstadoTerminal(estadoPago) && observacion != null && !observacion.trim().isEmpty();
  }

  public static String formatearMotivo(String estadoPago, String observacion) {
    return debeMostrarMotivo(estadoPago, observacion) ? "Motivo: " + observacion.trim() : "";
  }

  public void setErrorTexto(String texto) {
    if (binding != null && texto != null) {
      binding.tvErrorMotivoBaja.setText(texto);
    }
  }

  public void setErrorVisibilidad(Integer visibilidad) {
    if (binding != null && visibilidad != null) {
      binding.tvErrorMotivoBaja.setVisibility(visibilidad);
    }
  }

  public void cerrarSi(Boolean cerrar) {
    if (Boolean.TRUE.equals(cerrar)) {
      dismiss();
    }
  }

  public void dismiss() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
    dialog = null;
    binding = null;
  }
}

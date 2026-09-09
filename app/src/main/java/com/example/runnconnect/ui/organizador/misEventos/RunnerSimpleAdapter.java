package com.example.runnconnect.ui.organizador.misEventos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.data.response.InscriptoEventoResponse;
import com.example.runnconnect.databinding.ItemRunnerDialogBinding;

import java.util.ArrayList;
import java.util.List;

public class RunnerSimpleAdapter extends RecyclerView.Adapter<RunnerSimpleAdapter.ViewHolder> {

  public static class RunnerUI {
    public final String nombreCompleto;
    public final String dniTexto;
    public final String estadoTexto;
    public final int estadoColor;
    public final int visibilidadBotonBaja;
    public final InscriptoEventoResponse original;

    public RunnerUI(String nombreCompleto, String dniTexto, String estadoTexto, int estadoColor, int visibilidadBotonBaja, InscriptoEventoResponse original) {
      this.nombreCompleto = nombreCompleto;
      this.dniTexto = dniTexto;
      this.estadoTexto = estadoTexto;
      this.estadoColor = estadoColor;
      this.visibilidadBotonBaja = visibilidadBotonBaja;
      this.original = original;
    }
  }

  private List<RunnerUI> lista = new ArrayList<>();
  private final OnBajaClickListener listener;
  private boolean habilitarEliminacion = true;

  public interface OnBajaClickListener {
    void onBaja(InscriptoEventoResponse runner);
  }

  public RunnerSimpleAdapter(OnBajaClickListener listener) {
    this.listener = listener;
  }

  public void setHabilitarEliminacion(boolean habilitar) {
    this.habilitarEliminacion = habilitar;
    notifyDataSetChanged();
  }

  public void setLista(List<RunnerUI> nuevaLista) {
    this.lista = nuevaLista != null ? nuevaLista : new ArrayList<>();
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemRunnerDialogBinding binding = ItemRunnerDialogBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    RunnerUI item = lista.get(position);

    holder.binding.tvRunnerNombre.setText(item.nombreCompleto);
    holder.binding.tvRunnerDni.setText(item.dniTexto);
    holder.binding.tvRunnerEstado.setText(item.estadoTexto);
    holder.binding.tvRunnerEstado.setTextColor(item.estadoColor);

    int visibilidadBaja = habilitarEliminacion ? item.visibilidadBotonBaja : View.GONE;
    holder.binding.btnDarBaja.setVisibility(visibilidadBaja);
    holder.binding.btnDarBaja.setOnClickListener(visibilidadBaja == View.VISIBLE ? v -> listener.onBaja(item.original) : null);
  }

  @Override
  public int getItemCount() {
    return lista.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ItemRunnerDialogBinding binding;

    public ViewHolder(@NonNull ItemRunnerDialogBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
package com.example.runnconnect.ui.runner.misInscripciones;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.databinding.ItemMisInscripcionBinding;

import java.util.ArrayList;
import java.util.List;

public class MisInscripcionesAdapter extends RecyclerView.Adapter<MisInscripcionesAdapter.ViewHolder> {

  public interface OnInscripcionClickListener {
    void onInscripcionClick(int idEvento);
  }

  public interface OnSubirComprobanteClickListener {
    void onSubirComprobanteClick(int idInscripcion);
  }

  public interface OnVerComprobanteClickListener {
    void onVerComprobanteClick(String comprobanteUrl);
  }

  private final List<InscripcionUI> lista = new ArrayList<>();
  private final OnInscripcionClickListener inscripcionClickListener;
  private final OnSubirComprobanteClickListener subirComprobanteClickListener;
  private final OnVerComprobanteClickListener verComprobanteClickListener;

  public MisInscripcionesAdapter(OnInscripcionClickListener inscripcionClickListener,
                                 OnSubirComprobanteClickListener subirComprobanteClickListener,
                                 OnVerComprobanteClickListener verComprobanteClickListener) {
    this.inscripcionClickListener = inscripcionClickListener;
    this.subirComprobanteClickListener = subirComprobanteClickListener;
    this.verComprobanteClickListener = verComprobanteClickListener;
  }

  public void setLista(List<InscripcionUI> nuevaLista) {
    this.lista.clear();
    if (nuevaLista != null) {
      this.lista.addAll(nuevaLista);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemMisInscripcionBinding binding = ItemMisInscripcionBinding.inflate(
        LayoutInflater.from(parent.getContext()),
        parent,
        false
    );
    return new ViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    InscripcionUI item = lista.get(position);

    holder.binding.tvNombreEvento.setText(item.getNombreEvento());
    holder.binding.tvFechaLugar.setText(item.getFechaLugar());
    holder.binding.tvCategoriaCosto.setText(item.getCategoriaCosto());
    holder.binding.tvTalleRemera.setText(item.getTalleRemera());

    holder.binding.tvEstadoInscripcion.setText(item.getEstadoTexto());
    holder.binding.tvEstadoInscripcion.setTextColor(item.getEstadoColorTexto());
    holder.binding.tvEstadoInscripcion.setBackgroundColor(item.getEstadoColorFondo());

    holder.binding.tvObservacionRechazo.setText(item.getObservacionTexto());
    holder.binding.tvObservacionRechazo.setVisibility(item.getObservacionVisibilidad());

    holder.binding.btnSubirComprobante.setVisibility(item.getBtnSubirComprobanteVisibilidad());
    holder.binding.btnVerComprobante.setVisibility(item.getBtnVerComprobanteVisibilidad());

    holder.binding.getRoot().setOnClickListener(v -> {
      if (inscripcionClickListener != null) {
        inscripcionClickListener.onInscripcionClick(item.getIdEvento());
      }
    });

    holder.binding.btnSubirComprobante.setOnClickListener(v -> {
      if (subirComprobanteClickListener != null) {
        subirComprobanteClickListener.onSubirComprobanteClick(item.getIdInscripcion());
      }
    });

    holder.binding.btnVerComprobante.setOnClickListener(v -> {
      if (verComprobanteClickListener != null) {
        verComprobanteClickListener.onVerComprobanteClick(item.getComprobanteUrl());
      }
    });
  }

  @Override
  public int getItemCount() {
    return lista.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final ItemMisInscripcionBinding binding;

    public ViewHolder(@NonNull ItemMisInscripcionBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}

package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.databinding.ItemBusquedaGlobalBinding;
import com.example.runnconnect.ui.organizador.buscarInscripciones.BuscarInscripcionesViewModel.BusquedaItemUiModel;

import java.util.ArrayList;
import java.util.List;

public class BuscarAdapter extends RecyclerView.Adapter<BuscarAdapter.BusquedaViewHolder> {

  private List<BusquedaItemUiModel> resultados;
  private final OnItemActionListener listener;

  public interface OnItemActionListener {
    void onItemClick(BusquedaItemUiModel item);
  }

  public BuscarAdapter(List<BusquedaItemUiModel> resultados, OnItemActionListener listener) {
    this.resultados = resultados != null ? resultados : new ArrayList<>();
    this.listener = listener;
  }

  @NonNull
  @Override
  public BusquedaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemBusquedaGlobalBinding binding = ItemBusquedaGlobalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new BusquedaViewHolder(binding, listener);
  }

  @Override
  public void onBindViewHolder(@NonNull BusquedaViewHolder holder, int position) {
    holder.bind(resultados.get(position));
  }

  @Override
  public int getItemCount() {
    return resultados.size();
  }

  public void setResultados(List<BusquedaItemUiModel> nuevosResultados) {
    this.resultados = nuevosResultados != null ? nuevosResultados : new ArrayList<>();
    notifyDataSetChanged();
  }

  static class BusquedaViewHolder extends RecyclerView.ViewHolder {
    private final ItemBusquedaGlobalBinding binding;
    private final OnItemActionListener listener;

    public BusquedaViewHolder(ItemBusquedaGlobalBinding binding, OnItemActionListener listener) {
      super(binding.getRoot());
      this.binding = binding;
      this.listener = listener;
    }

    public void bind(final BusquedaItemUiModel item) {
      binding.tvRunnerNombre.setText(item.nombreCompleto);
      binding.tvRunnerDni.setText(item.dni);
      binding.tvFechaInscripcion.setText(item.fechaInscripcionTexto);
      binding.tvEventoCategoria.setText(item.eventoCategoria);
      binding.tvEstadoPago.setText(item.estadoPagoTexto);
      binding.tvEstadoPago.setTextColor(item.estadoPagoColor);

      binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
    }
  }
}
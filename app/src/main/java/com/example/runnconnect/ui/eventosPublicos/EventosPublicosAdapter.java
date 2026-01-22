package com.example.runnconnect.ui.eventosPublicos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.R;
import com.example.runnconnect.data.response.CategoriaResponse;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.databinding.ItemEventoPublicoBinding;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class EventosPublicosAdapter extends RecyclerView.Adapter<EventosPublicosAdapter.ViewHolder> {

  private List<EventoResumenResponse> lista = new ArrayList<>();
  private final OnEventoClickListener listener;

  public interface OnEventoClickListener {
    void onVerDetalle(int idEvento);
  }

  public EventosPublicosAdapter(OnEventoClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<EventoResumenResponse> nuevaLista) {
    this.lista = nuevaLista;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(ItemEventoPublicoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bind(lista.get(position));
  }

  @Override
  public int getItemCount() { return lista.size(); }

  class ViewHolder extends RecyclerView.ViewHolder {
    private final ItemEventoPublicoBinding binding;

    public ViewHolder(ItemEventoPublicoBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(EventoResumenResponse item) {
      Context context = binding.getRoot().getContext();

      binding.tvNombreEvento.setText(item.getNombre());

      String fechaLimpia = (item.getFechaHora() != null) ? item.getFechaHora().replace("T", " ") : "-";
      binding.tvFecha.setText(fechaLimpia);

      binding.tvLugar.setText(item.getLugar());
      binding.tvOrganizador.setText("Org: " + item.getNombreOrganizador());

      // 1. Limpiar chips anteriores
      binding.chipGroupCategorias.removeAllViews();

      // 2. Inflar chips desde XML (Diseño item_chip_categoria.xml)
      if (item.getCategorias() != null && !item.getCategorias().isEmpty()) {

        for (CategoriaResponse cat : item.getCategorias()) {

          // --- AQUÍ ESTÁ EL CAMBIO CLAVE ---
          // En lugar de "new Chip()", inflamos el archivo XML que creaste
          Chip chip = (Chip) LayoutInflater.from(context)
            .inflate(R.layout.item_chip_categoria, binding.chipGroupCategorias, false);

          // Solo cambiamos el texto, el resto del estilo viene del XML
          chip.setText(cat.getNombre());

          // Agregamos al grupo
          binding.chipGroupCategorias.addView(chip);
        }
      }

      binding.btnVerDetalle.setOnClickListener(v -> listener.onVerDetalle(item.getIdEvento()));
    }
  }
}
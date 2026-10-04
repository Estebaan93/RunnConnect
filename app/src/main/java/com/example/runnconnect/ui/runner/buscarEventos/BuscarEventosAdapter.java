package com.example.runnconnect.ui.runner.buscarEventos;

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

public class BuscarEventosAdapter extends RecyclerView.Adapter<BuscarEventosAdapter.ViewHolder> {

  private List<EventoResumenResponse> lista = new ArrayList<>();
  private final OnEventoClickListener listener;

  public interface OnEventoClickListener {
    void onVerDetalle(int idEvento);
  }

  public BuscarEventosAdapter(OnEventoClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<EventoResumenResponse> nuevaLista) {
    this.lista = nuevaLista != null ? nuevaLista : new ArrayList<>();
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
  public int getItemCount() {
    return lista.size();
  }

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

      binding.tvLugar.setText(item.getLugar() != null ? item.getLugar() : "-");
      binding.tvOrganizador.setText("Org: " + (item.getNombreOrganizador() != null ? item.getNombreOrganizador() : "-"));

      binding.chipGroupCategorias.removeAllViews();
      for (CategoriaResponse cat : item.getCategorias()) {
        Chip chip = (Chip) LayoutInflater.from(context)
            .inflate(R.layout.item_chip_categoria, binding.chipGroupCategorias, false);
        chip.setText(cat.getNombre());
        binding.chipGroupCategorias.addView(chip);
      }

      binding.btnVerDetalle.setOnClickListener(v -> listener.onVerDetalle(item.getIdEvento()));
      binding.getRoot().setOnClickListener(v -> listener.onVerDetalle(item.getIdEvento()));
    }
  }
}

package com.example.runnconnect.ui.runner.buscarEventos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.ItemEventoPublicoBinding;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class BuscarEventosAdapter extends RecyclerView.Adapter<BuscarEventosAdapter.ViewHolder> {

  private List<EventoCardUI> lista = new ArrayList<>();
  private final OnEventoClickListener listener;

  public interface OnEventoClickListener {
    void onVerDetalle(int idEvento);
  }

  public BuscarEventosAdapter(OnEventoClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<EventoCardUI> nuevaLista) {
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
  public int getItemCount() {
    return lista.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    private final ItemEventoPublicoBinding binding;

    public ViewHolder(ItemEventoPublicoBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(EventoCardUI item) {
      Context context = binding.getRoot().getContext();

      binding.tvNombreEvento.setText(item.getNombre());
      binding.tvFecha.setText(item.getFecha());
      binding.tvLugar.setText(item.getLugar());
      binding.tvOrganizador.setText(item.getOrganizador());

      binding.chipGroupCategorias.removeAllViews();
      for (String catNombre : item.getNombresCategorias()) {
        Chip chip = (Chip) LayoutInflater.from(context)
            .inflate(R.layout.item_chip_categoria, binding.chipGroupCategorias, false);
        chip.setText(catNombre);
        binding.chipGroupCategorias.addView(chip);
      }

      binding.btnVerDetalle.setOnClickListener(v -> listener.onVerDetalle(item.getIdEvento()));
      binding.getRoot().setOnClickListener(v -> listener.onVerDetalle(item.getIdEvento()));
    }
  }
}

package com.example.runnconnect.ui.organizador.misEventos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.databinding.ItemResultadoRankingBinding;

import java.util.ArrayList;
import java.util.List;

public class ResultadosAdapter extends RecyclerView.Adapter<ResultadosAdapter.ViewHolder> {

  private List<ResultadosEventoViewModel.ResultadoUI> lista = new ArrayList<>();

  public void setLista(List<ResultadosEventoViewModel.ResultadoUI> nuevaLista) {
    this.lista = nuevaLista != null ? nuevaLista : new ArrayList<>();
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemResultadoRankingBinding binding = ItemResultadoRankingBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ResultadosEventoViewModel.ResultadoUI item = lista.get(position);

    holder.binding.tvPosicion.setText(item.posicion);
    holder.binding.tvNombreRunner.setText(item.nombreRunner);
    holder.binding.tvCategoriaRunner.setText(item.categoriaYGenero);
    holder.binding.tvTiempoOficial.setText(item.tiempoOficial);
  }

  @Override
  public int getItemCount() {
    return lista.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final ItemResultadoRankingBinding binding;

    public ViewHolder(@NonNull ItemResultadoRankingBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
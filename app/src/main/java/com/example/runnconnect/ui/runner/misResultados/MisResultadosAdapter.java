package com.example.runnconnect.ui.runner.misResultados;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.databinding.ItemResultadoBinding;
import com.example.runnconnect.ui.runner.misResultados.MisResultadosViewModel.ResultadoItemUI;

import java.util.ArrayList;
import java.util.List;

public class MisResultadosAdapter extends RecyclerView.Adapter<MisResultadosAdapter.ViewHolder> {

  private List<ResultadoItemUI> lista = new ArrayList<>();
  private final OnResultadoClickListener listener;

  public interface OnResultadoClickListener {
    void onResultadoClick(int idEvento);
  }

  public MisResultadosAdapter(OnResultadoClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<ResultadoItemUI> nuevaLista) {
    this.lista = nuevaLista != null ? nuevaLista : new ArrayList<>();
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(ItemResultadoBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false
    ));
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
    private final ItemResultadoBinding binding;

    public ViewHolder(ItemResultadoBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(ResultadoItemUI item) {
      binding.tvNombreEvento.setText(item.nombreEvento);
      binding.tvFechaLugar.setText(item.fechaLugar);
      binding.tvCategoria.setText(item.categoria);
      binding.tvTiempo.setText(item.tiempoOficial);
      binding.tvPosicion.setText(item.posicionTexto);

      binding.getRoot().setOnClickListener(v -> listener.onResultadoClick(item.idEvento));
    }
  }
}

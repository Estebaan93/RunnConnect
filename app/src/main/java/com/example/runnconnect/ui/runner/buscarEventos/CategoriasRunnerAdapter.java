package com.example.runnconnect.ui.runner.buscarEventos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.databinding.ItemCategoriaRunnerBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoriasRunnerAdapter extends RecyclerView.Adapter<CategoriasRunnerAdapter.ViewHolder> {

  private List<CategoriaCompatibilidadUI> lista = new ArrayList<>();
  private final OnInscripcionClickListener listener;

  public interface OnInscripcionClickListener {
    void onInscribirmeClick(CategoriaCompatibilidadUI item);
  }

  public CategoriasRunnerAdapter(OnInscripcionClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<CategoriaCompatibilidadUI> nuevaLista) {
    this.lista = nuevaLista != null ? nuevaLista : new ArrayList<>();
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(ItemCategoriaRunnerBinding.inflate(
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
    private final ItemCategoriaRunnerBinding binding;

    public ViewHolder(ItemCategoriaRunnerBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(CategoriaCompatibilidadUI item) {
      binding.tvCatNombre.setText(item.getNombre());
      binding.tvCatPrecio.setText(item.getCostoTexto());
      binding.tvCatRequisitos.setText(item.getRequisitosTexto());
      binding.tvCatCupos.setText(item.getCuposTexto());

      binding.tvCatCompatibilidad.setVisibility(item.isEsCompatible() ? View.GONE : View.VISIBLE);
      binding.tvCatCompatibilidad.setText(item.getEstadoCompatibilidadTexto());
      binding.tvCatCompatibilidad.setTextColor(item.getColorEstadoCompatibilidad());

      binding.btnInscribirme.setEnabled(item.isEsCompatible());
      binding.btnInscribirme.setOnClickListener(v -> listener.onInscribirmeClick(item));
    }
  }
}

package com.example.runnconnect.ui.eventosPublicos.detalle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnconnect.data.response.CategoriaResponse;
import com.example.runnconnect.databinding.ItemCategoriaSimpleBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoriasDetalleAdapter extends RecyclerView.Adapter<CategoriasDetalleAdapter.ViewHolder> {

  private List<CategoriaResponse> lista = new ArrayList<>();

  public void setLista(List<CategoriaResponse> nuevaLista) {
    this.lista = nuevaLista;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(ItemCategoriaSimpleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    CategoriaResponse item = lista.get(position);

    // Nombre y Edad
    String info = item.getNombre() + " (" + item.getEdadMinima() + "-" + item.getEdadMaxima() + " años)";
    holder.binding.tvCatNombre.setText(info);

    // Precio (Usamos getPrecio() del modelo CategoriaResponse)
    String precio = "$ " + item.getPrecio().toString();
    holder.binding.tvCatPrecio.setText(precio);

    //ocultamos el btn eliminar
    holder.binding.btnEliminar.setVisibility(View.GONE);
  }

  @Override
  public int getItemCount() { return lista.size(); }

  static class ViewHolder extends RecyclerView.ViewHolder {
    ItemCategoriaSimpleBinding binding;

    public ViewHolder(ItemCategoriaSimpleBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
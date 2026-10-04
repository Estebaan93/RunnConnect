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

  public static class CategoriaCompatibilidadUI {
    private final int idCategoria;
    private final String nombre;
    private final String costoTexto;
    private final String cuposTexto;
    private final String requisitosTexto;
    private final String estadoCompatibilidadTexto;
    private final boolean esCompatible;
    private final int colorEstadoCompatibilidad;

    public CategoriaCompatibilidadUI(int idCategoria, String nombre, String costoTexto, String cuposTexto, String requisitosTexto, String estadoCompatibilidadTexto, boolean esCompatible, int colorEstadoCompatibilidad) {
      this.idCategoria = idCategoria;
      this.nombre = nombre;
      this.costoTexto = costoTexto;
      this.cuposTexto = cuposTexto;
      this.requisitosTexto = requisitosTexto;
      this.estadoCompatibilidadTexto = estadoCompatibilidadTexto;
      this.esCompatible = esCompatible;
      this.colorEstadoCompatibilidad = colorEstadoCompatibilidad;
    }

    public int getIdCategoria() {
      return idCategoria;
    }

    public String getNombre() {
      return nombre;
    }

    public String getCostoTexto() {
      return costoTexto;
    }

    public String getCuposTexto() {
      return cuposTexto;
    }

    public String getRequisitosTexto() {
      return requisitosTexto;
    }

    public String getEstadoCompatibilidadTexto() {
      return estadoCompatibilidadTexto;
    }

    public boolean isEsCompatible() {
      return esCompatible;
    }

    public int getColorEstadoCompatibilidad() {
      return colorEstadoCompatibilidad;
    }
  }

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

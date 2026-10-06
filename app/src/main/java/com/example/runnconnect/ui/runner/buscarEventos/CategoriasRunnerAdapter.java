package com.example.runnconnect.ui.runner.buscarEventos;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
    public static final int ACCION_NINGUNA = 0;
    public static final int ACCION_INSCRIBIR = 1;
    public static final int ACCION_SUBIR_COMPROBANTE = 2;
    public static final int ACCION_VER_COMPROBANTE = 3;

    private final int idCategoria;
    private final String nombre;
    private final String costoTexto;
    private final String cuposTexto;
    private final String requisitosTexto;
    private final String estadoCompatibilidadTexto;
    private final int colorEstadoCompatibilidad;
    private final String textoBoton;
    private final boolean botonHabilitado;
    private final int colorBoton;
    private final int tipoAccion;
    private final int idInscripcion;
    private final String urlComprobante;

    public CategoriaCompatibilidadUI(int idCategoria,
                                     String nombre,
                                     String costoTexto,
                                     String cuposTexto,
                                     String requisitosTexto,
                                     String estadoCompatibilidadTexto,
                                     int colorEstadoCompatibilidad,
                                     String textoBoton,
                                     boolean botonHabilitado,
                                     int colorBoton,
                                     int tipoAccion,
                                     int idInscripcion,
                                     String urlComprobante) {
      this.idCategoria = idCategoria;
      this.nombre = nombre;
      this.costoTexto = costoTexto;
      this.cuposTexto = cuposTexto;
      this.requisitosTexto = requisitosTexto;
      this.estadoCompatibilidadTexto = estadoCompatibilidadTexto;
      this.colorEstadoCompatibilidad = colorEstadoCompatibilidad;
      this.textoBoton = textoBoton;
      this.botonHabilitado = botonHabilitado;
      this.colorBoton = colorBoton;
      this.tipoAccion = tipoAccion;
      this.idInscripcion = idInscripcion;
      this.urlComprobante = urlComprobante != null ? urlComprobante : "";
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

    public int getColorEstadoCompatibilidad() {
      return colorEstadoCompatibilidad;
    }

    public String getTextoBoton() {
      return textoBoton;
    }

    public boolean isBotonHabilitado() {
      return botonHabilitado;
    }

    public int getColorBoton() {
      return colorBoton;
    }

    public int getTipoAccion() {
      return tipoAccion;
    }

    public int getIdInscripcion() {
      return idInscripcion;
    }

    public String getUrlComprobante() {
      return urlComprobante;
    }
  }

  private List<CategoriaCompatibilidadUI> lista = new ArrayList<>();
  private final OnCategoriaAccionListener listener;

  public interface OnCategoriaAccionListener {
    void onInscribirmeClick(CategoriaCompatibilidadUI item);
    void onSubirComprobanteClick(CategoriaCompatibilidadUI item);
    void onVerComprobanteClick(CategoriaCompatibilidadUI item);
  }

  public CategoriasRunnerAdapter(OnCategoriaAccionListener listener) {
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

      boolean tieneBadge = item.getEstadoCompatibilidadTexto() != null && !item.getEstadoCompatibilidadTexto().trim().isEmpty();
      binding.tvCatCompatibilidad.setVisibility(tieneBadge ? View.VISIBLE : View.GONE);
      binding.tvCatCompatibilidad.setText(item.getEstadoCompatibilidadTexto());
      binding.tvCatCompatibilidad.setTextColor(item.getColorEstadoCompatibilidad());

      binding.btnInscribirme.setText(item.getTextoBoton());
      binding.btnInscribirme.setEnabled(item.isBotonHabilitado());
      binding.btnInscribirme.setBackgroundTintList(ColorStateList.valueOf(item.getColorBoton()));
      binding.btnInscribirme.setTextColor(Color.WHITE);

      binding.btnInscribirme.setOnClickListener(v -> {
        if (item.getTipoAccion() == CategoriaCompatibilidadUI.ACCION_INSCRIBIR) {
          listener.onInscribirmeClick(item);
        } else if (item.getTipoAccion() == CategoriaCompatibilidadUI.ACCION_SUBIR_COMPROBANTE) {
          listener.onSubirComprobanteClick(item);
        } else if (item.getTipoAccion() == CategoriaCompatibilidadUI.ACCION_VER_COMPROBANTE) {
          listener.onVerComprobanteClick(item);
        }
      });
    }
  }
}

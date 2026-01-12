package com.example.runnconnect.ui.organizador.misEventos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.runnconnect.R;
import com.example.runnconnect.data.response.EventoResumenResponse;
import java.util.ArrayList;
import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {
  public interface OnEventoClickListener{
    void onEventoClick(int idEvento);
  }
  private List<EventoResumenResponse> lista = new ArrayList<>();
  private OnEventoClickListener listener;

  //carga inicial
  public void setEventos(List<EventoResumenResponse> nuevosEventos) {
    this.lista = new ArrayList<>(nuevosEventos); //copia
    notifyDataSetChanged();
  }

  public void setOnEventoClickListener(OnEventoClickListener listener) {
    this.listener = listener;
  }

  @NonNull
  @Override
  public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento_organizador, parent, false);
    return new EventoViewHolder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
    EventoResumenResponse evento = lista.get(position);

    // VALIDACION DE NULOS (Para evitar crashes)
    String nombre = (evento.getNombre() != null) ? evento.getNombre() : "Sin nombre";
    String fecha = (evento.getFechaHora() != null) ? evento.getFechaHora().replace("T", " ") : "--/--/----";
    String lugar = (evento.getLugar() != null) ? evento.getLugar() : "Sin ubicación";

    // Manejo seguro del estado
    String estado = (evento.getEstado() != null) ? evento.getEstado().toUpperCase() : "DESCONOCIDO";

    holder.tvNombre.setText(nombre);
    holder.tvFecha.setText("Fecha: " + fecha);
    holder.tvLugar.setText("Lugar: " + lugar);
    holder.tvInscriptos.setText("Inscriptos: "+evento.getInscriptosActuales());
    holder.tvCupo.setText("Cupo Total: " + evento.getCupoTotal());
    holder.tvEstado.setText(estado);

    // Lógica de colores segura
    if (estado.equals("PUBLICADO")) {
      holder.tvEstado.setTextColor(Color.parseColor("#2E7D32")); // Verde
      holder.tvEstado.setBackgroundColor(Color.parseColor("#E8F5E9"));
    } else if (estado.equals("FINALIZADO")) {
      holder.tvEstado.setTextColor(Color.parseColor("#616161")); // Gris
      holder.tvEstado.setBackgroundColor(Color.parseColor("#F5F5F5"));
    } else {
      holder.tvEstado.setTextColor(Color.parseColor("#C62828")); // Rojo
      holder.tvEstado.setBackgroundColor(Color.parseColor("#FFEBEE"));
    }

    //conf de click en la tarjeta
    holder.itemView.setOnClickListener(v->{
      if(listener !=null){
        listener.onEventoClick(evento.getIdEvento());
      }
    });
  }

  @Override
  public int getItemCount() { return lista.size(); }

  static class EventoViewHolder extends RecyclerView.ViewHolder {
    TextView tvNombre, tvFecha, tvLugar, tvEstado, tvCupo, tvInscriptos;
    public EventoViewHolder(@NonNull View itemView) {
      super(itemView);
      tvNombre = itemView.findViewById(R.id.tvNombreEvento);
      tvFecha = itemView.findViewById(R.id.tvFecha);
      tvLugar = itemView.findViewById(R.id.tvLugar);
      tvEstado = itemView.findViewById(R.id.tvEstado);
      tvCupo = itemView.findViewById(R.id.tvCupos);
      tvInscriptos=itemView.findViewById(R.id.tvInscriptos);
    }
  }

  //nuevo metodo para la paginancion
  public void agregarEventos(List<EventoResumenResponse> masEventos) {
    int posicionInicio = this.lista.size();
    this.lista.addAll(masEventos);
    notifyItemRangeInserted(posicionInicio, masEventos.size());
  }


}
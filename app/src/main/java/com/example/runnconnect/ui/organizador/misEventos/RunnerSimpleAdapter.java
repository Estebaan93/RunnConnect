package com.example.runnconnect.ui.organizador.misEventos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.runnconnect.R;
import com.example.runnconnect.data.response.InscriptoEventoResponse;
import java.util.ArrayList;
import java.util.List;

public class RunnerSimpleAdapter extends RecyclerView.Adapter<RunnerSimpleAdapter.ViewHolder> {

  private List<InscriptoEventoResponse> lista = new ArrayList<>();
  private final OnBajaClickListener listener;

  public interface OnBajaClickListener {
    void onBaja(InscriptoEventoResponse runner);
  }

  public RunnerSimpleAdapter(OnBajaClickListener listener) {
    this.listener = listener;
  }

  public void setLista(List<InscriptoEventoResponse> nuevaLista) {
    this.lista = nuevaLista;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // Puedes reutilizar un layout existente o crear uno nuevo simple.
    // Aquí asumo que creas 'item_runner_dialog.xml'
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_runner_dialog, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    InscriptoEventoResponse item = lista.get(position);

    if (item.getRunner() != null) {
      holder.tvNombre.setText(item.getRunner().getNombre() + " " + item.getRunner().getApellido());
      holder.tvDni.setText("DNI: " + item.getRunner().getDni());
    }

    holder.tvEstado.setText(item.getEstadoPago().toUpperCase());

    // Lógica visual del botón Baja
    if ("cancelado".equalsIgnoreCase(item.getEstadoPago())) {
      holder.btnBaja.setVisibility(View.GONE);
      holder.tvEstado.setTextColor(Color.RED);
    } else {
      holder.btnBaja.setVisibility(View.VISIBLE);
      holder.tvEstado.setTextColor(Color.BLACK);
    }

    holder.btnBaja.setOnClickListener(v -> listener.onBaja(item));
  }

  @Override
  public int getItemCount() { return lista.size(); }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvNombre, tvDni, tvEstado;
    ImageView btnBaja;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvNombre = itemView.findViewById(R.id.tvRunnerNombre);
      tvDni = itemView.findViewById(R.id.tvRunnerDni);
      tvEstado = itemView.findViewById(R.id.tvRunnerEstado);
      btnBaja = itemView.findViewById(R.id.btnDarBaja);
    }
  }
}
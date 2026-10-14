package com.example.runnconnect.utils;

public class FechaUtils {

  public static String formatearFecha(String fechaIso) {
    if (fechaIso == null || fechaIso.trim().isEmpty()) {
      return "-";
    }

    try {
      String soloFecha = fechaIso.contains("T")
          ? fechaIso.split("T")[0]
          : (fechaIso.contains(" ") ? fechaIso.split(" ")[0] : fechaIso);

      String[] partes = soloFecha.trim().split("[-/]");
      if (partes.length == 3) {
        String p0 = partes[0].trim();
        String p1 = partes[1].trim();
        String p2 = partes[2].trim();

        if (p2.length() > 2 && p0.length() == 4) {
          p2 = p2.substring(0, 2);
        }

        // Si viene en formato yyyy-MM-dd
        if (p0.length() == 4) {
          return String.format("%02d-%02d-%s", Integer.parseInt(p2), Integer.parseInt(p1), p0);
        }
        // Si ya viene en formato dd-MM-yyyy
        if (p2.length() == 4) {
          return String.format("%02d-%02d-%s", Integer.parseInt(p0), Integer.parseInt(p1), p2);
        }
      }
    } catch (Exception ignored) {}

    return fechaIso;
  }

  public static String formatearFechaHora(String fechaHoraIso) {
    if (fechaHoraIso == null || fechaHoraIso.trim().isEmpty()) {
      return "-";
    }

    try {
      String separador = fechaHoraIso.contains("T") ? "T" : (fechaHoraIso.contains(" ") ? " " : null);
      if (separador != null) {
        String[] partes = fechaHoraIso.split(separador);
        String fechaFormateada = formatearFecha(partes[0]);
        if (partes.length > 1 && !partes[1].trim().isEmpty()) {
          String[] partesHora = partes[1].trim().split(":");
          if (partesHora.length >= 2) {
            return fechaFormateada + " " + partesHora[0] + ":" + partesHora[1];
          }
        }
        return fechaFormateada;
      } else {
        return formatearFecha(fechaHoraIso);
      }
    } catch (Exception ignored) {}

    return fechaHoraIso.replace("T", " ");
  }

  public static String aFormatoApi(String fecha) {
    if (fecha == null || fecha.trim().isEmpty()) {
      return "";
    }

    try {
      String limpia = fecha.trim().split(" ")[0];
      String[] partes = limpia.split("[-/]");
      if (partes.length == 3) {
        if (partes[0].length() == 4) {
          return partes[0] + "-" + partes[1] + "-" + partes[2];
        }
        if (partes[2].length() == 4) {
          return String.format("%s-%02d-%02d", partes[2], Integer.parseInt(partes[1]), Integer.parseInt(partes[0]));
        }
      }
    } catch (Exception ignored) {}

    return fecha;
  }
}

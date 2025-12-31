package com.example.runnconnect.utils;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class LocalidadesHelper {

  public static List<String> getProvincias() {
    return Arrays.asList(
      "San Luis",
      "Buenos Aires",
      "Mendoza",
      "Córdoba",
      "San Juan",
      "La Pampa",
      "Santa Fe",
      "La Rioja",
      "Otra"
    );
  }

  public static List<String> getLocalidades(String provincia) {
    List<String> localidades;

    switch (provincia) {
      case "San Luis":
        localidades = Arrays.asList(
          "San Luis Capital", "Villa Mercedes", "Merlo", "La Punta", "Juana Koslay",
          "Potrero de los Funes", "Justo Daract", "Santa Rosa del Conlara", "Naschel",
          "Tilisarao", "Quines", "La Toma", "Buena Esperanza", "San Francisco del Monte de Oro",
          "Candelaria", "Luján", "El Volcán", "Carpintería", "Cortaderas", "Otra"
        );
        break;

      case "Buenos Aires":
        localidades = Arrays.asList(
          "La Plata", "Mar del Plata", "Bahía Blanca", "Tandil", "San Nicolás",
          "Pergamino", "Olavarría", "Junín", "Zárate", "Campana", "Luján",
          "Necochea", "Pilar", "Escobar", "Tigre", "San Isidro", "Quilmes",
          "Avellaneda", "Morón", "Lanús", "Ezeiza", "Otra"
        );
        break;

      case "Mendoza":
        localidades = Arrays.asList(
          "Mendoza Capital", "San Rafael", "Godoy Cruz", "Guaymallén", "Las Heras",
          "Maipú", "Luján de Cuyo", "San Martín", "Tunuyán", "General Alvear",
          "Malargüe", "Rivadavia", "Tupungato", "Lavalle", "Junín", "La Paz", "Otra"
        );
        break;

      case "Córdoba":
        localidades = Arrays.asList(
          "Córdoba Capital", "Río Cuarto", "Villa María", "Villa Carlos Paz",
          "San Francisco", "Alta Gracia", "Río Tercero", "Bell Ville", "La Falda",
          "Jesús María", "Cosquín", "Villa Dolores", "Cruz del Eje", "Mina Clavero",
          "Santa Rosa de Calamuchita", "Villa General Belgrano", "Otra"
        );
        break;

      case "San Juan":
        localidades = Arrays.asList(
          "San Juan Capital", "Rawson", "Rivadavia", "Chimbas", "Santa Lucía",
          "Pocito", "Caucete", "San José de Jáchal", "Albardón", "Sarmiento",
          "25 de Mayo", "9 de Julio", "San Martín", "Zonda", "Ullum", "Otra"
        );
        break;

      case "La Pampa":
        localidades = Arrays.asList(
          "Santa Rosa", "General Pico", "Toay", "General Acha", "Eduardo Castex",
          "25 de Mayo", "Intendente Alvear", "Realicó", "Victorica", "Macachín",
          "Ingeniero Luiggi", "Catriló", "Quemú Quemú", "Trenel", "Guatraché", "Otra"
        );
        break;

      case "Santa Fe":
        localidades = Arrays.asList(
          "Santa Fe Capital", "Rosario", "Rafaela", "Venado Tuerto", "Reconquista",
          "Santo Tomé", "Villa Gobernador Gálvez", "San Lorenzo", "Esperanza",
          "Casilda", "Granadero Baigorria", "Firmat", "Gálvez", "Sunchales",
          "Cañada de Gómez", "Coronda", "Otra"
        );
        break;

      case "La Rioja":
        localidades = Arrays.asList(
          "La Rioja Capital", "Chilecito", "Aimogasta", "Chamical", "Chepes",
          "Villa Unión", "Anillaco", "Famatina", "Olta", "Milagro", "Vinchina",
          "Villa Castelli", "Ulapes", "Tama", "Patquía", "Otra"
        );
        break;

      default:
        localidades = Arrays.asList("Capital", "Interior", "Otra");
        break;
    }


    return localidades;
  }
}
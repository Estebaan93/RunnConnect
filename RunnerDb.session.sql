-- Para borrar la BD local, y limpiarla asi pode importar el .sql de la nueva
DROP DATABASE runners_db;
CREATE DATABASE runners_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE runners_db;



-- CONSULTAS PARA LA BD RUNNERS
SELECT nombre, email, tipoUsuario FROM usuarios;

-- Ver notificaciones
SELECT * FROM notificaciones_evento;


-- ver eventos
SELECT e.nombre, e.idEvento, c.idCategoria, e.estado, e.cupoTotal FROM eventos e 
 INNER JOIN categorias_evento c ON e.idEvento= c.idEvento 
 WHERE e.estado="publicado";


-- ver punto interes
SELECT * FROM puntosinteres; 

SELECT tipo FROM puntosinteres;

DESCRIBE puntosinteres;

ALTER TABLE puntosinteres
MODIFY COLUMN tipo ENUM('hidratacion','primeros_auxilios','punto_energetico','otro')
NOT NULL;


ALTER TABLE puntosinteres
MODIFY COLUMN tipo VARCHAR(50) NOT NULL;

UPDATE puntosinteres
SET tipo = 'punto_energetico'
WHERE tipo IN ('meta','largada');

-- eventos
SELECT idEvento, nombre, estado, descripcion, fechaHora FROM eventos WHERE estado="publicado";

--cambiar fecha del evento
UPDATE eventos
SET fechaHora = '2026-02-02 19:00:00'
WHERE idEvento = 49;

SELECT idEvento, nombre, estado, fechaHora from  eventos;

SELECT * FROM inscripciones;
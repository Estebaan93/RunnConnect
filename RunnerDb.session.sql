-- Para borrar la BD local, y limpiarla asi pode importar el .sql de la nueva
DROP DATABASE runners_db;
CREATE DATABASE runners_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE runners_db;



-- CONSULTAS PARA LA BD RUNNERS
SELECT nombre, email, tipoUsuario FROM usuarios;

-- Ver notificaciones
SELECT * FROM notificaciones_evento;

--ver eventos 
SELECT DISTINCT e.idEvento ,e.nombre As "nombre evento", e.estado as "estado", e.fechaHora, c.nombre "nombre categ", c.estado as "estado categ" 
FROM eventos e 
LEFT JOIN categorias_evento c ON e.idEvento= c.idEvento 
WHERE e.idEvento=26;


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

-- agregar estado evento a categorias
USE `runners_db`;
ALTER TABLE `categorias_evento`
ADD COLUMN `estado` ENUM('programada', 'retrasada', 'cancelada', 'finalizada') 
DEFAULT 'programada' 
AFTER `genero`;

--
USE `runners_db`;

ALTER TABLE `categorias_evento`
MODIFY COLUMN `estado` 
ENUM('programada', 'retrasada', 'cancelada', 'finalizada', 'suspendido') 
DEFAULT 'programada';

--limpiar datos de evento 26
-- 1. Limpiar datos corruptos/viejos del evento de prueba
DELETE FROM inscripciones WHERE idCategoria IN (SELECT idCategoria FROM categorias_evento WHERE idEvento = 26);
DELETE FROM notificaciones_evento WHERE idEvento = 26;
DELETE FROM categorias_evento WHERE idEvento = 26;

-- 2. Asegurar que el evento padre esté sano
UPDATE eventos 
SET estado = 'publicado' 
WHERE idEvento = 26;

-- 3. Crear las categorías limpias (ahora soportarán 'suspendido' si lo necesitas probar)
INSERT INTO `categorias_evento` (idEvento, nombre, costoInscripcion, cupoCategoria, edadMinima, edadMaxima, genero, estado) VALUES 
(26, '5K Participativa', 5000.00, 500, 14, 99, 'X', 'programada'),
(26, '15K Competitiva', 15000.00, 300, 18, 99, 'X', 'programada'),
(26, '42K Elite', 25000.00, 200, 18, 99, 'X', 'programada');
-- CONSULTAS PARA LA BD RUNNERS
SELECT * FROM usuarios;

-- Ver notificaciones
SELECT * FROM notificaciones_evento;


-- ver eventos
SELECT e.nombre, e.idEvento, c.idCategoria, e.estado FROM eventos e 
 INNER JOIN categorias_evento c ON e.idEvento= c.idEvento 
 WHERE e.estado="publicado";
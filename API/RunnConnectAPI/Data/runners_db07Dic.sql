-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 08-12-2025 a las 00:04:35
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `runners_db`
--
CREATE DATABASE IF NOT EXISTS `runners_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `runners_db`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias_evento`
--

CREATE TABLE `categorias_evento` (
  `idCategoria` int(11) NOT NULL,
  `idEvento` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL COMMENT 'Ej: 10k Competitiva, 2k Corre Caminata',
  `costoInscripcion` decimal(10,2) NOT NULL DEFAULT 0.00,
  `cupoCategoria` int(11) DEFAULT NULL,
  `edadMinima` int(11) DEFAULT 0,
  `edadMaxima` int(11) DEFAULT 99,
  `genero` enum('F','M','X') DEFAULT 'X' COMMENT 'Categoría aplica a Femenino, Masculino o Mixto/Todos (X)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `categorias_evento`
--

INSERT INTO `categorias_evento` (`idCategoria`, `idEvento`, `nombre`, `costoInscripcion`, `cupoCategoria`, `edadMinima`, `edadMaxima`, `genero`) VALUES
(1, 1, '10K Competitiva', 5000.00, 200, 18, 90, 'X');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `eventos`
--

CREATE TABLE `eventos` (
  `idEvento` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `fechaHora` datetime NOT NULL,
  `lugar` varchar(255) NOT NULL,
  `cupoTotal` int(11) DEFAULT NULL,
  `idOrganizador` int(11) NOT NULL,
  `urlPronosticoClima` varchar(255) DEFAULT NULL,
  `datosPago` text DEFAULT NULL COMMENT 'Datos para transferencia (CBU, Alias, Titular), inicialmente puede ser nulo hasta que el orga cargue datos de alias',
  `estado` enum('publicado','cancelado','finalizado') NOT NULL DEFAULT 'publicado'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `eventos`
--

INSERT INTO `eventos` (`idEvento`, `nombre`, `descripcion`, `fechaHora`, `lugar`, `cupoTotal`, `idOrganizador`, `urlPronosticoClima`, `datosPago`, `estado`) VALUES
(1, 'Maratón San Luis 2025 - ACTUALIZADO', 'Carrera de 10K y 5K - Descripción actualizada', '2025-12-05 09:00:00', 'Plaza Pringles, San Luis Capital', 600, 5, 'https://www.weather.com/sanluisargentina', 'CBU: 0000003100012345678901 - Alias: RUNNERS.SL.2025', 'finalizado'),
(2, 'Maratón Independencia San Luis 2026', 'Carrera de 10K por las calles de San Luis', '2026-01-15 08:00:00', 'Av España - Av La Finur, San Luis', 1000, 5, 'https://www.weather.com/sanluisargentina', 'CBU: 0000003100012345678901 - Alias: RUNNERS.SL - Titular: Runners Club San Luis S.A', 'publicado');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inscripciones`
--

CREATE TABLE `inscripciones` (
  `idInscripcion` int(11) NOT NULL,
  `idUsuario` int(11) NOT NULL,
  `idCategoria` int(11) NOT NULL,
  `fechaInscripcion` datetime DEFAULT current_timestamp(),
  `estadoPago` enum('pendiente','procesando','pagado','rechazado','reembolsado','cancelado') DEFAULT 'pendiente',
  `talleRemera` enum('XS','S','M','L','XL','XXL') DEFAULT NULL,
  `aceptoDeslinde` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Debe ser true (1) para aceptar el deslinde',
  `comprobantePagoURL` varchar(255) DEFAULT NULL COMMENT 'URL o path al comprobante subido'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `inscripciones`
--

INSERT INTO `inscripciones` (`idInscripcion`, `idUsuario`, `idCategoria`, `fechaInscripcion`, `estadoPago`, `talleRemera`, `aceptoDeslinde`, `comprobantePagoURL`) VALUES
(2, 2, 1, '2025-12-03 15:36:12', 'pagado', 'L', 1, '/uploads/comprobantes/comprobante_2_20251203181006.pdf'),
(3, 4, 1, '2025-12-04 16:55:46', 'pagado', 'M', 1, '/uploads/comprobantes/comprobante_3_20251204165759.pdf');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `notificaciones_evento`
--

CREATE TABLE `notificaciones_evento` (
  `idNotificacion` int(11) NOT NULL,
  `idEvento` int(11) NOT NULL COMMENT 'Evento al que se asocia',
  `titulo` varchar(255) NOT NULL COMMENT 'Ej: Evento Suspendido',
  `mensaje` text DEFAULT NULL COMMENT 'Ej: La carrera se pasa al próximo domingo...',
  `fechaEnvio` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `perfiles_organizadores`
--

CREATE TABLE `perfiles_organizadores` (
  `idPerfilOrganizador` int(11) NOT NULL,
  `idUsuario` int(11) NOT NULL COMMENT 'FK a la tabla usuarios',
  `razonSocial` varchar(100) NOT NULL COMMENT 'Nombre Legal y Oficial de la entidad',
  `nombreComercial` varchar(100) NOT NULL COMMENT 'Nombre de marca que se usa públicamente (puede ser igual al campo nombre en usuarios)',
  `cuit_taxid` varchar(30) DEFAULT NULL COMMENT 'CUIT/ID Fiscal de la organizacion. Requisito para crear evento',
  `direccionLegal` varchar(255) DEFAULT NULL COMMENT 'Requisito para crear evento'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `perfiles_organizadores`
--

INSERT INTO `perfiles_organizadores` (`idPerfilOrganizador`, `idUsuario`, `razonSocial`, `nombreComercial`, `cuit_taxid`, `direccionLegal`) VALUES
(1, 3, 'Runners Club San Luis S.A', 'Runners Club SL', '30-12345678-9', 'Av Illia 435, San Luis'),
(2, 5, 'Club Deportivo La Punta', 'CD La Punta', '20-55667788-3', 'Av. Costanera s/n, La Punta, San Luis');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `perfiles_runners`
--

CREATE TABLE `perfiles_runners` (
  `idPerfilRunner` int(11) NOT NULL,
  `idUsuario` int(11) NOT NULL COMMENT 'FK a la tabla usuarios',
  `nombre` varchar(100) NOT NULL COMMENT 'Nombre de Pila del Runner',
  `apellido` varchar(100) NOT NULL COMMENT 'Apellido del Runner',
  `fechaNacimiento` datetime DEFAULT NULL COMMENT 'Completar post registro y requisito al inscribirse a evento',
  `genero` enum('F','M','X') DEFAULT NULL COMMENT 'Completar post registro y requisito al inscribirse a evento',
  `dni` int(11) DEFAULT NULL COMMENT 'DNI del Runner. Completar post registro y requisito al inscribirse a evento',
  `localidad` varchar(100) DEFAULT NULL COMMENT 'Completar post registro y requisito al inscribirse a evento',
  `agrupacion` varchar(100) DEFAULT NULL COMMENT 'Agrupacion o libre (si no tiene). Requisito antes de inscribirse a evento',
  `nombreContactoEmergencia` varchar(100) DEFAULT NULL COMMENT 'Nombre/Relacion contacto emergencia. Requisito para inscribirse a evento',
  `telefonoEmergencia` varchar(50) DEFAULT NULL COMMENT 'Contacto de emergencia. Requisito para inscribirse a evento'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `perfiles_runners`
--

INSERT INTO `perfiles_runners` (`idPerfilRunner`, `idUsuario`, `nombre`, `apellido`, `fechaNacimiento`, `genero`, `dni`, `localidad`, `agrupacion`, `nombreContactoEmergencia`, `telefonoEmergencia`) VALUES
(2, 2, 'Carlos Angel', 'González Pérez', '1990-03-20 00:00:00', 'M', 11111111, 'Juana Koslay', 'Equipo Trail Running SL', 'Emilia (pareja)', '2664888999'),
(3, 4, 'Test1 Runner Nombre', 'Test Runner Apellido', '2000-03-20 00:00:00', 'M', 22222222, 'Juana Koslay', 'Equipo Trail Running SL', 'Pareja', '2664888999');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `puntosinteres`
--

CREATE TABLE `puntosinteres` (
  `idPuntoInteres` int(11) NOT NULL,
  `idEvento` int(11) NOT NULL,
  `tipo` enum('hidratacion','primeros_auxilios','meta','largada','otro') NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `latitud` decimal(10,7) NOT NULL,
  `longitud` decimal(10,7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `puntosinteres`
--

INSERT INTO `puntosinteres` (`idPuntoInteres`, `idEvento`, `tipo`, `nombre`, `latitud`, `longitud`) VALUES
(1, 2, 'largada', 'Arco de Largada/Llegada', -33.3021500, -66.3368000),
(2, 2, 'hidratacion', 'Agua e Isotónica (Km 0.5)', -33.3032000, -66.3380000);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `resultados`
--

CREATE TABLE `resultados` (
  `idResultado` int(11) NOT NULL,
  `idInscripcion` int(11) NOT NULL,
  `tiempoOficial` varchar(20) DEFAULT NULL COMMENT 'Ej: 00:45:30.123',
  `posicionGeneral` int(11) DEFAULT NULL,
  `posicionCategoria` int(11) DEFAULT NULL,
  `tiempoSmartwatch` varchar(20) DEFAULT NULL COMMENT 'Ej: 00:45:28',
  `distanciaKm` decimal(6,2) DEFAULT NULL COMMENT 'Ej: 10.02',
  `ritmoPromedio` varchar(20) DEFAULT NULL COMMENT 'Ej: 4:32 min/km',
  `velocidadPromedio` varchar(20) DEFAULT NULL COMMENT 'Ej: 13.2 km/h',
  `caloriasQuemadas` int(11) DEFAULT NULL,
  `pulsacionesPromedio` int(11) DEFAULT NULL,
  `pulsacionesMax` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `resultados`
--

INSERT INTO `resultados` (`idResultado`, `idInscripcion`, `tiempoOficial`, `posicionGeneral`, `posicionCategoria`, `tiempoSmartwatch`, `distanciaKm`, `ritmoPromedio`, `velocidadPromedio`, `caloriasQuemadas`, `pulsacionesPromedio`, `pulsacionesMax`) VALUES
(1, 2, '00:45:10', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 3, '00:46:03', 2, 1, '00:48:15', 10.52, '04:35 min/km', '13.1 km/h', 850, 160, 185);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rutas`
--

CREATE TABLE `rutas` (
  `idRuta` int(11) NOT NULL,
  `idEvento` int(11) NOT NULL,
  `orden` int(11) NOT NULL COMMENT 'Orden del punto en el trazado',
  `latitud` decimal(10,7) NOT NULL,
  `longitud` decimal(10,7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `rutas`
--

INSERT INTO `rutas` (`idRuta`, `idEvento`, `orden`, `latitud`, `longitud`) VALUES
(1, 2, 1, -33.3021500, -66.3368000),
(2, 2, 2, -33.3021500, -66.3380000),
(3, 2, 3, -33.3032000, -66.3380000),
(4, 2, 4, -33.3032000, -66.3368000),
(5, 2, 5, -33.3021500, -66.3368000);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tokens_recuperacion`
--

CREATE TABLE `tokens_recuperacion` (
  `idToken` int(11) NOT NULL,
  `idUsuario` int(11) NOT NULL,
  `token` varchar(255) NOT NULL COMMENT 'Token unico generado',
  `tipoToken` varchar(20) NOT NULL DEFAULT 'recuperacion' COMMENT 'Tipo de token: recuperacion, reactivacion',
  `fechaCreacion` datetime NOT NULL DEFAULT current_timestamp(),
  `fechaExpiracion` datetime NOT NULL COMMENT 'Token valido por 1 hora',
  `usado` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'false=no usado, true=ya usado'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `tokens_recuperacion`
--

INSERT INTO `tokens_recuperacion` (`idToken`, `idUsuario`, `token`, `tipoToken`, `fechaCreacion`, `fechaExpiracion`, `usado`) VALUES
(1, 4, '9b933ab08a974a96a703ee8959e9be3e', 'recuperacion', '2025-11-25 22:04:24', '2025-11-25 23:04:24', 0),
(2, 4, '5a1d425c997349d38b9293a47e6e4369', 'recuperacion', '2025-11-25 22:20:38', '2025-11-25 23:20:38', 1),
(3, 4, '252701acb2af470eb213e038a907e310', 'recuperacion', '2025-11-27 09:37:27', '2025-11-27 10:37:27', 1),
(4, 4, '566866dc43ef43d59222003acddd2c31', 'reactivacion', '2025-11-27 09:51:16', '2025-11-27 10:51:16', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `idUsuario` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL COMMENT 'Nombre de Pila (Runner) o Nombre Comercial (Organizador)',
  `email` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL COMMENT 'Numero de celu',
  `passwordHash` varchar(255) NOT NULL,
  `tipoUsuario` enum('runner','organizador') NOT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT 1 COMMENT '0 false, 1 true (Al crear) sera de estado true',
  `imgAvatar` varchar(500) DEFAULT NULL COMMENT 'URL o ruta del avatar del usuario'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`idUsuario`, `nombre`, `email`, `telefono`, `passwordHash`, `tipoUsuario`, `estado`, `imgAvatar`) VALUES
(2, 'Carlos Angel', 'carlos@test.com', '2664222333', '$2a$12$1eEF/knMNucRBPUTHbWD1Ortnqnafy4rIlyGPrDSAKe10NIAKaRri', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(3, 'Juan Carlos', 'eventos@runnersclub.com', '2664111113', '$2a$12$4OatEBqyW4e6SJmeBhk8R.mT4StPBw5WtNHpuTBGldfeQ7NdKebpy', 'organizador', 1, '/uploads/avatars/defaults/default_organization.png'),
(4, 'Test1 Runner Nombre', 'esteban.dev22@gmail.com', '2664222222', '$2a$12$2ViVQFRklBukbJzIXLHbv.dNsYJ.vpPZedPPBdKHLJLEqVdMNgrg6', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(5, 'CD La Punta', 'test@orgaclub.com', '2664555888', '$2a$12$lW5ct48GPMIkPIc6HMhvee9799yiqGvgkibashONZslvx0WbJIXgW', 'organizador', 1, '/uploads/avatars/defaults/default_organization.png');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categorias_evento`
--
ALTER TABLE `categorias_evento`
  ADD PRIMARY KEY (`idCategoria`),
  ADD KEY `idEvento` (`idEvento`);

--
-- Indices de la tabla `eventos`
--
ALTER TABLE `eventos`
  ADD PRIMARY KEY (`idEvento`),
  ADD KEY `idOrganizador` (`idOrganizador`);

--
-- Indices de la tabla `inscripciones`
--
ALTER TABLE `inscripciones`
  ADD PRIMARY KEY (`idInscripcion`),
  ADD KEY `idUsuario` (`idUsuario`),
  ADD KEY `idCategoria` (`idCategoria`);

--
-- Indices de la tabla `notificaciones_evento`
--
ALTER TABLE `notificaciones_evento`
  ADD PRIMARY KEY (`idNotificacion`),
  ADD KEY `idEvento` (`idEvento`);

--
-- Indices de la tabla `perfiles_organizadores`
--
ALTER TABLE `perfiles_organizadores`
  ADD PRIMARY KEY (`idPerfilOrganizador`),
  ADD UNIQUE KEY `razonSocial` (`razonSocial`),
  ADD UNIQUE KEY `cuit_taxid` (`cuit_taxid`),
  ADD UNIQUE KEY `idUsuario_UNIQUE` (`idUsuario`);

--
-- Indices de la tabla `perfiles_runners`
--
ALTER TABLE `perfiles_runners`
  ADD PRIMARY KEY (`idPerfilRunner`),
  ADD UNIQUE KEY `dni` (`dni`),
  ADD UNIQUE KEY `idUsuario_UNIQUE` (`idUsuario`);

--
-- Indices de la tabla `puntosinteres`
--
ALTER TABLE `puntosinteres`
  ADD PRIMARY KEY (`idPuntoInteres`),
  ADD KEY `idEvento` (`idEvento`);

--
-- Indices de la tabla `resultados`
--
ALTER TABLE `resultados`
  ADD PRIMARY KEY (`idResultado`),
  ADD UNIQUE KEY `idInscripcion` (`idInscripcion`) COMMENT 'Garantiza Relación 1:1';

--
-- Indices de la tabla `rutas`
--
ALTER TABLE `rutas`
  ADD PRIMARY KEY (`idRuta`),
  ADD KEY `idEvento` (`idEvento`);

--
-- Indices de la tabla `tokens_recuperacion`
--
ALTER TABLE `tokens_recuperacion`
  ADD PRIMARY KEY (`idToken`),
  ADD UNIQUE KEY `token_UNIQUE` (`token`),
  ADD KEY `fk_tokens_usuarios` (`idUsuario`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`idUsuario`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `email_2` (`email`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `categorias_evento`
--
ALTER TABLE `categorias_evento`
  MODIFY `idCategoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `eventos`
--
ALTER TABLE `eventos`
  MODIFY `idEvento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `inscripciones`
--
ALTER TABLE `inscripciones`
  MODIFY `idInscripcion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `notificaciones_evento`
--
ALTER TABLE `notificaciones_evento`
  MODIFY `idNotificacion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `perfiles_organizadores`
--
ALTER TABLE `perfiles_organizadores`
  MODIFY `idPerfilOrganizador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `perfiles_runners`
--
ALTER TABLE `perfiles_runners`
  MODIFY `idPerfilRunner` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `puntosinteres`
--
ALTER TABLE `puntosinteres`
  MODIFY `idPuntoInteres` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `resultados`
--
ALTER TABLE `resultados`
  MODIFY `idResultado` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `rutas`
--
ALTER TABLE `rutas`
  MODIFY `idRuta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `tokens_recuperacion`
--
ALTER TABLE `tokens_recuperacion`
  MODIFY `idToken` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `idUsuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `categorias_evento`
--
ALTER TABLE `categorias_evento`
  ADD CONSTRAINT `categorias_evento_ibfk_1` FOREIGN KEY (`idEvento`) REFERENCES `eventos` (`idEvento`) ON DELETE CASCADE;

--
-- Filtros para la tabla `eventos`
--
ALTER TABLE `eventos`
  ADD CONSTRAINT `eventos_ibfk_1` FOREIGN KEY (`idOrganizador`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `inscripciones`
--
ALTER TABLE `inscripciones`
  ADD CONSTRAINT `inscripciones_ibfk_1` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE,
  ADD CONSTRAINT `inscripciones_ibfk_2` FOREIGN KEY (`idCategoria`) REFERENCES `categorias_evento` (`idCategoria`) ON DELETE CASCADE;

--
-- Filtros para la tabla `notificaciones_evento`
--
ALTER TABLE `notificaciones_evento`
  ADD CONSTRAINT `notificaciones_evento_ibfk_1` FOREIGN KEY (`idEvento`) REFERENCES `eventos` (`idEvento`) ON DELETE CASCADE;

--
-- Filtros para la tabla `perfiles_organizadores`
--
ALTER TABLE `perfiles_organizadores`
  ADD CONSTRAINT `fk_perfiles_organizadores_usuarios` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `perfiles_runners`
--
ALTER TABLE `perfiles_runners`
  ADD CONSTRAINT `fk_perfiles_runners_usuarios` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `puntosinteres`
--
ALTER TABLE `puntosinteres`
  ADD CONSTRAINT `puntosinteres_ibfk_1` FOREIGN KEY (`idEvento`) REFERENCES `eventos` (`idEvento`) ON DELETE CASCADE;

--
-- Filtros para la tabla `resultados`
--
ALTER TABLE `resultados`
  ADD CONSTRAINT `resultados_ibfk_1` FOREIGN KEY (`idInscripcion`) REFERENCES `inscripciones` (`idInscripcion`) ON DELETE CASCADE;

--
-- Filtros para la tabla `rutas`
--
ALTER TABLE `rutas`
  ADD CONSTRAINT `rutas_ibfk_1` FOREIGN KEY (`idEvento`) REFERENCES `eventos` (`idEvento`) ON DELETE CASCADE;

--
-- Filtros para la tabla `tokens_recuperacion`
--
ALTER TABLE `tokens_recuperacion`
  ADD CONSTRAINT `fk_tokens_usuarios` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

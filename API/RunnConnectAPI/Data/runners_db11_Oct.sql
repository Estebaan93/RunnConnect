-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 12-10-2026 a las 03:29:28
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.0.30

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
CREATE DATABASE IF NOT EXISTS `runners_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
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
  `genero` enum('F','M','X') DEFAULT 'X' COMMENT 'Categoría aplica a Femenino, Masculino o Mixto/Todos (X)',
  `estado` enum('programada','retrasada','cancelada','finalizada','suspendido') DEFAULT 'programada'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `categorias_evento`
--

INSERT INTO `categorias_evento` (`idCategoria`, `idEvento`, `nombre`, `costoInscripcion`, `cupoCategoria`, `edadMinima`, `edadMaxima`, `genero`, `estado`) VALUES
(104, 91, '5K', 60000.00, NULL, 1, 80, 'X', 'programada'),
(105, 92, '3K', 60000.00, NULL, 18, 80, 'X', 'finalizada'),
(106, 93, '5K', 70000.00, NULL, 18, 80, 'X', 'programada');

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
  `tipoEvento` enum('calle','trail','cross','aventura','obstaculos','correcaminata','kids','triatlon') NOT NULL DEFAULT 'calle',
  `cupoTotal` int(11) DEFAULT NULL,
  `idOrganizador` int(11) NOT NULL,
  `urlPronosticoClima` varchar(255) DEFAULT NULL,
  `datosPago` text DEFAULT NULL COMMENT 'Datos para transferencia (CBU, Alias, Titular), inicialmente puede ser nulo hasta que el orga cargue datos de alias',
  `estado` enum('publicado','cancelado','finalizado','suspendido','retrasado') DEFAULT 'publicado'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `eventos`
--

INSERT INTO `eventos` (`idEvento`, `nombre`, `descripcion`, `fechaHora`, `lugar`, `tipoEvento`, `cupoTotal`, `idOrganizador`, `urlPronosticoClima`, `datosPago`, `estado`) VALUES
(91, 'Primavera 26', '3er temporada', '2026-12-16 12:10:00', 'San Luis', 'calle', NULL, 1, '{\"min\":13,\"max\":14}', 'Alias: Runnes.mp', 'publicado'),
(92, 'TestPrimavera', 'TestPrimavera', '2026-10-10 12:00:00', 'San Luis', 'calle', NULL, 1, NULL, 'Alias: runner.mpmp', 'finalizado'),
(93, 'Test3primavera', 'descripcionPirmavera', '2026-11-25 12:00:00', 'san Luis', 'calle', NULL, 1, '{\"min\":13,\"max\":13}', 'Alias: SLRUN.mp', 'publicado');

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
  `comprobantePagoURL` varchar(255) DEFAULT NULL COMMENT 'URL o path al comprobante subido',
  `observacion` varchar(255) NOT NULL COMMENT 'observacion del estado de pago'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `inscripciones`
--

INSERT INTO `inscripciones` (`idInscripcion`, `idUsuario`, `idCategoria`, `fechaInscripcion`, `estadoPago`, `talleRemera`, `aceptoDeslinde`, `comprobantePagoURL`, `observacion`) VALUES
(74, 2, 105, '2026-10-11 18:59:07', 'pagado', 'XS', 1, '/uploads/comprobantes/comprobante_74_20261011185914.png', 'Pago confirmado por organizador'),
(75, 3, 105, '2026-10-11 19:09:43', 'pagado', 'L', 1, '/uploads/comprobantes/comprobante_75_20261011190948.png', 'Pago confirmado por organizador');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `notificaciones_evento`
--

CREATE TABLE `notificaciones_evento` (
  `idNotificacion` int(11) NOT NULL,
  `idEvento` int(11) NOT NULL COMMENT 'Evento al que se asocia',
  `idCategoria` int(11) DEFAULT NULL,
  `idUsuarioDestino` int(11) DEFAULT NULL COMMENT 'Si es NULL es pública para el evento, si tiene ID es un mensaje privado para ese usuario',
  `titulo` varchar(255) NOT NULL COMMENT 'Ej: Evento Suspendido',
  `mensaje` text DEFAULT NULL COMMENT 'Ej: La carrera se pasa al próximo domingo...',
  `fechaEnvio` datetime DEFAULT current_timestamp(),
  `estadoEvento` varchar(50) DEFAULT NULL COMMENT 'Estado del evento al momento de crear la notificacion',
  `esAnuncioGlobal` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `notificaciones_evento`
--

INSERT INTO `notificaciones_evento` (`idNotificacion`, `idEvento`, `idCategoria`, `idUsuarioDestino`, `titulo`, `mensaje`, `fechaEnvio`, `estadoEvento`, `esAnuncioGlobal`) VALUES
(162, 91, NULL, NULL, '¡Nuevo Evento Disponible!', 'Primavera 26 en San Luis. ¡Inscripciones abiertas!', '2026-10-05 23:11:35', 'publicado', 1),
(163, 92, NULL, NULL, '¡Nuevo Evento Disponible!', 'TestPrimavera en San Luis. ¡Inscripciones abiertas!', '2026-10-05 23:22:48', 'publicado', 1),
(164, 93, NULL, NULL, '¡Nuevo Evento Disponible!', 'Test3primavera en san Luis. ¡Inscripciones abiertas!', '2026-10-05 23:46:16', 'publicado', 1),
(165, 92, 105, 2, '¡Pago Confirmado!', 'Tu pago para el evento \'TestPrimavera\' ha sido aprobado.', '2026-10-11 18:59:48', 'publicado', 0),
(166, 92, 105, 3, '¡Pago Confirmado!', 'Tu pago para el evento \'TestPrimavera\' ha sido aprobado.', '2026-10-11 19:10:08', 'publicado', 0),
(167, 92, 105, NULL, '¡Resultados Oficiales Disponibles!', 'Se han cargado los tiempos de tu categoría. ¡Revisa tu posición en los Podios!', '2026-10-11 19:31:42', 'finalizado', 0);

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
  `direccionLegal` varchar(255) DEFAULT NULL COMMENT 'Requisito para crear evento',
  `fechaUltimaLectura` datetime DEFAULT '2000-01-01 00:00:00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `perfiles_organizadores`
--

INSERT INTO `perfiles_organizadores` (`idPerfilOrganizador`, `idUsuario`, `razonSocial`, `nombreComercial`, `cuit_taxid`, `direccionLegal`, `fechaUltimaLectura`) VALUES
(7, 1, 'RunnerSL', 'Runners San Luis', '20123321457', 'Sarmiento 95', '2026-10-11 22:24:34');

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
  `telefonoEmergencia` varchar(50) DEFAULT NULL COMMENT 'Contacto de emergencia. Requisito para inscribirse a evento',
  `fechaUltimaLectura` datetime DEFAULT '2000-01-01 00:00:00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `perfiles_runners`
--

INSERT INTO `perfiles_runners` (`idPerfilRunner`, `idUsuario`, `nombre`, `apellido`, `fechaNacimiento`, `genero`, `dni`, `localidad`, `agrupacion`, `nombreContactoEmergencia`, `telefonoEmergencia`, `fechaUltimaLectura`) VALUES
(13, 2, 'Esteban', 'Moreira', '1993-08-11 00:00:00', 'M', 37599292, 'San Luis Capital, San Luis', 'Sin agrupacion', 'Pareja', '1158596061', '2026-10-11 22:01:50'),
(14, 3, 'Marilena', 'Escudero', '1985-06-06 00:00:00', 'F', 33012305, 'San Luis Capital, San Luis', 'Sin Agrupacion', 'Pareja', '1112131415', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `puntosinteres`
--

CREATE TABLE `puntosinteres` (
  `idPuntoInteres` int(11) NOT NULL,
  `idEvento` int(11) NOT NULL,
  `tipo` enum('hidratacion','primeros_auxilios','punto_energetico','otro') NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `latitud` decimal(10,7) NOT NULL,
  `longitud` decimal(10,7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `puntosinteres`
--

INSERT INTO `puntosinteres` (`idPuntoInteres`, `idEvento`, `tipo`, `nombre`, `latitud`, `longitud`) VALUES
(92, 91, 'hidratacion', 'Puesto de Hidratación', -33.2660195, -66.3318383),
(93, 91, 'punto_energetico', 'Punto Energético', -33.2712725, -66.3201077),
(94, 91, 'primeros_auxilios', 'Primeros Auxilios', -33.2846914, -66.3025043),
(95, 93, 'hidratacion', 'Puesto de Hidratación', -33.2801057, -66.3348055),
(96, 93, 'hidratacion', 'Puesto de Hidratación', -33.2816706, -66.3309833),
(97, 93, 'punto_energetico', 'Punto Energético', -33.2879843, -66.3371876),
(98, 93, 'otro', 'Punto de Interés', -33.2904024, -66.3327502),
(99, 93, 'punto_energetico', 'Punto Energético', -33.2907427, -66.3294008);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `resultados`
--

CREATE TABLE `resultados` (
  `idResultado` int(11) NOT NULL,
  `idInscripcion` int(11) NOT NULL,
  `tiempoOficial` varchar(20) DEFAULT NULL COMMENT 'Ej: 00:45:30.123',
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

INSERT INTO `resultados` (`idResultado`, `idInscripcion`, `tiempoOficial`, `posicionCategoria`, `tiempoSmartwatch`, `distanciaKm`, `ritmoPromedio`, `velocidadPromedio`, `caloriasQuemadas`, `pulsacionesPromedio`, `pulsacionesMax`) VALUES
(54, 74, '00:30:10', 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(55, 75, '00:28:45', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

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
(2429, 91, 1, -33.2623986, -66.3401421),
(2430, 91, 2, -33.2754119, -66.3110415),
(2431, 91, 3, -33.2763697, -66.3087693),
(2432, 91, 4, -33.2789941, -66.3050417),
(2433, 91, 5, -33.2812765, -66.3033579),
(2434, 91, 6, -33.2833674, -66.3027038),
(2435, 91, 7, -33.2871281, -66.3020108),
(2436, 91, 8, -33.2893147, -66.3016185),
(2437, 91, 9, -33.2899518, -66.3014794),
(2438, 92, 1, -33.2804037, -66.3380590),
(2439, 92, 2, -33.2799664, -66.3312905),
(2440, 92, 3, -33.2896561, -66.3294267),
(2441, 92, 4, -33.2909680, -66.3366529),
(2442, 92, 5, -33.2971073, -66.3356169),
(2443, 92, 6, -33.2963832, -66.3282109),
(2444, 92, 7, -33.2996193, -66.3275873),
(2445, 92, 8, -33.2984990, -66.3221103),
(2446, 92, 9, -33.2894128, -66.3234426),
(2447, 92, 10, -33.2791317, -66.3248907),
(2448, 92, 11, -33.2795146, -66.3289123),
(2460, 93, 1, -33.2804107, -66.3378773),
(2461, 93, 2, -33.2799325, -66.3314034),
(2462, 93, 3, -33.2843607, -66.3304060),
(2463, 93, 4, -33.2848935, -66.3374032),
(2464, 93, 5, -33.2910708, -66.3367001),
(2465, 93, 6, -33.2898545, -66.3295886),
(2466, 93, 7, -33.2923992, -66.3290448),
(2467, 93, 8, -33.2929323, -66.3365489),
(2468, 93, 9, -33.3010174, -66.3345983),
(2469, 93, 10, -33.2997465, -66.3275357),
(2470, 93, 11, -33.2963030, -66.3281898);

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
(1, 'Runners San Luis', 'run@sl.com', '2665859556', '$2a$12$yMHvuEeyD3lJ9GMniijgK.OwkJl2bumruH/VDYtwTCbGV8hpdRPqm', 'organizador', 1, '/uploads/avatars/defaults/default_organization.png'),
(2, 'Esteban', 'esteban.dev22@gmail.com', '1158133266', '$2a$12$RSPrZp08/anrtyipVxQ.aOzOGe1IVbLXcLO9sm3a6Y5PmcMdj6Rpy', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(3, 'Marilena', 'marusofok2@gmail.com', '1178798081', '$2a$12$Bzghz1J.TB3UJF3ebxs9QOEybtDhWbJxM3w7iOnh0op290Rv8FTNy', 'runner', 1, '/uploads/avatars/defaults/default_runner.png');

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
  ADD KEY `idEvento` (`idEvento`),
  ADD KEY `fk_notificaciones_categorias` (`idCategoria`),
  ADD KEY `fk_notificaciones_usuario` (`idUsuarioDestino`);

--
-- Indices de la tabla `perfiles_organizadores`
--
ALTER TABLE `perfiles_organizadores`
  ADD PRIMARY KEY (`idPerfilOrganizador`),
  ADD UNIQUE KEY `razonSocial` (`razonSocial`),
  ADD UNIQUE KEY `idUsuario_UNIQUE` (`idUsuario`),
  ADD UNIQUE KEY `cuit_taxid` (`cuit_taxid`);

--
-- Indices de la tabla `perfiles_runners`
--
ALTER TABLE `perfiles_runners`
  ADD PRIMARY KEY (`idPerfilRunner`),
  ADD UNIQUE KEY `idUsuario_UNIQUE` (`idUsuario`),
  ADD UNIQUE KEY `dni` (`dni`);

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
  MODIFY `idCategoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=107;

--
-- AUTO_INCREMENT de la tabla `eventos`
--
ALTER TABLE `eventos`
  MODIFY `idEvento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=94;

--
-- AUTO_INCREMENT de la tabla `inscripciones`
--
ALTER TABLE `inscripciones`
  MODIFY `idInscripcion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=76;

--
-- AUTO_INCREMENT de la tabla `notificaciones_evento`
--
ALTER TABLE `notificaciones_evento`
  MODIFY `idNotificacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=168;

--
-- AUTO_INCREMENT de la tabla `perfiles_organizadores`
--
ALTER TABLE `perfiles_organizadores`
  MODIFY `idPerfilOrganizador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `perfiles_runners`
--
ALTER TABLE `perfiles_runners`
  MODIFY `idPerfilRunner` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `puntosinteres`
--
ALTER TABLE `puntosinteres`
  MODIFY `idPuntoInteres` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=100;

--
-- AUTO_INCREMENT de la tabla `resultados`
--
ALTER TABLE `resultados`
  MODIFY `idResultado` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;

--
-- AUTO_INCREMENT de la tabla `rutas`
--
ALTER TABLE `rutas`
  MODIFY `idRuta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2471;

--
-- AUTO_INCREMENT de la tabla `tokens_recuperacion`
--
ALTER TABLE `tokens_recuperacion`
  MODIFY `idToken` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `idUsuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

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
  ADD CONSTRAINT `fk_notificaciones_categorias` FOREIGN KEY (`idCategoria`) REFERENCES `categorias_evento` (`idCategoria`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_notificaciones_usuario` FOREIGN KEY (`idUsuarioDestino`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE,
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

-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 05-02-2026 a las 04:04:09
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
(1, 1, '10K Competitiva', 5000.00, 200, 18, 90, 'X'),
(3, 5, '5K Calle', 50000.00, 5000, 18, 85, 'X'),
(23, 26, '7K Calle', 1.00, 5, 18, 60, 'X'),
(47, 26, '15K Competitiva', 5000.00, 3, 16, 90, 'F'),
(48, 50, '21K Calle', 20000.00, 10, 17, 90, 'X'),
(49, 51, '3K Calle', 3000.00, 10, 18, 80, 'X'),
(50, 51, '6K Calle', 4000.00, 10, 18, 80, 'X'),
(52, 53, '5K Calle', 60000.00, 500, 18, 89, 'X'),
(53, 54, '5K Calle', 6000.00, 600, 18, 60, 'X'),
(54, 55, '5K Calle', 5000.00, 500, 17, 60, 'X'),
(55, 56, '5K Calle', 10000.00, 500, 18, 90, 'X'),
(56, 57, '21K Calle', 5000.00, 10, 17, 85, 'X'),
(57, 58, '5K Calle (Fem)', 5000.00, 4000, 17, 70, 'F'),
(58, 59, '3K Calle', 400.00, 5000, 18, 80, 'X');

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
  `estado` enum('publicado','cancelado','finalizado','suspendido') DEFAULT 'publicado'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `eventos`
--

INSERT INTO `eventos` (`idEvento`, `nombre`, `descripcion`, `fechaHora`, `lugar`, `cupoTotal`, `idOrganizador`, `urlPronosticoClima`, `datosPago`, `estado`) VALUES
(1, 'Maratón San Luis 2025 - ACTUALIZADO', 'Carrera de 10K y 5K - Descripción actualizada', '2025-12-05 09:00:00', 'Plaza Pringles, San Luis Capital', 600, 5, 'https://www.weather.com/sanluisargentina', 'CBU: 0000003100012345678901 - Alias: RUNNERS.SL.2025', 'finalizado'),
(2, 'Maratón Independencia San Luis 2026', 'Carrera de 10K por las calles de San Luis', '2026-01-15 08:00:00', 'Av España - Av La Finur, San Luis', 1000, 5, 'https://www.weather.com/sanluisargentina', 'CBU: 0000003100012345678901 - Alias: RUNNERS.SL - Titular: Runners Club San Luis S.A', 'finalizado'),
(3, 'Potrero Corre', 'Gran premio FEST POTRERO', '2026-02-02 12:29:00', 'Potrero de los Funes', 40000, 9, NULL, 'Costo: $10000', 'finalizado'),
(5, 'Corre San Francisco', 'San Francisco con premio', '2026-03-12 12:00:00', 'San Francisco- San Luis', 5000, 9, NULL, 'trump.maduro', 'cancelado'),
(26, 'San valentin', 'San valentin', '2026-02-14 18:04:00', 'San Luis - Zona Norte', 5, 9, NULL, 'correSanLuis', 'publicado'),
(50, 'San Luis - La Punta', 'Gran evento San Luis - La Punta', '2026-03-19 19:45:00', 'San Luis - La Punta', 10, 9, NULL, 'teresita.mp', 'publicado'),
(51, 'Teresita', '1er Rotonda - Teresita', '2026-04-16 18:00:00', 'San Luis', 10, 9, NULL, 'teresita.mp', 'publicado'),
(53, 'Test PI', 'puntos de interes y direccion del circuito', '2026-03-31 18:30:00', 'San Luis', 500, 9, NULL, 'PuntosInteres', 'publicado'),
(54, 'Test PI2', 'Puntos interes y sentido evento', '2026-03-21 06:22:00', 'San Luis', 600, 9, NULL, 'sl.com', 'publicado'),
(55, 'Test PI3', 'puntos interes', '2026-03-20 05:27:00', 'san luis', 500, 9, NULL, 'alias. puntos', 'publicado'),
(56, 'Test 4', 'Ver los puntos de interes', '2026-02-24 06:20:00', 'San Luis', 500, 9, NULL, 'alisa.com', 'publicado'),
(57, 'Test 57', 'descriocion57', '2026-04-23 18:00:00', 'San Luis', 10, 9, NULL, 'descripon57', 'publicado'),
(58, 'Test 58', 'descripcion 58', '2026-03-31 18:11:00', 'San Luis', 4000, 9, NULL, 'test58', 'publicado'),
(59, 'CargaResultados', 'Cargando resultados', '2026-02-02 18:29:00', 'San Luis', 5000, 9, NULL, 'SanLuis.mp', 'finalizado');

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
(3, 4, 1, '2025-12-04 16:55:46', 'pagado', 'M', 1, '/uploads/comprobantes/comprobante_3_20251204165759.pdf'),
(4, 4, 23, '2026-01-17 14:41:32', 'cancelado', 'L', 1, '/uploads/comprobantes/comprobante_4_20260117144719.pdf'),
(5, 8, 23, '2026-01-19 01:48:43', 'cancelado', 'M', 1, '/uploads/comprobantes/comprobante_5_20260119015011.jpeg'),
(6, 2, 23, '2026-01-19 02:47:25', 'rechazado', 'M', 1, '/uploads/comprobantes/comprobante_6_20260119024909.png'),
(7, 2, 23, '2026-01-19 02:49:54', 'rechazado', 'M', 1, '/uploads/comprobantes/comprobante_7_20260119025044.pdf'),
(8, 2, 23, '2026-01-19 02:51:06', 'pagado', 'M', 1, '/uploads/comprobantes/comprobante_8_20260119025243.jpg'),
(10, 10, 23, '2026-01-28 01:14:18', 'rechazado', 'S', 1, '/uploads/comprobantes/comprobante_10_20260128011455.png'),
(11, 10, 23, '2026-01-29 04:00:28', 'cancelado', 'S', 1, '/uploads/comprobantes/comprobante_11_20260129043548.png'),
(12, 10, 23, '2026-01-30 19:02:22', 'rechazado', 'M', 1, '/uploads/comprobantes/comprobante_12_20260130190456.jpg'),
(13, 10, 23, '2026-01-30 19:34:29', 'rechazado', 'M', 1, '/uploads/comprobantes/comprobante_13_20260130193439.jpg'),
(14, 10, 23, '2026-01-30 19:35:29', 'cancelado', 'M', 1, '/uploads/comprobantes/comprobante_14_20260130193537.jpg'),
(15, 10, 23, '2026-01-30 19:51:25', 'cancelado', 'M', 1, '/uploads/comprobantes/comprobante_15_20260130211237.jpg'),
(16, 10, 23, '2026-02-02 00:30:23', 'rechazado', 'M', 1, '/uploads/comprobantes/comprobante_16_20260202003207.jpg'),
(17, 10, 23, '2026-02-02 00:33:24', 'cancelado', 'M', 1, '/uploads/comprobantes/comprobante_17_20260202003333.jpg'),
(18, 10, 47, '2026-02-02 00:36:08', 'cancelado', 'M', 1, NULL),
(19, 2, 58, '2026-02-03 10:16:50', 'pagado', 'M', 1, '/uploads/comprobantes/comprobante_19_20260203101735.jpg'),
(20, 4, 58, '2026-02-03 10:19:36', 'pagado', 'M', 1, '/uploads/comprobantes/comprobante_20_20260203101946.jpg'),
(21, 8, 58, '2026-02-03 10:20:19', 'pagado', 'M', 1, '/uploads/comprobantes/comprobante_21_20260203102026.jpg'),
(22, 10, 58, '2026-02-03 10:21:21', 'pagado', 'M', 1, '/uploads/comprobantes/comprobante_22_20260203102130.jpg');

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

--
-- Volcado de datos para la tabla `notificaciones_evento`
--

INSERT INTO `notificaciones_evento` (`idNotificacion`, `idEvento`, `titulo`, `mensaje`, `fechaEnvio`) VALUES
(1, 1, 'Retiro de Kits', 'Recuerden que el retiro de kits es hoy hasta las 18hs en el Centro Cultural.', '2025-12-07 20:08:00'),
(2, 1, 'Cambio de Horario', 'La largada se retrasa 30 minutos por clima.', '2025-12-07 20:56:48'),
(44, 26, 'Evento SUSPENDIDO', 'Reprgramado - fecha a confirmar', '2026-01-19 16:21:16'),
(45, 26, 'Evento PUBLICADO', 'Republicado', '2026-01-19 16:46:52'),
(46, 26, 'Evento SUSPENDIDO', 'Reprogramdao', '2026-01-19 18:20:28'),
(47, 26, 'Evento SUSPENDIDO', 'Re', '2026-01-19 18:22:27'),
(48, 26, 'Evento SUSPENDIDO', 'A', '2026-01-19 19:45:43'),
(50, 59, 'Evento FINALIZADO', 'Evento finalizado', '2026-02-03 10:23:19'),
(51, 59, 'URGENTE: Evento Cancelado', 'Adadads', '2026-02-03 19:46:25');

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
(2, 5, 'Club Deportivo La Punta', 'CLUB La Punta', '13231331112', 'Av. Costanera s/n, La Punta, San Luis'),
(3, 9, 'RUNNER ORGANIZACION S.A', 'RUNNER San Luis', '20331231234', 'Av Sarmiento 100');

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
(2, 2, 'Carlos', 'González Pérez', '1993-05-08 00:00:00', 'M', 11111331, 'San Luis Capital', 'Equipo Trail Running SL', 'Luzmila (pareja)', '266483133237', '2000-01-01 00:00:00'),
(3, 4, 'Test1 Runner Nombre', 'Test Runner Apellido', '2000-03-20 00:00:00', 'M', 22222222, 'Juana Koslay', 'Equipo Trail Running SL', 'Pareja', '2664888999', '2025-12-07 20:58:12'),
(6, 8, 'Esteban', 'Moreira', '1993-05-08 00:00:00', 'M', 37599292, 'San Luis Capital, San Luis', 'Sin agrupacion', 'La Rosalia (pareja)', '2665031234', NULL),
(7, 10, 'Beatriz', 'Rosales', '1993-08-21 00:00:00', 'F', 11222333, 'San Luis Capital, San Luis', 'Sin agrupacion', 'Enrique', '2664044026', NULL),
(8, 11, 'Beatriz', 'Zalazar', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

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
(33, 26, 'punto_energetico', 'Punto Energético', -33.2743201, -66.3059375),
(34, 26, 'primeros_auxilios', 'Primeros Auxilios', -33.2757379, -66.3051097),
(35, 26, 'punto_energetico', 'Punto Energético', -33.2770208, -66.3064391),
(36, 26, 'primeros_auxilios', 'Primeros Auxilios', -33.2769334, -66.3050390),
(37, 56, 'hidratacion', 'Puesto de Hidratación', -33.2776201, -66.3019930),
(38, 56, 'hidratacion', 'Puesto de Hidratación', -33.2772680, -66.3014291),
(39, 56, 'primeros_auxilios', 'Primeros Auxilios', -33.2757572, -66.3021094),
(40, 26, 'otro', 'Punto de Interés', -33.2772428, -66.3059600),
(41, 57, 'hidratacion', 'Puesto de Hidratación', -33.2577918, -66.2980120),
(42, 57, 'primeros_auxilios', 'Primeros Auxilios', -33.2523571, -66.2925929),
(43, 57, 'punto_energetico', 'Punto Energético', -33.2524252, -66.2925379),
(44, 57, 'otro', 'Punto de Interés', -33.2495911, -66.2902476),
(45, 57, 'hidratacion', 'Puesto de Hidratación', -33.2463727, -66.2900968),
(46, 57, 'hidratacion', 'Puesto de Hidratación', -33.2392251, -66.2897960),
(47, 57, 'primeros_auxilios', 'Primeros Auxilios', -33.2392262, -66.2898879),
(48, 57, 'otro', 'Punto de Interés', -33.2392386, -66.2900612),
(49, 59, 'hidratacion', 'Puesto de Hidratación', -33.2679044, -66.3283370),
(50, 59, 'punto_energetico', 'Punto Energético', -33.2662258, -66.3314792);

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
(2, 3, '00:46:03', 2, 1, '00:48:15', 10.52, '04:35 min/km', '13.1 km/h', 850, 160, 185),
(23, 21, '01:40:03', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(24, 19, '01:48:15', 2, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(25, 22, '01:52:30', 3, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(26, 20, '01:59:47', 4, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

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
(5, 2, 5, -33.3021500, -66.3368000),
(872, 50, 1, -33.2761530, -66.3085842),
(873, 50, 2, -33.2687389, -66.3044047),
(874, 50, 3, -33.2629304, -66.3010948),
(875, 50, 4, -33.2587845, -66.2987355),
(876, 50, 5, -33.2580200, -66.2982463),
(877, 50, 6, -33.2549792, -66.2952030),
(878, 50, 7, -33.2502691, -66.2903247),
(879, 50, 8, -33.2300268, -66.2894822),
(880, 50, 9, -33.2285581, -66.2902862),
(881, 50, 10, -33.2269594, -66.2912917),
(882, 50, 11, -33.2170864, -66.2942351),
(883, 50, 12, -33.2131891, -66.2943333),
(884, 50, 13, -33.2119883, -66.2941197),
(885, 50, 14, -33.2106225, -66.2943548),
(886, 50, 15, -33.2078339, -66.2944285),
(887, 50, 16, -33.2072720, -66.2943584),
(888, 50, 17, -33.2066357, -66.2941181),
(889, 50, 18, -33.2040796, -66.2925553),
(890, 50, 19, -33.2029016, -66.2913527),
(891, 50, 20, -33.2010921, -66.2892542),
(892, 50, 21, -33.1998546, -66.2883976),
(893, 50, 22, -33.1987551, -66.2871480),
(894, 50, 23, -33.1979068, -66.2862314),
(895, 50, 24, -33.1966844, -66.2854458),
(896, 50, 25, -33.1956530, -66.2848735),
(897, 50, 26, -33.1918275, -66.2844859),
(898, 50, 27, -33.1872838, -66.2833741),
(899, 50, 28, -33.1868205, -66.2831767),
(900, 50, 29, -33.1864872, -66.2832437),
(901, 50, 30, -33.1862072, -66.2834680),
(902, 50, 31, -33.1852933, -66.2837329),
(903, 50, 32, -33.1843137, -66.2843595),
(904, 50, 33, -33.1838583, -66.2845406),
(905, 50, 34, -33.1835670, -66.2847266),
(906, 50, 35, -33.1836403, -66.2849700),
(907, 50, 36, -33.1839054, -66.2852416),
(908, 50, 37, -33.1850267, -66.2888468),
(909, 50, 38, -33.1859069, -66.2918774),
(910, 50, 39, -33.1868334, -66.2947464),
(911, 50, 40, -33.1885327, -66.3001292),
(912, 50, 41, -33.1903174, -66.3059201),
(913, 50, 42, -33.1911457, -66.3092102),
(914, 50, 43, -33.1910427, -66.3094271),
(915, 50, 44, -33.1905784, -66.3096739),
(916, 50, 45, -33.1899280, -66.3099843),
(917, 50, 46, -33.1889794, -66.3103602),
(918, 50, 47, -33.1868149, -66.3112480),
(919, 50, 48, -33.1842607, -66.3123866),
(920, 50, 49, -33.1840996, -66.3121630),
(921, 50, 50, -33.1838530, -66.3120050),
(922, 50, 51, -33.1835628, -66.3119819),
(923, 50, 52, -33.1833041, -66.3121117),
(924, 50, 53, -33.1831456, -66.3123279),
(925, 50, 54, -33.1830507, -66.3126226),
(926, 50, 55, -33.1830670, -66.3129274),
(927, 50, 56, -33.1805020, -66.3140797),
(928, 50, 57, -33.1770931, -66.3160377),
(929, 50, 58, -33.1746603, -66.3174127),
(930, 50, 59, -33.1727591, -66.3181644),
(931, 50, 60, -33.1723690, -66.3181728),
(932, 50, 61, -33.1720536, -66.3181094),
(933, 50, 62, -33.1718347, -66.3181775),
(934, 50, 63, -33.1717628, -66.3183612),
(935, 50, 64, -33.1710632, -66.3188517),
(936, 50, 65, -33.1694874, -66.3195960),
(937, 50, 66, -33.1661796, -66.3210642),
(938, 50, 67, -33.1653618, -66.3214032),
(939, 50, 68, -33.1594535, -66.3227919),
(940, 50, 69, -33.1591405, -66.3227956),
(941, 50, 70, -33.1588784, -66.3225894),
(942, 50, 71, -33.1586614, -66.3220932),
(943, 50, 72, -33.1581598, -66.3194874),
(944, 50, 73, -33.1576442, -66.3170667),
(945, 50, 74, -33.1572162, -66.3145756),
(946, 50, 75, -33.1571196, -66.3140509),
(947, 50, 76, -33.1570388, -66.3137823),
(948, 50, 77, -33.1569310, -66.3133619),
(949, 50, 78, -33.1567227, -66.3129133),
(950, 50, 79, -33.1564940, -66.3125924),
(951, 50, 80, -33.1563859, -66.3124151),
(952, 50, 81, -33.1550672, -66.3098210),
(953, 50, 82, -33.1544794, -66.3100259),
(954, 50, 83, -33.1540963, -66.3104825),
(955, 50, 84, -33.1537819, -66.3107769),
(956, 50, 85, -33.1531144, -66.3116161),
(957, 50, 86, -33.1528003, -66.3118988),
(958, 50, 87, -33.1526019, -66.3121448),
(959, 50, 88, -33.1524618, -66.3123665),
(960, 50, 89, -33.1523181, -66.3125331),
(961, 50, 90, -33.1515069, -66.3127561),
(962, 50, 91, -33.1493190, -66.3132600),
(963, 50, 92, -33.1491750, -66.3131654),
(964, 50, 93, -33.1488112, -66.3125556),
(965, 50, 94, -33.1481541, -66.3116201),
(966, 50, 95, -33.1480707, -66.3114002),
(967, 50, 96, -33.1480238, -66.3111115),
(968, 50, 97, -33.1478905, -66.3108101),
(969, 50, 98, -33.1477636, -66.3102935),
(970, 50, 99, -33.1475949, -66.3090274),
(971, 50, 100, -33.1475211, -66.3089306),
(972, 50, 101, -33.1475309, -66.3087572),
(973, 50, 102, -33.1476000, -66.3086841),
(974, 50, 103, -33.1476547, -66.3084333),
(975, 50, 104, -33.1481125, -66.3060720),
(976, 50, 105, -33.1482172, -66.3056566),
(977, 50, 106, -33.1482995, -66.3053267),
(978, 50, 107, -33.1483422, -66.3049605),
(979, 50, 108, -33.1484354, -66.3048727),
(980, 50, 109, -33.1484241, -66.3047775),
(981, 50, 110, -33.1483607, -66.3046793),
(982, 50, 111, -33.1483742, -66.3045931),
(983, 50, 112, -33.1484575, -66.3045636),
(984, 50, 113, -33.1485252, -66.3046383),
(985, 50, 114, -33.1486192, -66.3047493),
(986, 50, 115, -33.1488598, -66.3049897),
(987, 50, 116, -33.1490976, -66.3052164),
(988, 50, 117, -33.1492755, -66.3053649),
(989, 50, 118, -33.1494178, -66.3054185),
(990, 50, 119, -33.1495767, -66.3053830),
(991, 50, 120, -33.1497224, -66.3052673),
(992, 50, 121, -33.1499748, -66.3050118),
(993, 50, 122, -33.1501951, -66.3048181),
(994, 51, 1, -33.2729479, -66.3067663),
(995, 51, 2, -33.2687025, -66.3044067),
(996, 51, 3, -33.2602416, -66.2995130),
(997, 51, 4, -33.2580918, -66.2983315),
(998, 51, 5, -33.2503058, -66.2903244),
(999, 51, 6, -33.2465275, -66.2900860),
(1000, 51, 7, -33.2462808, -66.2900683),
(1001, 51, 8, -33.2462567, -66.2902530),
(1002, 51, 9, -33.2464942, -66.2902711),
(1003, 51, 10, -33.2469980, -66.2902530),
(1004, 51, 11, -33.2497302, -66.2903831),
(1005, 51, 12, -33.2501090, -66.2904545),
(1006, 51, 13, -33.2505567, -66.2907509),
(1007, 51, 14, -33.2534761, -66.2938368),
(1008, 51, 15, -33.2556313, -66.2960871),
(1009, 51, 16, -33.2576070, -66.2980998),
(1010, 51, 17, -33.2578885, -66.2983117),
(1011, 51, 18, -33.2589247, -66.2989792),
(1012, 51, 19, -33.2626486, -66.3010532),
(1013, 51, 20, -33.2663430, -66.3031953),
(1014, 51, 21, -33.2719879, -66.3063379),
(1015, 51, 22, -33.2728448, -66.3068505),
(1057, 54, 1, -33.2762632, -66.3032178),
(1058, 54, 2, -33.2781563, -66.3028111),
(1059, 54, 3, -33.2772540, -66.3014438),
(1060, 54, 4, -33.2761059, -66.3015092),
(1061, 54, 5, -33.2754688, -66.3027434),
(1062, 54, 6, -33.2758601, -66.3030585),
(1077, 55, 1, -33.3158192, -66.3283736),
(1078, 55, 2, -33.3170474, -66.3278287),
(1079, 55, 3, -33.3184797, -66.3271481),
(1080, 55, 4, -33.3194602, -66.3267347),
(1081, 55, 5, -33.3202755, -66.3263793),
(1082, 55, 6, -33.3211844, -66.3259911),
(1083, 55, 7, -33.3221299, -66.3255442),
(1084, 55, 8, -33.3229950, -66.3252310),
(1085, 55, 9, -33.3241036, -66.3247978),
(1086, 55, 10, -33.3252911, -66.3242688),
(1087, 55, 11, -33.3260105, -66.3239315),
(1088, 55, 12, -33.3259934, -66.3219181),
(1089, 55, 13, -33.3260576, -66.3214377),
(1090, 55, 14, -33.3260144, -66.3203350),
(1118, 26, 1, -33.2762006, -66.3083136),
(1119, 26, 2, -33.2758396, -66.3081872),
(1120, 26, 3, -33.2751167, -66.3077527),
(1121, 26, 4, -33.2736241, -66.3067114),
(1122, 26, 5, -33.2753112, -66.3048103),
(1123, 26, 6, -33.2761821, -66.3054034),
(1124, 26, 7, -33.2764594, -66.3047916),
(1125, 26, 8, -33.2774825, -66.3054735),
(1126, 26, 9, -33.2766116, -66.3073745),
(1137, 56, 1, -33.2762351, -66.3032282),
(1138, 56, 2, -33.2781257, -66.3028131),
(1139, 56, 3, -33.2772708, -66.3014636),
(1140, 56, 4, -33.2760639, -66.3015122),
(1141, 56, 5, -33.2758211, -66.3020192),
(1142, 56, 6, -33.2748409, -66.3013456),
(1143, 56, 7, -33.2747708, -66.3014807),
(1144, 56, 8, -33.2757076, -66.3021747),
(1145, 56, 9, -33.2754814, -66.3027266),
(1146, 56, 10, -33.2757984, -66.3029925),
(1163, 57, 1, -33.2762836, -66.3086801),
(1164, 57, 2, -33.2732299, -66.3069323),
(1165, 57, 3, -33.2731304, -66.3067855),
(1166, 57, 4, -33.2729488, -66.3067925),
(1167, 57, 5, -33.2709409, -66.3056589),
(1168, 57, 6, -33.2658995, -66.3028175),
(1169, 57, 7, -33.2615343, -66.3002549),
(1170, 57, 8, -33.2587753, -66.2988203),
(1171, 57, 9, -33.2569899, -66.2973079),
(1172, 57, 10, -33.2551420, -66.2955480),
(1173, 57, 11, -33.2534693, -66.2937442),
(1174, 57, 12, -33.2514568, -66.2917138),
(1175, 57, 13, -33.2502099, -66.2904102),
(1176, 57, 14, -33.2470342, -66.2902862),
(1177, 57, 15, -33.2441287, -66.2900247),
(1178, 57, 16, -33.2392237, -66.2899905),
(1179, 58, 1, -33.3369488, -66.3190703),
(1180, 58, 2, -33.3331709, -66.3072924),
(1181, 58, 3, -33.3328003, -66.3059805),
(1182, 58, 4, -33.3325785, -66.3055416),
(1183, 58, 5, -33.3322379, -66.3049374),
(1184, 58, 6, -33.3318922, -66.3043470),
(1185, 58, 7, -33.3316149, -66.3039671),
(1186, 58, 8, -33.3312697, -66.3035333),
(1187, 58, 9, -33.3309131, -66.3031789),
(1286, 59, 1, -33.2622180, -66.3397639),
(1287, 59, 2, -33.2627989, -66.3384315),
(1288, 59, 3, -33.2632567, -66.3372215),
(1289, 59, 4, -33.2639609, -66.3359136),
(1290, 59, 5, -33.2643983, -66.3348836),
(1291, 59, 6, -33.2647742, -66.3341349),
(1292, 59, 7, -33.2652314, -66.3330768),
(1293, 59, 8, -33.2657470, -66.3320043),
(1294, 59, 9, -33.2659710, -66.3314380),
(1295, 59, 10, -33.2664725, -66.3314440),
(1296, 59, 11, -33.2668167, -66.3306715),
(1297, 59, 12, -33.2670746, -66.3300664),
(1298, 59, 13, -33.2674150, -66.3293834),
(1299, 59, 14, -33.2677996, -66.3285613),
(1300, 59, 15, -33.2684362, -66.3271863),
(1301, 59, 16, -33.2687628, -66.3264343),
(1302, 59, 17, -33.2691715, -66.3256689),
(1303, 59, 18, -33.2694005, -66.3250476),
(1304, 59, 19, -33.2695956, -66.3246677),
(1305, 59, 20, -33.2699208, -66.3240103),
(1306, 59, 21, -33.2701992, -66.3233665),
(1307, 59, 22, -33.2704756, -66.3227640),
(1308, 59, 23, -33.2707421, -66.3221797),
(1309, 59, 24, -33.2711335, -66.3213371),
(1310, 59, 25, -33.2714547, -66.3206853),
(1311, 59, 26, -33.2717919, -66.3199481),
(1312, 59, 27, -33.2721491, -66.3191622),
(1313, 59, 28, -33.2725488, -66.3183793),
(1314, 59, 29, -33.2726489, -66.3181631),
(1315, 59, 30, -33.2728358, -66.3179512),
(1316, 59, 31, -33.2729034, -66.3179076),
(1317, 59, 32, -33.2730393, -66.3179046),
(1318, 59, 33, -33.2731265, -66.3177993),
(1319, 59, 34, -33.2731478, -66.3176585),
(1320, 59, 35, -33.2730845, -66.3175059),
(1321, 59, 36, -33.2729059, -66.3174335),
(1322, 59, 37, -33.2727453, -66.3175887),
(1323, 59, 38, -33.2725796, -66.3177269),
(1324, 59, 39, -33.2723257, -66.3177647),
(1325, 59, 40, -33.2721437, -66.3176705),
(1326, 59, 41, -33.2719596, -66.3175425),
(1327, 59, 42, -33.2717418, -66.3177061),
(1328, 59, 43, -33.2717689, -66.3179582),
(1329, 59, 44, -33.2717922, -66.3181962),
(1330, 59, 45, -33.2714628, -66.3189157),
(1331, 59, 46, -33.2711626, -66.3195283),
(1332, 59, 47, -33.2706185, -66.3206857),
(1333, 59, 48, -33.2703539, -66.3212694),
(1334, 59, 49, -33.2702123, -66.3215721);

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
(4, 4, '566866dc43ef43d59222003acddd2c31', 'reactivacion', '2025-11-27 09:51:16', '2025-11-27 10:51:16', 1),
(5, 4, '6403356e0c9e4bbba82acf826e137dba', 'recuperacion', '2026-02-01 22:17:49', '2026-02-01 23:17:49', 1),
(6, 4, 'f7684de42cfb4850b68948d9d33012e1', 'recuperacion', '2026-02-01 22:19:26', '2026-02-01 23:19:26', 0),
(7, 4, '70d3fce2def54d3db842abcfd97a68ba', 'reactivacion', '2026-02-01 22:22:01', '2026-02-01 23:22:01', 1);

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
(2, 'Carlos', 'carlos@test.com', '2664222333', '$2a$12$1VrSrzRZp5VM7/4fNJ/f2.DPVbPE9b4.MF9lfXAGG7lp7Amncusmq', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(3, 'Juan Carlos', 'eventos@runnersclub.com', '2664111113', '$2a$12$4OatEBqyW4e6SJmeBhk8R.mT4StPBw5WtNHpuTBGldfeQ7NdKebpy', 'organizador', 1, '/uploads/avatars/defaults/default_organization.png'),
(4, 'Test1 Runner Nombre', 'esteban.dev22@gmail.com', '2664222222', '$2a$12$HIqX.CjZWvIR/iBibNuj5eTbHVPMOf9Dwe1XOelVGu.NuD1QGXRxK', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(5, 'La Punta RUNNER', 'test@orgaclub.com', '2664555888', '$2a$12$z0./mOCu5roRcP71s16Mh.C/XX98/V0jssxBrxBte.2sS1UZ874/m', 'organizador', 1, '/uploads/avatars/5_20251228015156.jpg'),
(8, 'Esteban', 'este@test.com', '2665044026', '$2a$12$xNmXFNF0xSwK3/kgNx3MsuExSmTlV2mdZHtItnTE9xkx.dmPbPcOO', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(9, 'RUNNer San Luis', 'run@sl.com', '2664123456', '$2a$12$kZfACtQSfY.eimN6SyLAM.kF3U88lcnJ2ndVrCHVqxZLNbkYO8wrC', 'organizador', 1, '/uploads/avatars/9_20251231015301.jpg'),
(10, 'Yanina', 'yani@test.com', '2664010203', '$2a$12$5Zrx1Fh/uIjShuTBBvciTumdrXFL/.EZcXc/yzTc/DZuDwzm8veEm', 'runner', 1, '/uploads/avatars/10_20260119020531.jpg'),
(11, 'Beatriz', 'beatriz@test.com', NULL, '$2a$12$FypSs4mmEk7IIykmgGkEv.Nu/55BbPszrl1k5PPM/ZrpaZ.ZzpGZ6', 'runner', 1, '/uploads/avatars/defaults/default_runner.png');

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
  MODIFY `idCategoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=59;

--
-- AUTO_INCREMENT de la tabla `eventos`
--
ALTER TABLE `eventos`
  MODIFY `idEvento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=60;

--
-- AUTO_INCREMENT de la tabla `inscripciones`
--
ALTER TABLE `inscripciones`
  MODIFY `idInscripcion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT de la tabla `notificaciones_evento`
--
ALTER TABLE `notificaciones_evento`
  MODIFY `idNotificacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=52;

--
-- AUTO_INCREMENT de la tabla `perfiles_organizadores`
--
ALTER TABLE `perfiles_organizadores`
  MODIFY `idPerfilOrganizador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `perfiles_runners`
--
ALTER TABLE `perfiles_runners`
  MODIFY `idPerfilRunner` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `puntosinteres`
--
ALTER TABLE `puntosinteres`
  MODIFY `idPuntoInteres` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=51;

--
-- AUTO_INCREMENT de la tabla `resultados`
--
ALTER TABLE `resultados`
  MODIFY `idResultado` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT de la tabla `rutas`
--
ALTER TABLE `rutas`
  MODIFY `idRuta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1335;

--
-- AUTO_INCREMENT de la tabla `tokens_recuperacion`
--
ALTER TABLE `tokens_recuperacion`
  MODIFY `idToken` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `idUsuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

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

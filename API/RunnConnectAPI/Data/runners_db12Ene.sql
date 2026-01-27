-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 13-01-2026 a las 03:19:51
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
(1, 1, '10K Competitiva', 5000.00, 200, 18, 90, 'X'),
(2, 4, '20K Competitiva', 60000.00, 4000, 18, 99, 'X'),
(3, 5, '5K Calle', 50000.00, 5000, 18, 85, 'X'),
(4, 6, '10K Calle', 40000.00, 4000, 17, 70, 'X'),
(5, 7, '10K Calle', 40000.00, 4000, 17, 70, 'X'),
(6, 8, '10K Calle', 6000.00, 4000, 16, 78, 'X'),
(7, 9, '10K Calle', 40000.00, 6000, 18, 99, 'X'),
(8, 10, '5K Calle', 5000.00, 1000, 15, 90, 'X'),
(9, 11, '5K Calle', 1000.00, 1000, 18, 60, 'X'),
(11, 13, '21K Calle', 50000.00, 5000, 18, 67, 'X'),
(12, 14, '10K Calle', 12345.00, 1234, 17, 90, 'X'),
(13, 15, '3K Calle', 1.00, 5, 15, 36, 'X'),
(14, 16, '10K Calle', 5000.00, 4000, 17, 70, 'X');

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
(2, 'Maratón Independencia San Luis 2026', 'Carrera de 10K por las calles de San Luis', '2026-01-15 08:00:00', 'Av España - Av La Finur, San Luis', 1000, 5, 'https://www.weather.com/sanluisargentina', 'CBU: 0000003100012345678901 - Alias: RUNNERS.SL - Titular: Runners Club San Luis S.A', 'publicado'),
(3, 'Potrero Corre', 'Gran premio FEST POTRERO', '2026-01-14 12:29:00', 'Potrero de los Funes', 40000, 9, NULL, 'Costo: $10000', 'cancelado'),
(4, 'TEST1', 'test1', '2026-01-21 12:54:00', 'Potrero', 4000, 9, NULL, 'potrero.mp', 'publicado'),
(5, 'Corre San Francisco', 'San Francisco con premio', '2026-03-12 12:00:00', 'San Francisco- San Luis', 5000, 9, NULL, 'trump.maduro', 'cancelado'),
(6, 'Nuevo Test', 'Test para El dibujado', '2026-01-28 00:30:00', 'San Luis', 4000, 9, NULL, 'SanLuis.RUN', 'publicado'),
(7, 'Nuevo Test', 'Test para El dibujado', '2026-01-28 00:30:00', 'San Luis', 4000, 9, NULL, 'SanLuis.RUN', 'publicado'),
(8, 'Test para dibujo', 'dibujo', '2026-01-28 16:25:00', 'SanLuis', 4000, 9, NULL, 'asdad.pm', 'publicado'),
(9, 'Test1 plaza', 'plaza 1 de', '2026-01-24 15:49:00', 'plaza pringles', 6000, 9, NULL, '', 'publicado'),
(10, 'aasdadasda', 'adasdasdasd', '2026-01-20 17:03:00', 'aaaaaaaaaaaaa', 1000, 9, NULL, 'ddasdads', 'publicado'),
(11, 'aaaaaaaaaaa', 'aaaaaaaaaa', '2026-01-20 17:06:00', 'ddddd', 1000, 9, NULL, 'aaaaaaa', 'publicado'),
(13, 'test111', 'desc111', '2026-01-20 05:43:00', 'ubi111', 5000, 9, NULL, 'alias111', 'publicado'),
(14, 'Evento 11', 'evento 11 descr', '2026-01-29 06:20:00', 'ubi del evento 11', 1234, 9, NULL, 'evento11.mp', 'publicado'),
(15, 'Evento 12', 'Desc evento 12', '2026-01-31 04:40:00', 'ubi del evento 12', 5, 9, NULL, 'alias.evento12', 'cancelado'),
(16, 'Evento test 16', 'Descricp 16', '2026-02-26 18:00:00', 'ubicacion 16', 4000, 9, NULL, 'alias16.mp', 'cancelado');

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

--
-- Volcado de datos para la tabla `notificaciones_evento`
--

INSERT INTO `notificaciones_evento` (`idNotificacion`, `idEvento`, `titulo`, `mensaje`, `fechaEnvio`) VALUES
(1, 1, 'Retiro de Kits', 'Recuerden que el retiro de kits es hoy hasta las 18hs en el Centro Cultural.', '2025-12-07 20:08:00'),
(2, 1, 'Cambio de Horario', 'La largada se retrasa 30 minutos por clima.', '2025-12-07 20:56:48'),
(3, 16, 'URGENTE: Evento Cancelado', 'Mal tiempo evento 16', '2026-01-12 12:04:50'),
(4, 15, 'URGENTE: Evento Cancelado', 'Reprogramado El evento 12', '2026-01-12 21:19:16'),
(5, 16, 'URGENTE: Evento Cancelado', 'Reprogramado por mal tiempo', '2026-01-12 21:35:13'),
(6, 16, 'URGENTE: Evento Cancelado', 'Cancelado1', '2026-01-12 21:37:41');

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
(3, 9, 'RUNNER ORGANIZACION', 'RUNN San Luis', '20331231234', 'Av Sarmiento 100');

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
(2, 2, 'Carlos', 'González Pérez', '1993-05-08 00:00:00', 'M', 11111331, 'San Luis Capital', 'Equipo Trail Running SL', 'Yanina (pareja)', '266483133237', '2000-01-01 00:00:00'),
(3, 4, 'Test1 Runner Nombre', 'Test Runner Apellido', '2000-03-20 00:00:00', 'M', 22222222, 'Juana Koslay', 'Equipo Trail Running SL', 'Pareja', '2664888999', '2025-12-07 20:58:12'),
(6, 8, 'Esteban', 'Moreira', '1993-05-08 00:00:00', 'M', 37599292, 'San Luis Capital, San Luis', 'Sin agrupacion', 'Yanina (pareja)', '2665031234', NULL);

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
(5, 2, 5, -33.3021500, -66.3368000),
(6, 11, 1, -33.2880824, -66.3370642),
(7, 11, 2, -33.2893627, -66.3369238),
(8, 11, 3, -33.2904826, -66.3367615),
(9, 11, 4, -33.2910924, -66.3366773),
(10, 11, 5, -33.2907981, -66.3356524),
(11, 11, 6, -33.2906908, -66.3351294),
(12, 11, 7, -33.2896894, -66.3352048),
(13, 11, 8, -33.2879933, -66.3353751),
(14, 11, 9, -33.2879860, -66.3364752),
(15, 11, 10, -33.2880401, -66.3370029),
(349, 13, 1, -33.2360142, -66.2381494),
(350, 13, 2, -33.2359806, -66.2368314),
(351, 13, 3, -33.2363070, -66.2356751),
(352, 13, 4, -33.2367436, -66.2347993),
(353, 13, 5, -33.2367268, -66.2338431),
(354, 13, 6, -33.2361197, -66.2332376),
(355, 13, 7, -33.2351401, -66.2324614),
(356, 13, 8, -33.2334070, -66.2302791),
(357, 13, 9, -33.2330635, -66.2292049),
(358, 13, 10, -33.2331221, -66.2284941),
(359, 13, 11, -33.2333145, -66.2278936),
(360, 13, 12, -33.2334020, -66.2276127),
(361, 13, 13, -33.2334477, -66.2274350),
(362, 13, 14, -33.2334830, -66.2272469),
(363, 13, 15, -33.2334976, -66.2269726),
(364, 13, 16, -33.2334976, -66.2267208),
(365, 13, 17, -33.2334314, -66.2263413),
(366, 13, 18, -33.2332682, -66.2260657),
(367, 13, 19, -33.2325326, -66.2259591),
(368, 13, 20, -33.2305153, -66.2258213),
(369, 13, 21, -33.2294008, -66.2257492),
(370, 13, 22, -33.2284428, -66.2258076),
(371, 13, 23, -33.2280028, -66.2258585),
(372, 13, 24, -33.2276154, -66.2260087),
(373, 13, 25, -33.2273058, -66.2262028),
(374, 13, 26, -33.2269238, -66.2265730),
(375, 13, 27, -33.2266725, -66.2269113),
(376, 13, 28, -33.2264162, -66.2272603),
(377, 13, 29, -33.2262129, -66.2274863),
(378, 13, 30, -33.2259439, -66.2277444),
(379, 13, 31, -33.2254040, -66.2280318),
(380, 13, 32, -33.2249617, -66.2281297),
(381, 13, 33, -33.2241141, -66.2279573),
(382, 13, 34, -33.2227760, -66.2275976),
(383, 13, 35, -33.2215770, -66.2273089),
(384, 13, 36, -33.2209986, -66.2272938),
(385, 13, 37, -33.2203827, -66.2272878),
(386, 13, 38, -33.2200540, -66.2273314),
(387, 13, 39, -33.2198391, -66.2276030),
(388, 13, 40, -33.2197897, -66.2278548),
(389, 13, 41, -33.2197019, -66.2294503),
(390, 13, 42, -33.2195460, -66.2322710),
(391, 13, 43, -33.2194966, -66.2333315),
(392, 13, 44, -33.2195194, -66.2339709),
(393, 13, 45, -33.2195726, -66.2343048),
(394, 13, 46, -33.2196310, -66.2345187),
(395, 13, 47, -33.2197903, -66.2350830),
(396, 13, 48, -33.2200649, -66.2359634),
(397, 13, 49, -33.2201732, -66.2362232),
(398, 13, 50, -33.2202938, -66.2363527),
(399, 13, 51, -33.2204334, -66.2364117),
(400, 13, 52, -33.2205681, -66.2364331),
(401, 13, 53, -33.2207484, -66.2364009),
(402, 13, 54, -33.2208704, -66.2363557),
(403, 13, 55, -33.2210814, -66.2363175),
(404, 13, 56, -33.2213290, -66.2362903),
(405, 13, 57, -33.2215677, -66.2363282),
(406, 13, 58, -33.2219194, -66.2364952),
(407, 13, 59, -33.2222064, -66.2367107),
(408, 13, 60, -33.2223637, -66.2369035),
(409, 13, 61, -33.2228052, -66.2373756),
(410, 13, 62, -33.2230882, -66.2376488),
(411, 13, 63, -33.2236163, -66.2381005),
(412, 13, 64, -33.2240746, -66.2384498),
(413, 13, 65, -33.2244294, -66.2386912),
(414, 13, 66, -33.2247390, -66.2388310),
(415, 13, 67, -33.2250946, -66.2389601),
(416, 13, 68, -33.2253619, -66.2390801),
(417, 13, 69, -33.2258382, -66.2392746),
(418, 13, 70, -33.2273602, -66.2399096),
(419, 13, 71, -33.2279231, -66.2402375),
(420, 13, 72, -33.2281388, -66.2406777),
(421, 13, 73, -33.2284282, -66.2412695),
(422, 13, 74, -33.2286655, -66.2417157),
(423, 13, 75, -33.2287757, -66.2417613),
(424, 13, 76, -33.2289914, -66.2417992),
(425, 13, 77, -33.2291358, -66.2417439),
(426, 13, 78, -33.2295004, -66.2415769),
(427, 13, 79, -33.2298268, -66.2414690),
(428, 13, 80, -33.2313713, -66.2408598),
(429, 13, 81, -33.2318318, -66.2406720),
(430, 13, 82, -33.2319981, -66.2405644),
(431, 13, 83, -33.2321593, -66.2404400),
(432, 13, 84, -33.2322813, -66.2403062),
(433, 13, 85, -33.2324633, -66.2401238),
(434, 13, 86, -33.2325396, -66.2399354),
(435, 13, 87, -33.2326335, -66.2396669),
(436, 13, 88, -33.2326692, -66.2394513),
(437, 13, 89, -33.2326877, -66.2391381),
(438, 13, 90, -33.2326916, -66.2389333),
(439, 13, 91, -33.2329036, -66.2385219),
(440, 13, 92, -33.2333627, -66.2377283),
(441, 13, 93, -33.2338652, -66.2370618),
(442, 13, 94, -33.2339819, -66.2370135),
(443, 13, 95, -33.2340859, -66.2369920),
(444, 13, 96, -33.2341978, -66.2370299),
(445, 13, 97, -33.2343019, -66.2370940),
(446, 13, 98, -33.2344542, -66.2373682),
(447, 13, 99, -33.2345341, -66.2376643),
(448, 13, 100, -33.2347321, -66.2385021),
(449, 13, 101, -33.2349085, -66.2393313),
(450, 13, 102, -33.2350885, -66.2394191),
(451, 13, 103, -33.2353143, -66.2393188),
(452, 13, 104, -33.2353729, -66.2390607),
(453, 13, 105, -33.2353375, -66.2386081),
(454, 13, 106, -33.2353426, -66.2382969),
(455, 13, 107, -33.2355226, -66.2381330),
(456, 13, 108, -33.2357080, -66.2381719),
(457, 13, 109, -33.2358586, -66.2382396),
(458, 13, 110, -33.2359461, -66.2382419),
(459, 13, 111, -33.2359904, -66.2382342),
(460, 15, 1, -33.2853809, -66.3013285),
(461, 15, 2, -33.2872579, -66.3009490),
(462, 15, 3, -33.2873257, -66.3007106),
(463, 15, 4, -33.2870278, -66.2996364),
(464, 15, 5, -33.2865513, -66.2988169),
(465, 15, 6, -33.2862315, -66.2986721),
(466, 15, 7, -33.2860429, -66.2985514),
(467, 15, 8, -33.2860132, -66.2984575),
(468, 15, 9, -33.2855407, -66.2980978),
(469, 15, 10, -33.2853677, -66.2978899),
(470, 15, 11, -33.2852797, -66.2978068),
(471, 15, 12, -33.2851334, -66.2977739),
(472, 15, 13, -33.2849367, -66.2976646),
(473, 15, 14, -33.2843377, -66.2975178),
(474, 15, 15, -33.2836861, -66.2973149),
(475, 15, 16, -33.2835070, -66.2973853),
(476, 15, 17, -33.2832962, -66.2973015),
(477, 15, 18, -33.2831409, -66.2973833),
(478, 15, 19, -33.2832578, -66.2978913),
(479, 15, 20, -33.2834874, -66.2984240),
(480, 15, 21, -33.2838985, -66.2993514),
(481, 15, 22, -33.2840527, -66.2996769),
(482, 15, 23, -33.2852999, -66.3012031),
(483, 15, 24, -33.2853476, -66.3012990),
(484, 16, 1, -33.2982271, -66.3157947),
(485, 16, 2, -33.2996854, -66.3155730),
(486, 16, 3, -33.2996398, -66.3152924),
(487, 16, 4, -33.2993621, -66.3143536),
(488, 16, 5, -33.2992744, -66.3140811),
(489, 16, 6, -33.2978384, -66.3143285),
(490, 16, 7, -33.2981781, -66.3156552),
(491, 16, 8, -33.2981994, -66.3157219),
(492, 16, 9, -33.2982339, -66.3158232);

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
(2, 'Carlos', 'carlos@test.com', '2664222333', '$2a$12$1VrSrzRZp5VM7/4fNJ/f2.DPVbPE9b4.MF9lfXAGG7lp7Amncusmq', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(3, 'Juan Carlos', 'eventos@runnersclub.com', '2664111113', '$2a$12$4OatEBqyW4e6SJmeBhk8R.mT4StPBw5WtNHpuTBGldfeQ7NdKebpy', 'organizador', 1, '/uploads/avatars/defaults/default_organization.png'),
(4, 'Test1 Runner Nombre', 'esteban.dev22@gmail.com', '2664222222', '$2a$12$2ViVQFRklBukbJzIXLHbv.dNsYJ.vpPZedPPBdKHLJLEqVdMNgrg6', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(5, 'La Punta RUNNER', 'test@orgaclub.com', '2664555888', '$2a$12$z0./mOCu5roRcP71s16Mh.C/XX98/V0jssxBrxBte.2sS1UZ874/m', 'organizador', 1, '/uploads/avatars/5_20251228015156.jpg'),
(8, 'Esteban', 'este@test.com', '2665044026', '$2a$12$xNmXFNF0xSwK3/kgNx3MsuExSmTlV2mdZHtItnTE9xkx.dmPbPcOO', 'runner', 1, '/uploads/avatars/defaults/default_runner.png'),
(9, 'RUNN San Luis', 'run@sl.com', '2664123456', '$2a$12$kZfACtQSfY.eimN6SyLAM.kF3U88lcnJ2ndVrCHVqxZLNbkYO8wrC', 'organizador', 1, '/uploads/avatars/9_20251231015301.jpg');

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
  MODIFY `idCategoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `eventos`
--
ALTER TABLE `eventos`
  MODIFY `idEvento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT de la tabla `inscripciones`
--
ALTER TABLE `inscripciones`
  MODIFY `idInscripcion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `notificaciones_evento`
--
ALTER TABLE `notificaciones_evento`
  MODIFY `idNotificacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `perfiles_organizadores`
--
ALTER TABLE `perfiles_organizadores`
  MODIFY `idPerfilOrganizador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `perfiles_runners`
--
ALTER TABLE `perfiles_runners`
  MODIFY `idPerfilRunner` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

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
  MODIFY `idRuta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=493;

--
-- AUTO_INCREMENT de la tabla `tokens_recuperacion`
--
ALTER TABLE `tokens_recuperacion`
  MODIFY `idToken` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `idUsuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

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

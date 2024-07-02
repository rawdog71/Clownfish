USE clownfish;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_maven`
--

CREATE TABLE IF NOT EXISTS  `cf_npm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `npm_id` varchar(255) DEFAULT NULL,
  `npm_latestversion` varchar(64) DEFAULT NULL,
  `npm_filename` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `npm_id_UNIQUE` (`npm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

COMMIT;
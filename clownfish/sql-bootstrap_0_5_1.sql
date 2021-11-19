USE clownfish;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_sourcecode`
--

CREATE TABLE IF NOT EXISTS `cf_sourcecode` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `content` mediumtext,
  `checkedoutby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_javascriptversion`
--

CREATE TABLE IF NOT EXISTS `cf_sourcecodeversion` (
  `sourcecoderef` bigint(20) unsigned NOT NULL,
  `version` bigint(20) NOT NULL,
  `content` blob,
  `tstamp` datetime NOT NULL,
  `commitedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`sourcecoderef`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

COMMIT;
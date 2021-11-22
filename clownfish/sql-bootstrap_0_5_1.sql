USE clownfish;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_javascript`
--

CREATE TABLE IF NOT EXISTS `cf_java` (
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

CREATE TABLE IF NOT EXISTS `cf_javaversion` (
  `javaref` bigint(20) unsigned NOT NULL,
  `version` bigint(20) NOT NULL,
  `content` blob,
  `tstamp` datetime NOT NULL,
  `commitedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`javaref`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_maven`
--

CREATE TABLE IF NOT EXISTS  `cf_maven` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `maven_id` varchar(255) DEFAULT NULL,
  `maven_group` varchar(255) DEFAULT NULL,
  `maven_artifact` varchar(255) DEFAULT NULL,
  `maven_latestversion` varchar(64) DEFAULT NULL,
  `maven_package` varchar(16) DEFAULT NULL,
  `maven_filename` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `maven_id_UNIQUE` (`maven_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('folder_maven', '', 1);

COMMIT;
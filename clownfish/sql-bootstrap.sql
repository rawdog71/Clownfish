-- phpMyAdmin SQL Dump
-- version 3.4.9
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 18. Mrz 2019 um 10:01
-- Server Version: 5.7.18
-- PHP-Version: 5.3.8

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Datenbank: `clownfish`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_asset`
--

CREATE TABLE IF NOT EXISTS `cf_asset` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `fileextension` varchar(255) DEFAULT NULL,
  `mimetype` varchar(255) DEFAULT NULL,
  `imagewidth` varchar(255) DEFAULT NULL,
  `imageheight` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_assetkeyword`
--

CREATE TABLE IF NOT EXISTS `cf_assetkeyword` (
  `assetref` bigint(20) unsigned NOT NULL,
  `keywordref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`assetref`,`keywordref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_attribut`
--

CREATE TABLE IF NOT EXISTS `cf_attribut` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `attributetype` bigint(20) unsigned NOT NULL,
  `classref` bigint(20) unsigned NOT NULL,
  `identity` tinyint(1) DEFAULT '0',
  `autoincrementor` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_attributcontent`
--

CREATE TABLE IF NOT EXISTS `cf_attributcontent` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `attributref` bigint(20) unsigned NOT NULL,
  `classcontentref` bigint(20) unsigned NOT NULL,
  `content_boolean` tinyint(1) DEFAULT NULL,
  `content_integer` bigint(20) DEFAULT NULL,
  `content_real` double DEFAULT NULL,
  `content_string` varchar(256) DEFAULT NULL,
  `content_text` longtext,
  `content_date` datetime DEFAULT NULL,
  `salt` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_attributetype`
--

CREATE TABLE IF NOT EXISTS `cf_attributetype` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_class`
--

CREATE TABLE IF NOT EXISTS `cf_class` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_classcontent`
--

CREATE TABLE IF NOT EXISTS `cf_classcontent` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `classref` bigint(20) unsigned NOT NULL,
  `name` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_classcontentkeyword`
--

CREATE TABLE IF NOT EXISTS `cf_classcontentkeyword` (
  `classcontentref` bigint(20) unsigned NOT NULL,
  `keywordref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`classcontentref`,`keywordref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_datasource`
--

CREATE TABLE IF NOT EXISTS `cf_datasource` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `server` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `port` int(10) unsigned NOT NULL,
  `databasename` varchar(255) NOT NULL,
  `user` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `driverclass` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_javascript`
--

CREATE TABLE IF NOT EXISTS `cf_javascript` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `content` text,
  `checkedoutby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_javascriptversion`
--

CREATE TABLE IF NOT EXISTS `cf_javascriptversion` (
  `javascriptref` bigint(20) unsigned NOT NULL,
  `version` bigint(20) NOT NULL,
  `content` varbinary(64000) DEFAULT NULL,
  `tstamp` datetime NOT NULL,
  `commitedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`javascriptref`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_keyword`
--

CREATE TABLE IF NOT EXISTS `cf_keyword` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_list`
--

CREATE TABLE IF NOT EXISTS `cf_list` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `classref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_listcontent`
--

CREATE TABLE IF NOT EXISTS `cf_listcontent` (
  `listref` bigint(20) unsigned NOT NULL,
  `classcontentref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`listref`,`classcontentref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_property`
--

CREATE TABLE IF NOT EXISTS `cf_property` (
  `hashkey` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  `nodelete` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY (`hashkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_relation`
--

CREATE TABLE IF NOT EXISTS `cf_relation` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `typ` int(11) NOT NULL,
  `ref1` bigint(20) NOT NULL,
  `ref2` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_site`
--

CREATE TABLE IF NOT EXISTS `cf_site` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `templateref` bigint(20) unsigned DEFAULT NULL,
  `parentref` bigint(20) unsigned DEFAULT NULL,
  `stylesheetref` bigint(20) unsigned DEFAULT NULL,
  `javascriptref` bigint(20) unsigned DEFAULT NULL,
  `htmlcompression` int(1) NOT NULL,
  `characterencoding` varchar(16) DEFAULT NULL,
  `contenttype` varchar(16) DEFAULT NULL,
  `locale` varchar(16) DEFAULT NULL,
  `aliaspath` varchar(255) DEFAULT NULL,
  `gzip` int(1) NOT NULL,
  `title` varchar(255) DEFAULT NULL,  
  `job` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `description` text DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `alias` (`aliaspath`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_sitecontent`
--

CREATE TABLE IF NOT EXISTS `cf_sitecontent` (
  `siteref` bigint(20) unsigned NOT NULL,
  `classcontentref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`siteref`,`classcontentref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_sitedatasource`
--

CREATE TABLE IF NOT EXISTS `cf_sitedatasource` (
  `siteref` bigint(20) unsigned NOT NULL,
  `datasourceref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`siteref`,`datasourceref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_sitelist`
--

CREATE TABLE IF NOT EXISTS `cf_sitelist` (
  `siteref` bigint(20) unsigned NOT NULL,
  `listref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`siteref`,`listref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_sitesaprfc`
--

CREATE TABLE IF NOT EXISTS `cf_sitesaprfc` (
  `siteref` bigint(20) unsigned NOT NULL,
  `rfcgroup` varchar(64) NOT NULL,
  `rfcfunction` varchar(64) NOT NULL,
  PRIMARY KEY (`siteref`,`rfcgroup`,`rfcfunction`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_stylesheet`
--

CREATE TABLE IF NOT EXISTS `cf_stylesheet` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `content` text NOT NULL,
  `checkedoutby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_stylesheetversion`
--

CREATE TABLE IF NOT EXISTS `cf_stylesheetversion` (
  `stylesheetref` bigint(20) unsigned NOT NULL,
  `version` bigint(20) NOT NULL,
  `content` varbinary(64000) DEFAULT NULL,
  `tstamp` datetime NOT NULL,
  `commitedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`stylesheetref`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_template`
--

CREATE TABLE IF NOT EXISTS `cf_template` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `content` text NOT NULL,
  `scriptlanguage` int(11) NOT NULL,
  `checkedoutby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_templateversion`
--

CREATE TABLE IF NOT EXISTS `cf_templateversion` (
  `templateref` bigint(20) unsigned NOT NULL,
  `version` bigint(20) NOT NULL,
  `content` varbinary(64000) DEFAULT NULL,
  `tstamp` datetime NOT NULL,
  `commitedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`templateref`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_user`
--

CREATE TABLE IF NOT EXISTS `cf_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `vorname` varchar(50) NOT NULL,
  `nachname` varchar(50) NOT NULL,
  `email` varchar(50) NOT NULL,
  `passwort` varchar(50) NOT NULL,
  `salt` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;



SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Tabellenstruktur für Tabelle `cf_quartz`
--


CREATE TABLE IF NOT EXISTS `cf_quartz` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `schedule` varchar(50) DEFAULT NULL,
  `site_ref` bigint(20) DEFAULT NULL,
  `active` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=3 ;

--
-- Datenbank: `clownfish`
--

--
-- Daten für Tabelle `cf_attributetype`
--

INSERT INTO `cf_attributetype` (`id`, `name`) VALUES
(1, 'boolean'),
(2, 'string'),
(3, 'integer'),
(4, 'real'),
(5, 'htmltext'),
(6, 'datetime'),
(7, 'hashstring'),
(8, 'media'),
(9, 'text'),
(10, 'markdown');

--
-- Daten für Tabelle `cf_property`
--

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('cache_folder', '', 1),
('html.compression', 'on', 1),
('html.gzip', 'on', 1),
('mail.password', '', 1),
('mail.sendfrom', '', 1),
('mail.smtp.host', '', 1),
('mail.transport.protocol', 'smtp', 1),
('mail.user', '', 1),
('media.folder', '', 1),
('response.characterencoding', 'UTF-8', 1),
('response.contenttype', 'text/html', 1),
('response.locale', 'de', 1),
('sap.support', 'false', 1);

INSERT INTO `cf_user` (`id`, `vorname`, `nachname`, `email`, `passwort`, `salt`) VALUES
(1, 'Admin', 'Istrator', 'admin', 'Ll66CGHeusR7eoQPejg8t3CKkpVdpm2IlN/dZif4aGE=', 'zm85UW0YCIyBCxOXTagQQYcezjLzIQ');

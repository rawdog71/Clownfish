-- phpMyAdmin SQL Dump
-- version 3.4.9
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 18. Mrz 2019 um 10:01
-- Server Version: 5.7.18
-- PHP-Version: 5.3.8

CREATE DATABASE IF NOT EXISTS clownfish CHARACTER SET UTF8 collate utf8_general_ci;
USE clownfish;

CREATE USER `clownfish`@'localhost' IDENTIFIED BY 'clownfish';
CREATE USER `clownfish`@'%' IDENTIFIED BY 'clownfish';

GRANT ALL PRIVILEGES ON `clownfish`.* TO 'clownfish'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON `clownfish`.* TO 'clownfish'@'%' WITH GRANT OPTION;

FLUSH PRIVILEGES;

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
  `description` varchar(255) DEFAULT NULL,
  `indexed` tinyint(4) NOT NULL DEFAULT '0',
  `scrapped` tinyint(4) NOT NULL DEFAULT '0',  
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
  `relationref` bigint(20) unsigned DEFAULT NULL,
  `isindex` tinyint(1) DEFAULT '0',
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
  `indexed` tinyint(4) NOT NULL DEFAULT '0',
  `content_classref` bigint(20) DEFAULT NULL,
  `content_assetref` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_attributetype`
--

CREATE TABLE IF NOT EXISTS `cf_attributetype` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `searchrelevant` tinyint(3) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_class`
--

CREATE TABLE IF NOT EXISTS `cf_class` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `searchrelevant` tinyint(4) NOT NULL DEFAULT '0',
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
  `scrapped` tinyint(4) NOT NULL DEFAULT '0',
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
  `content` mediumtext,
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
  `content` blob,
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
  `contenttype` varchar(32) DEFAULT NULL,
  `locale` varchar(16) DEFAULT NULL,
  `aliaspath` varchar(255) DEFAULT NULL,
  `gzip` int(1) NOT NULL,
  `title` varchar(255) DEFAULT NULL,  
  `job` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `description` text DEFAULT '',
  `staticsite` tinyint(4) NOT NULL DEFAULT '0',
  `searchrelevant` tinyint(4) NOT NULL DEFAULT '0',
  `hitcounter` bigint(20) unsigned NOT NULL DEFAULT '0',
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

--
-- Tabellenstruktur für Tabelle `cf_sitekeywordlist`
--

CREATE TABLE `cf_sitekeywordlist` (
  `keywordlistref` bigint(20) UNSIGNED NOT NULL,
  `siteref` bigint(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`siteref`,`keywordlistref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_stylesheet`
--

CREATE TABLE IF NOT EXISTS `cf_stylesheet` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `content` mediumtext NOT NULL,
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
  `content` blob,
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
  `content` mediumtext NOT NULL,
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
  `content` blob,
  `tstamp` datetime NOT NULL,
  `commitedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`templateref`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_java`
--

CREATE TABLE IF NOT EXISTS `cf_java` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `content` mediumtext NOT NULL,
  `checkedoutby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_javaversion`
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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- --------------------------------------------------------
--
-- Tabellenstruktur für Tabelle `cf_assetlist`
--

CREATE TABLE IF NOT EXISTS `cf_assetlist` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_assetlistcontent`
--

CREATE TABLE IF NOT EXISTS `cf_assetlistcontent` (
  `assetlistref` bigint(20) unsigned NOT NULL,
  `assetref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`assetlistref`,`assetref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_keywordlist`
--

CREATE TABLE IF NOT EXISTS `cf_keywordlist` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_keywordlistcontent`
--

CREATE TABLE IF NOT EXISTS `cf_keywordlistcontent` (
  `keywordlistref` bigint(20) unsigned NOT NULL,
  `keywordref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`keywordlistref`,`keywordref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_siteassetlist`
--

CREATE TABLE IF NOT EXISTS `cf_siteassetlist` (
  `siteref` bigint(20) unsigned NOT NULL,
  `assetlistref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`siteref`,`assetlistref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Tabellenstruktur für Tabelle `cf_backend`
--

CREATE TABLE IF NOT EXISTS `cf_backend` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_userbackend`
--

CREATE TABLE IF NOT EXISTS `cf_userbackend` (
  `userref` bigint(20) unsigned NOT NULL,
  `backendref` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`userref`,`backendref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_searchhistory`
--

CREATE TABLE `clownfish`.`cf_searchhistory` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `expression` VARCHAR(255) NULL,
  `counter` INT UNSIGNED NULL DEFAULT 1,
  PRIMARY KEY (`id`));

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_webservice`
--

CREATE TABLE IF NOT EXISTS `cf_webservice` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `clownfish`.`cf_webservice` 
ADD UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cf_webserviceauth`
--

CREATE TABLE IF NOT EXISTS `cf_webserviceauth` (
  `user_ref` bigint(20) unsigned NOT NULL,
  `webservice_ref` bigint(20) unsigned NOT NULL,
  `hash` varchar(255) NOT NULL,
  PRIMARY KEY (`user_ref`,`webservice_ref`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Daten für Tabelle `cf_attributetype`
--

INSERT INTO `cf_attributetype` (`id`, `name`, `searchrelevant`) VALUES
(1, 'boolean', 0),
(2, 'string', 1),
(3, 'integer', 0),
(4, 'real', 0),
(5, 'htmltext', 1),
(6, 'datetime', 0),
(7, 'hashstring', 0),
(8, 'media', 0),
(9, 'text', 1),
(10, 'markdown', 1),
(11, 'classref', 0),
(12, 'assetref', 0);

--
-- Daten für Tabelle `cf_property`
--

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('html_compression', 'on', 1),
('html_gzip', 'on', 1),
('mail_password', '', 1),
('mail_sendfrom', '', 1),
('mail_smtp_host', '', 1),
('mail_transport_protocol', 'smtp', 1),
('mail_user', '', 1),
('folder_cache', '#foldercache#', 1),
('folder_media', '#foldermedia#', 1),
('folder_static', '#folderstatic#', 1),
('folder_index', '#folderindex#', 1),
('folder_icon', '#foldericon#', 1),
('folder_pdf', '#folderpdf#', 1),
('folder_attachments', '#folderattachments#', 1),
('job_support', 'true', 1),
('lucene_searchlimit', '25', 1),
('response_characterencoding', 'UTF-8', 1),
('response_contenttype', 'text/html', 1),
('response_locale', 'de', 1),
('sap_support', 'false', 1),
('site_error', 'error', 1),
('site_root', 'root', 1),
('site_search', 'searchresult', 1),
('info_assetmetadata', 'true', 1);

--
-- Daten für Tabelle `cf_backend`
--

INSERT INTO `cf_backend` (`id`, `name`) VALUES
(1, 'Siteoverview'),
(2, 'Templates'),
(3, 'Stylesheets'),
(4, 'Javascripts'),
(5, 'Classes'),
(6, 'Content'),
(7, 'Datalists'),
(8, 'Datasources'),
(9, 'Assets'),
(10, 'AssetLibraries'),
(11, 'Keywords'),
(12, 'KeywordLists'),
(13, 'Properties'),
(14, 'Jobs'),
(15, 'User'),
(16, 'Management');

--
-- Daten für Tabelle `cf_webservice`
--

INSERT INTO `cf_webservice` (`name`) VALUES ('BarcodeServlet');

INSERT INTO `cf_user` (`id`, `vorname`, `nachname`, `email`, `passwort`, `salt`) VALUES
(1, 'Admin', 'Istrator', 'admin', 'Ll66CGHeusR7eoQPejg8t3CKkpVdpm2IlN/dZif4aGE=', 'zm85UW0YCIyBCxOXTagQQYcezjLzIQ');

INSERT INTO `cf_userbackend` (`userref`, `backendref`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(1, 11),
(1, 12),
(1, 13),
(1, 14),
(1, 15),
(1, 16);

INSERT INTO `cf_site` (`id`, `name`, `templateref`, `parentref`, `stylesheetref`, `javascriptref`, `htmlcompression`, `characterencoding`, `contenttype`, `locale`, `aliaspath`, `gzip`, `title`, `job`, `description`, `staticsite`, `searchrelevant`) VALUES
(1, 'root', 1, 0, NULL, NULL, 0, 'UTF-8', 'text/html', 'de', 'root', 0, 'Clownfish root', 0, 'Default root site', 0, 0),
(2, 'error', 2, 0, NULL, NULL, 0, 'UTF-8', 'text/html', 'de', 'error', 0, 'Clownfish error', 0, 'Default error site', 0, 0),
(3, 'searchresult', 3, 0, NULL, NULL, 1, 'UTF-8', 'text/html', 'de', 'searchresult', 0, 'Searchresult', 0, 'Searchresult', 0, 0);

INSERT INTO `cf_template` (`id`, `name`, `content`, `scriptlanguage`, `checkedoutby`) VALUES
(1, 'root', '<!DOCTYPE html><html xmlns="http://www.w3.org/1999/xhtml" lang="de"><head><title>${metainfo.title}</title><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"><meta name="description" content="${metainfo.description}"/><link rel="icon" sizes="192x192" href="/images/favicon.ico"><link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono" rel="stylesheet"><link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous"><link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css"><link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet"><link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.9/css/mdb.min.css" rel="stylesheet"><style>${css}	main {  padding-top: 30px; 	}	.bg-light { background-color: #FFFFFF!important; opacity: 1.0!important; }	.container-fluid { padding-bottom: 10px; 	}</style></head><body><nav class="navbar navbar-expand-md navbar-dark sticky-top light-blue lighten-3 bg-dark text-black"><div class="container"><div class="row w-100"><div class="col-1 align-self-center"><img src="/images/clownfish.svg" width="72" alt="clownfish-logo" /></div><div class="col-11 align-self-center"><h1>Clownfish CMS - Version ${metainfo.version}</h1><h3>on Tomcat ${metainfo.versionTomcat} with Mojarra ${metainfo.versionMojarra}</h3></div></div></div></nav><main role="main" class="container"><div class="jumbotron"><h2>Clownfish Content Management System</h2><div class="container-fluid">This CMS is used to implement a bandwidth from simple websites to complex portal systems. </div><h2>Features</h2><div class="container-fluid"><ul class="list-unstyled"><li class="media my-4"><img class="mr-3" src="/images/spring-boot.svg" width="64" alt="springboot-logo" /><div class="media-body"><h5 class="mt-0 mb-1">Spring Boot</h5>Based upon Spring Boot framework.</div></li><li class="media my-4"><img class="mr-3" src="/images/Tomcat.svg" width="64" alt="tomcat-logo" /><div class="media-body"><h5 class="mt-0 mb-1">Apache Tomcat</h5>Uses embedded Apache Tomcat as application server.</div></li><li class="media my-4"><img class="mr-3" src="/images/freemarker.svg" width="64" alt="freemarker-logo" /><div class="media-body"><h5 class="mt-0 mb-1">Freemarker</h5>Code your templates with Freemarker.</div></li><li class="media my-4"><img class="mr-3" src="/images/velocity.svg" width="64" alt="velocity-logo" /><div class="media-body"><h5 class="mt-0 mb-1">Velocity</h5>Code your templates with Velocity.</div></li><li class="media my-4"><img class="mr-3" src="/images/MySQL.svg" width="64" alt="mysql-logo" /><div class="media-body"><h5 class="mt-0 mb-1">MySQL</h5>MySQL database as content storage layer.</div></li><li class="media my-4"><img class="mr-3" src="/images/SAP.svg" width="64" alt="sap-logo" /><div class="media-body"><h5 class="mt-0 mb-1">SAP</h5>Optional SAP integration via remote function calls.</div></li><li class="media my-4"><img class="mr-3" src="/images/database.svg" width="64" alt="database-logo" /><div class="media-body"><h5 class="mt-0 mb-1">External datasources</h5>Connect external databases (MSSQL Server / MySQL / Oracle / PostgreSQL).</div></li><li class="media my-4"><img class="mr-3" src="/images/markdown.svg" width="64" alt="markdown-logo" /><div class="media-body"><h5 class="mt-0 mb-1">Markdown content</h5>Write content elements in markdown.</div></li><li class="media my-4"><img class="mr-3" src="/images/quartz.svg" width="64" alt="quartz-logo" /><div class="media-body"><h5 class="mt-0 mb-1">Quartz job scheduler</h5>Trigger your jobs with quartz.</div></li><li class="media my-4"><img class="mr-3" src="/images/Lucene.svg" width="64" alt="lucene-logo" /><div class="media-body"><h5 class="mt-0 mb-1">Lucene search engine</h5>Automatically index content for searching.</div></li><li class="media my-4"><img class="mr-3" src="/images/tika.svg" width="64" alt="tika-logo" /><div class="media-body"><h5 class="mt-0 mb-1">Tika content analyzer</h5>Analyze metadata and content of assets.</div></li></ul></div></div></main><footer class="page-footer font-small light-blue lighten-3 text-black"><div class="container"><div class="row"><div class="col-md-12 py-5"><div class="mb-5 flex-center"><a class="git-ic" href="https://github.com/rawdog71/Clownfish" target="_blank" aria-label="Github"><i class="fab fa-github fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="tw-ic" href="https://twitter.com/ClownfishCms" target="_blank" aria-label="Twitter"><i class="fab fa-twitter fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="li-ic" href="https://www.linkedin.com/in/rainer-sulzbach-a59859151/" target="_blank" aria-label="LinkedIn"><i class="fab fa-linkedin-in fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="xin-ic" href="https://www.xing.com/profile/Rainer_Sulzbach/cv" target="_blank" aria-label="Xing"><i class="fab fa-xing fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a></div></div></div></div><div class="footer-copyright text-center py-3 black-text">&copy; 2019 Copyright: Rainer Sulzbach</div></footer><script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script><script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script><script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script><script src="/resources/js/clownfish.js"></script><script>${js}</script></body></html>', 0, 0),
(2, 'error', '<!DOCTYPE html><html xmlns="http://www.w3.org/1999/xhtml" lang="de"><head><title>${metainfo.title}</title><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"><meta name="description" content="${metainfo.description}"/><link rel="icon" sizes="192x192" href="/images/favicon.ico"><link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono" rel="stylesheet"><link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous"><link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css"><link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet"><link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.9/css/mdb.min.css" rel="stylesheet"><style>${css}	main {  padding-top: 30px; 	}	.bg-light { background-color: #FFFFFF!important; opacity: 1.0!important; }	.container-fluid { padding-bottom: 10px; 	}</style></head><body><nav class="navbar navbar-expand-md navbar-dark sticky-top light-blue lighten-3 bg-dark text-black"><div class="container"><div class="row w-100"><div class="col-1 align-self-center"><img src="/images/clownfish.svg" width="72" /></div><div class="col-11 align-self-center"><h1>Clownfish CMS - Version ${metainfo.version}</h1><h3>on Tomcat ${metainfo.versionTomcat} with Mojarra ${metainfo.versionMojarra}</h3></div></div></div></nav><main role="main" class="container"><div class="jumbotron"><h2>Clownfish Content Management System</h2><div class="container-fluid"><ul class="list-unstyled"><li class="media my-4"><img class="mr-3" src="/images/clownfish-error-48.png" width="64" /><div class="media-body text-danger"><h5 class="mt-0 mb-1">ERROR</h5><b>The site you are looking for does not exist.</b></div></li></ul></div></div></main><footer class="page-footer font-small light-blue lighten-3 text-black"><div class="container"><div class="row"><div class="col-md-12 py-5"><div class="mb-5 flex-center"><a class="git-ic" href="https://github.com/rawdog71/Clownfish" target="_blank"><i class="fab fa-github fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="tw-ic" href="https://twitter.com/ClownfishCms" target="_blank"><i class="fab fa-twitter fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="li-ic" href="https://www.linkedin.com/in/rainer-sulzbach-a59859151/" target="_blank"><i class="fab fa-linkedin-in fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="xin-ic" href="https://www.xing.com/profile/Rainer_Sulzbach/cv" target="_blank"><i class="fab fa-xing fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a></div></div></div></div><div class="footer-copyright text-center py-3 black-text">&copy; 2019 Copyright: Rainer Sulzbach</div></footer><script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script><script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script><script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script><script src="/resources/js/clownfish.js"></script><script>${js}</script></body></html>', 0, 0),
(3, 'searchresult', '<#assign textHash = {  "application/pdf": "pdf.svg", "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "docx.svg", "application/msword": "docx.svg" }><!DOCTYPE html><html xmlns="http://www.w3.org/1999/xhtml" lang="de"><head><title>${metainfo.title}</title><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"><meta name="description" content="${metainfo.description}"/><link rel="icon" sizes="192x192" href="/images/favicon.ico"><link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono" rel="stylesheet"><link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous"><link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css"><link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet"><link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.9/css/mdb.min.css" rel="stylesheet"><style>${css}	main {  padding-top: 30px; 	}	.bg-light { background-color: #FFFFFF!important; opacity: 1.0!important; }	.container-fluid { padding-bottom: 10px; 	}</style></head><#assign nothingfound = true><body><nav class="navbar navbar-expand-md navbar-dark sticky-top light-blue lighten-3 bg-dark text-black"><div class="container"><div class="row w-100"><div class="col-1 align-self-center"><img src="/images/clownfish.svg" width="72" /></div><div class="col-11 align-self-center"><h1>Clownfish CMS - Version ${metainfo.version}</h1><h3>on Tomcat ${metainfo.versionTomcat} with Mojarra ${metainfo.versionMojarra}</h3></div></div></div></nav><main role="main" class="container"><div class="jumbotron"><h2>Clownfish Content Management System</h2><div class="container-fluid">Search results for ${searchmetadata.cfSearchQuery}</div><div class="container-fluid"><#if searchcontentlist?has_content><#assign nothingfound = false><ul class="list-unstyled"><#list searchcontentlist?keys as searchitem><li class="media my-4"><img class="mr-3" src="/images/document.svg" width="64" /><div class="media-body"><#if searchcontentlist[searchitem].title?has_content><h5 class="mt-0 mb-1"><a href="/${searchcontentlist[searchitem].name}">${searchcontentlist[searchitem].title}</a> </h5><#else><h5 class="mt-0 mb-1"><a href="/${searchcontentlist[searchitem].name}">${searchcontentlist[searchitem].name}</a> </h5>  </#if>   ${searchcontentlist[searchitem].description} </div></li></#list></ul></#if><#if searchassetlist?has_content><#assign nothingfound = false><ul class="list-unstyled"><#list searchassetlist?keys as searchitem><li class="media my-4"><#assign textKey = searchassetlist[searchitem].mimetype><img class="mr-3" src="/images/${textHash[textKey]}" width="64" /><div class="media-body"><h5 class="mt-0 mb-1"><a href="/GetAsset?mediaid=${searchassetlist[searchitem].id}">${searchassetlist[searchitem].name}</a> </h5><#if searchassetlist[searchitem].description??>${searchassetlist[searchitem].description} </#if></div></li></#list></ul></#if><#if nothingfound>No matches found.</#if></div><div class="container-fluid">Search time ${searchmetadata.cfSearchTime} ms</div></div></main><footer class="page-footer font-small light-blue lighten-3 text-black"><div class="container"><div class="row"><div class="col-md-12 py-5"><div class="mb-5 flex-center"><a class="git-ic" href="https://github.com/rawdog71/Clownfish" target="_blank"><i class="fab fa-github fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="tw-ic" href="https://twitter.com/ClownfishCms" target="_blank"><i class="fab fa-twitter fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="li-ic" href="https://www.linkedin.com/in/rainer-sulzbach-a59859151/" target="_blank"><i class="fab fa-linkedin-in fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a><a class="xin-ic" href="https://www.xing.com/profile/Rainer_Sulzbach/cv" target="_blank"><i class="fab fa-xing fa-lg black-text mr-md-5 mr-3 fa-2x"> </i></a></div></div></div></div><div class="footer-copyright text-center py-3 black-text">&copy; 2019 Copyright: Rainer Sulzbach</div></footer><script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script><script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script><script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script><script src="/resources/js/clownfish.js"></script><script>${js}</script></body></html>', 0, 0);

COMMIT;
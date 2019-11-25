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
  `indexed` tinyint(4) NOT NULL DEFAULT '0',  
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
  `staticsite` tinyint(4) NOT NULL DEFAULT '0',
  `searchrelevant` tinyint(4) NOT NULL DEFAULT '0',
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
-- Datenbank: `clownfish`
--

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
(10, 'markdown', 1);

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
('folder_cache', '', 1),
('folder_media', '', 1),
('folder_static', '', 1),
('folder_index', '', 1),
('folder_icon', '', 1),
('job_support', 'true', 1),
('lucene_searchlimit', '25', 1),
('response_characterencoding', 'UTF-8', 1),
('response_contenttype', 'text/html', 1),
('response_locale', 'de', 1),
('sap_support', 'false', 1),
('site_error', 'error', 1),
('site_root', 'root', 1),
('site_search', 'searchresult', 1);

INSERT INTO `cf_user` (`id`, `vorname`, `nachname`, `email`, `passwort`, `salt`) VALUES
(1, 'Admin', 'Istrator', 'admin', 'Ll66CGHeusR7eoQPejg8t3CKkpVdpm2IlN/dZif4aGE=', 'zm85UW0YCIyBCxOXTagQQYcezjLzIQ');

INSERT INTO `cf_site` (`id`, `name`, `templateref`, `parentref`, `stylesheetref`, `javascriptref`, `htmlcompression`, `characterencoding`, `contenttype`, `locale`, `aliaspath`, `gzip`, `title`, `job`, `description`, `staticsite`, `searchrelevant`) VALUES
(1, 'root', 1, 0, NULL, NULL, 0, 'UTF-8', 'text/html', 'de', 'root', 0, 'Clownfish root', 0, 'Default root site', 0, 0),
(2, 'error', 2, 0, NULL, NULL, 0, 'UTF-8', 'text/html', 'de', 'error', 0, 'Clownfish error', 0, 'Default error site', 0, 0),
(3, 'searchresult', 3, 0, NULL, NULL, 1, 'UTF-8', 'text/html', 'de', 'searchresult', 0, 'Searchresult', 0, 'Searchresult', 0, 0);

INSERT INTO `cf_template` (`id`, `name`, `content`, `scriptlanguage`, `checkedoutby`) VALUES
(1, 'root', '<!DOCTYPE html>\r\n<html xmlns="http://www.w3.org/1999/xhtml" lang="de">\r\n   <head>\r\n     <title>${metainfo.title}</title>\r\n     <meta charset="utf-8">\r\n     <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">\r\n     <meta name="description" content="${metainfo.description}"/>\r\n     <link rel="icon" sizes="192x192" href="/images/favicon.ico">\r\n     <link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono" rel="stylesheet">\r\n     <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">\r\n     <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.9/css/mdb.min.css" rel="stylesheet">\r\n     <style>\r\n       ${css}\r\n		main { \r\n          padding-top: 30px; \r\n       	}\r\n		.bg-light {\r\n          background-color: #FFFFFF!important;\r\n          opacity: 1.0!important;\r\n        }\r\n       	.container-fluid {\r\n          padding-bottom: 10px; \r\n       	}\r\n     </style>\r\n   </head>\r\n   <body>\r\n     <nav class="navbar navbar-expand-md navbar-dark sticky-top light-blue lighten-3 bg-dark text-black">\r\n       <div class="container">\r\n         <div class="row w-100">\r\n           <div class="col-1 align-self-center">\r\n             <img src="/images/clownfish.svg" width="72" />\r\n           </div>\r\n           <div class="col-11 align-self-center">\r\n             <h1>Clownfish CMS - Version ${metainfo.version}</h1>\r\n           </div>\r\n         </div>\r\n        </div>\r\n     </nav>\r\n     <main role="main" class="container">\r\n      <div class="jumbotron">\r\n        <h2>Clownfish Content Management System</h2>\r\n        <div class="container-fluid">\r\n          This CMS is used to implement a bandwidth from simple websites to complex portal systems. \r\n        </div>\r\n     	<h2>Features</h2>\r\n        <div class="container-fluid">\r\n          <ul class="list-unstyled">\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/spring-boot.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Spring Boot</h5>\r\n                Based upon Spring Boot framework.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/Tomcat.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Apache Tomcat</h5>\r\n                Uses embedded Apache Tomcat as application server.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/freemarker.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Freemarker</h5>\r\n                Code your templates with Freemarker.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/velocity.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Velocity</h5>\r\n                Code your templates with Velocity.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/MySQL.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">MySQL</h5>\r\n                MySQL database as content storage layer.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/SAP.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">SAP</h5>\r\n                Optional SAP integration via remote function calls.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/database.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">External datasources</h5>\r\n                Connect external databases (MSSQL Server / MySQL).\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/markdown.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Markdown content</h5>\r\n                Write content elements in markdown.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/quartz.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Quartz job scheduler</h5>\r\n                Trigger your jobs with quartz.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/Lucene.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Lucene search engine</h5>\r\n                Automatically index content for searching.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/tika.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Tika content analyzer</h5>\r\n                Analyze metadata and content of assets.\r\n              </div>\r\n            </li>\r\n          </ul>\r\n        </div>\r\n       </div>\r\n     </main>\r\n     <footer class="page-footer font-small light-blue lighten-3 text-black">\r\n       <div class="container">\r\n         <div class="row">\r\n           <div class="col-md-12 py-5">\r\n             <div class="mb-5 flex-center">\r\n               <a class="git-ic" href="https://github.com/rawdog71/Clownfish" target="_blank">\r\n                 <i class="fab fa-github fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="tw-ic" href="https://twitter.com/ClownfishCms" target="_blank">\r\n                 <i class="fab fa-twitter fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="li-ic" href="https://www.linkedin.com/in/rainer-sulzbach-a59859151/" target="_blank">\r\n                 <i class="fab fa-linkedin-in fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="xin-ic" href="https://www.xing.com/profile/Rainer_Sulzbach/cv" target="_blank">\r\n                 <i class="fab fa-xing fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n             </div>\r\n           </div>\r\n         </div>\r\n       </div>\r\n       <div class="footer-copyright text-center py-3 black-text">&copy; 2019 Copyright: Rainer Sulzbach</div>\r\n     </footer>\r\n     <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>\r\n     <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>\r\n     <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>\r\n     <script src="/resources/js/clownfish.js"></script>\r\n     <script>${js}</script>\r\n   </body>\r\n</html>', 0, 0),
(2, 'error', '<!DOCTYPE html>\r\n<html xmlns="http://www.w3.org/1999/xhtml" lang="de">\r\n   <head>\r\n     <title>${metainfo.title}</title>\r\n     <meta charset="utf-8">\r\n     <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">\r\n     <meta name="description" content="${metainfo.description}"/>\r\n     <link rel="icon" sizes="192x192" href="/images/favicon.ico">\r\n     <link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono" rel="stylesheet">\r\n     <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">\r\n     <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.9/css/mdb.min.css" rel="stylesheet">\r\n     <style>\r\n       ${css}\r\n		main { \r\n          padding-top: 30px; \r\n       	}\r\n		.bg-light {\r\n          background-color: #FFFFFF!important;\r\n          opacity: 1.0!important;\r\n        }\r\n       	.container-fluid {\r\n          padding-bottom: 10px; \r\n       	}\r\n     </style>\r\n   </head>\r\n   <body>\r\n     <nav class="navbar navbar-expand-md navbar-dark sticky-top light-blue lighten-3 bg-dark text-black">\r\n       <div class="container">\r\n         <div class="row w-100">\r\n           <div class="col-1 align-self-center">\r\n             <img src="/images/clownfish.svg" width="72" />\r\n           </div>\r\n           <div class="col-11 align-self-center">\r\n             <h1>Clownfish CMS - Version ${metainfo.version}</h1>\r\n           </div>\r\n         </div>\r\n        </div>\r\n     </nav>\r\n     <main role="main" class="container">\r\n      <div class="jumbotron">\r\n        <h2>Clownfish Content Management System</h2>\r\n        <div class="container-fluid">\r\n          <ul class="list-unstyled">\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/clownfish-error-48.png" width="64" />\r\n              <div class="media-body text-danger">\r\n                <h5 class="mt-0 mb-1">ERROR</h5>\r\n                <b>The site you are looking for does not exist.</b>\r\n              </div>\r\n            </li>\r\n          </ul>\r\n        </div>\r\n       </div>\r\n     </main>\r\n     <footer class="page-footer font-small light-blue lighten-3 text-black">\r\n       <div class="container">\r\n         <div class="row">\r\n           <div class="col-md-12 py-5">\r\n             <div class="mb-5 flex-center">\r\n               <a class="git-ic" href="https://github.com/rawdog71/Clownfish" target="_blank">\r\n                 <i class="fab fa-github fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="tw-ic" href="https://twitter.com/ClownfishCms" target="_blank">\r\n                 <i class="fab fa-twitter fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="li-ic" href="https://www.linkedin.com/in/rainer-sulzbach-a59859151/" target="_blank">\r\n                 <i class="fab fa-linkedin-in fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="xin-ic" href="https://www.xing.com/profile/Rainer_Sulzbach/cv" target="_blank">\r\n                 <i class="fab fa-xing fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n             </div>\r\n           </div>\r\n         </div>\r\n       </div>\r\n       <div class="footer-copyright text-center py-3 black-text">&copy; 2019 Copyright: Rainer Sulzbach</div>\r\n     </footer>\r\n     <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>\r\n     <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>\r\n     <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>\r\n     <script src="/resources/js/clownfish.js"></script>\r\n     <script>${js}</script>\r\n   </body>\r\n</html>', 0, 0),
(3, 'searchresult', '<#assign textHash = { \r\n  "application/pdf": "pdf.svg",\r\n  "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "docx.svg",\r\n  "application/msword": "docx.svg"\r\n}>\r\n<!DOCTYPE html>\r\n<html xmlns="http://www.w3.org/1999/xhtml" lang="de">\r\n   <head>\r\n     <title>${metainfo.title}</title>\r\n     <meta charset="utf-8">\r\n     <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">\r\n     <meta name="description" content="${metainfo.description}"/>\r\n     <link rel="icon" sizes="192x192" href="/images/favicon.ico">\r\n     <link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono" rel="stylesheet">\r\n     <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">\r\n     <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.9/css/mdb.min.css" rel="stylesheet">\r\n     <style>\r\n       ${css}\r\n		main { \r\n          padding-top: 30px; \r\n       	}\r\n		.bg-light {\r\n          background-color: #FFFFFF!important;\r\n          opacity: 1.0!important;\r\n        }\r\n       	.container-fluid {\r\n          padding-bottom: 10px; \r\n       	}\r\n     </style>\r\n   </head>\r\n<#assign nothingfound = true>\r\n   <body>\r\n     <nav class="navbar navbar-expand-md navbar-dark sticky-top light-blue lighten-3 bg-dark text-black">\r\n       <div class="container">\r\n         <div class="row w-100">\r\n           <div class="col-1 align-self-center">\r\n             <img src="/images/clownfish.svg" width="72" />\r\n           </div>\r\n           <div class="col-11 align-self-center">\r\n             <h1>Clownfish CMS - Version ${metainfo.version}</h1>\r\n           </div>\r\n         </div>\r\n        </div>\r\n     </nav>\r\n     <main role="main" class="container">\r\n      <div class="jumbotron">\r\n        <h2>Clownfish Content Management System</h2>\r\n        <div class="container-fluid">\r\n          Search results for ${searchmetadata.cfSearchQuery}\r\n        </div>\r\n        <div class="container-fluid">\r\n          <#if searchcontentlist?has_content>\r\n            <#assign nothingfound = false>\r\n          <ul class="list-unstyled">\r\n  			<#list searchcontentlist?keys as searchitem>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/document.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1"><a href="/${searchcontentlist[searchitem].name}">${searchcontentlist[searchitem].title}</a> </h5>\r\n                ${searchcontentlist[searchitem].description} \r\n              </div>\r\n            </li>\r\n  			</#list>\r\n          </ul>\r\n          </#if>\r\n\r\n          <#if searchassetlist?has_content>\r\n            <#assign nothingfound = false>\r\n            <ul class="list-unstyled">\r\n            <#list searchassetlist?keys as searchitem>\r\n              <li class="media my-4">\r\n                <#assign textKey = searchassetlist[searchitem].mimetype>\r\n                <img class="mr-3" src="/images/${textHash[textKey]}" width="64" />\r\n                <div class="media-body">\r\n                  <h5 class="mt-0 mb-1"><a href="/GetAsset?mediaid=${searchassetlist[searchitem].id}">${searchassetlist[searchitem].name}</a> </h5>\r\n                  <#if searchassetlist[searchitem].description??>\r\n                  ${searchassetlist[searchitem].description} \r\n                  </#if>\r\n              </div>\r\n              </li>\r\n            </#list>\r\n            </ul>\r\n            </#if>\r\n		  <#if nothingfound>\r\n            No matches found.\r\n          </#if>\r\n        </div>\r\n        <div class="container-fluid">\r\n          Search time ${searchmetadata.cfSearchTime} ms\r\n        </div>\r\n       </div>\r\n     </main>\r\n     <footer class="page-footer font-small light-blue lighten-3 text-black">\r\n       <div class="container">\r\n         <div class="row">\r\n           <div class="col-md-12 py-5">\r\n             <div class="mb-5 flex-center">\r\n               <a class="git-ic" href="https://github.com/rawdog71/Clownfish" target="_blank">\r\n                 <i class="fab fa-github fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="tw-ic" href="https://twitter.com/ClownfishCms" target="_blank">\r\n                 <i class="fab fa-twitter fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="li-ic" href="https://www.linkedin.com/in/rainer-sulzbach-a59859151/" target="_blank">\r\n                 <i class="fab fa-linkedin-in fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="xin-ic" href="https://www.xing.com/profile/Rainer_Sulzbach/cv" target="_blank">\r\n                 <i class="fab fa-xing fa-lg black-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n             </div>\r\n           </div>\r\n         </div>\r\n       </div>\r\n       <div class="footer-copyright text-center py-3 black-text">&copy; 2019 Copyright: Rainer Sulzbach</div>\r\n     </footer>\r\n     <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>\r\n     <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>\r\n     <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>\r\n     <script src="/resources/js/clownfish.js"></script>\r\n     <script>${js}</script>\r\n   </body>\r\n</html>', 0, 0);

INSERT INTO `cf_templateversion` (`templateref`, `version`, `content`, `tstamp`, `commitedby`) VALUES
(2, 1, 'xڵXQS�8~���?��ν�d�	Bnr��A!)���#۲�D�\\IN�2��[�$qB��ޕ�,���}��]�}�������(6��?��[<Q.x�{VlL��8��Ԟ�l�"��t:N^�X��$�Y�`B�STCx1�p���VPCXJ����:��\\�XF~L���ge&Ļ��bB�YF��T�B�LM@x��:a>����%�0±�	�=�w�cŒ16�������j_��0��ί�^�����F����b�f_)��v�9�Z(V4�Y$��	ɤ�ᏵzD%W�����v$e�)I��})_�?B"����,1٫3	h*���8�1���`[muM� �$�''��F��x)�-&�-�a7\n��9[0����\r�33����>N��ȿ\\�D^��_5ڻ����"�g[o�''����as�u#��>�O�qtԿv�>n��`_I��bKzId22��V��]�H�TKAKH��\r^���]�YB"��@�rP2p�!''�:��H�p�i�L�1T�:c-�}������4���s��:a�;��s�7�^ނ���_�=��-V�''%A��nP��Z�4�\\~Vn��s���7z�GJfI�}ɥ�C��?/�(n2I�뺼L����\\��Q�n��..)�\ṅ<c���ɞ4F\n8��N����L_]O�[	� �\r7�Q�z`��Љ`>5F�0<+�B%+����4�-l�r���\Z�d-]�\r�B�cm}UB�)�b��X�X?�cП`My�}Hnkg�{���V�2_A�M����֓�BU��v �9k���\r�˄��?�+EgC���4d]TKƓj\nJ	�?m����{��-�A�JBհ������F��0S���j�fHUP�I�WQ�3m�\0\0���M���{��n��r�i�����`�P�*s9AF����W���)ܲ	L��\no��i�����񰆥ԍ�+U�|\0m���n �a��?�\r��������B�5�d� O".��?\n�B��\Z%� �Sv����0za��Uʝ�?Gª���[Y��9�(��SE��Z@ڜ(���� �6Q:��ױ�C�Q�i���q2����_��0g^U��4�ю�,.��QQ�~��xSt@��5��C!�ՑňG�d\n�!�b��.��b��[����h�<��L7\0���%�����i?g@-}�t�ݬz>��[ʌ�3��w�I����m��1h��1d֟//l�+Q�-U2d�:����=@ǟ���L/�+�M5��l��v]�}W:SegV�����U������{��\r���~���Bs�֒\\�b�R��GU�Xt�2���KFլtC5�-�i���L���h���IK6ߜ����Ƿ������l��`<P�:�8�i�M���O�Go�>�������ӊo�������/	��J[�6���;�i\n`��v��{g"�O>����{Q����G���LZ�d�%��<��:C�R�/v�+�ݴ:�����.�xr��IH\Z������_{��O��hN>\r��)�[W�f�;zg�''�R���\r���|]��=��}���Q�W;��G��h�KN�L�Й��=����/oG�nu:��ǇƢ���?��S', '2019-09-13 06:46:59', 1),
(1, 1, 'x��Y�S۸��7s��ί����l�	Bޤ�p�B	�B�ٖm%��Jr��𿿕�/NHh˻Bf�����~��Ү����tp���E*f�����O4�Y"[F�T�gۣ��\Z�,.B�i4\Z�X���$l>1`B����bd��]L�I����i32ݍ�IT��T`�\Z˝	�I�R2J�P�x�H�#ꫨ�!�����hB�̔f����d$h207�Z	_;�O�''h�(OJ�d/��|\nS#AXˠ�*�-ԜFu�	Z�Mcix��,�g,OQ�i�%��\0�\n9�)���cۓ��)����,QٛS�ܥ�0"#B��F�R�\n��=?�\\ΕT��C3�7�[VŪh�6+�@%�`+\n�&�#µ�-�(��c���`~}�~S��^ܜ����`���zxyU�۵����^{D��N���۴�}|��K�\ri�2pI�3�cje�X\ZD<"��$W	�,a�ukת�*a�rE[��+�x��b2��c�QW�jD�"�,#V��M�=�Z?�9��<w�F�Z��[�6�B������˫W1�;�C�����i��{�VI�oݯ�A�����Bw�.���Y�g\\�u��o4֞���L�S�-�!Ǫ�%�_𵴓���|�2�/s���r�x>����.Ѵ��r2G+�C�1,�������S������b����`��B9*��2R��Ĭ!@+�Sd��\0%c�M�Ι�u,�/S>B#өT�(Vga��0�OLIX`z�V����8DRx�x�7J*#KC��؁�f�����7$�."g�`�����>!!�R0M��\0�ガ6,}7mX��v�M_p�5�j<�\ne��Yf& ���Dղ"ņ�Nq����7��Ġ@�<j�����eDe< ��HqÊ�1x^�竅�cؕt\ZWRE���PMc�;C2GZhR��B�U&�|��͌����̒���U3�X8���O1�''��[)�uF''̚�l�2E�j�|����9SSG��L����ʬ��5c��sC��S_3����`�%bX 8���XDz�T���OA��~���#�\n~���m\Z��%��-�@X"���»�\n��/�] �!��?�Μ���OЄg��q��(Z�|Y���q��?X���~�ٸ��t��~|�r> ����v!~i�fGp��Ąp���k�?G�o�o\0�S��6\n$Ӝ��NC�H��+��,��6�K�|Y�f����ᜧ��<%�����{c�O!R�Ŕ��Ӟ6�^�]X�^DJ}8�=��NY�|o�א�����4(� �\\�E�k���}��9#��.�pt�3�q��4���]\0L7���/\n������т���"D���jg�OCl�Al��C\n13����p�}Y��g\0���Շ܎Mn7\ZZ��F:�Ձ\r���X��*�z�д3�)�}����v�@\n�?U-��i�.��2�u^_��g��([ľ�TQ:1�K�s�:\n ��TW\0r<#�2��Z1��(s�B!�<�q�yo �E������`���I�8�E6�)�Q���AC`��W]?k��:6�a]�\Z�m��&j�F�i=2�d��A,���t����k���\r� \n>Yԣi˒�d�n]HM\\o��Nݱ���l~�ǟ��X3X��XGI�[*x@�/r��M�������\Z���}�Ҷ�Yr�"O''"�\Z籤p]�쵒����5�[T�8\r8"N��-4Ck%�,�����#��uH���׌�I�ūY���cIF㼌�_{�qr\\���''��`���I����6>�99\\[6�n\Zn��_Gǣ����gm�(=�t\Z��&=���6�r4�B��E�b �i\n�/m�r�V��bָY�vz�Q�ݛ/N�D\rk��u-���U���y]�>߉��{Sk�w<����&��\0Wܭ������D�W�U�ks���v/%�h�R�w����������G����{A�:�����ߞ�������>�|aO��d��hA���������~��iO��0�/e��LD', '2019-09-17 11:28:40', 1),
(3, 1, 'xڵXos�8���;���󦵍!$!�dx�$��I.��I��#l�Ȗk��i����l��3�mi�����jW�WX�H�����3z�!\rG�6���f��1��a����]#���#���8�R��u�Mn''	�1���& /�f�3�1��7p�Z�D�������o��8�����2`j@=h���K��h42F5�Ǟi5\Z\rs�h4�p�4�h''Jj�;�+|H*9y��i�r#�6�l"''S���q,�li�t�#my2�iiCJF���lJ\0��Fԑ~�!C0��~�E4��b�3Ҳ�"��4��.����2w��c\Z)K�t/L5s΂k�Ҩ��\n���լFu\Z�c�4��#�t�P��O[f��)c��\n���cGT6L[�S�MZ�$�ɛh2�BN>!R+ѭ0�")�c�	��R�G�C	���FŨ(�1#�@%�Ɩċ���׎��˰��׎���S�M�~t�t7���������[�r^=�=��G�һh?�N�v���~̅�1�h��p��I��c�AeD<"�$�K�úqdTSH��ȶ�\0��aO��8c��xl2��Q)I�-V3�M۾[?$9p�2��F*F�CZ:�!��ȧ/_����̕�"�8�j ��cT�D�w��t���tF=_�����^̓��m�x|�^]���h�"��]��G�_:F�Q)%�.�\Z*HAW����,K�U�q)y\0׵��`�4g髙''�Kظ\n$t''9i�;��1C<D6�5-\r^{8F�C''�������L�9Qj4�����P�!0fJ���#j��j:t.ln���2E�Gh�[���*�[��P���\r�o�g����Hg���Х�OO�ec�Ҟ�"�y;4�.|��,��n�HG�H, )�B�fCp�\0�vEV���&l��P�s8T4��mم"�~��p8���"��A78���u''B�\0\0T����>s�eSu	�m�_$L\n��G���H�ذ݌���x2�`��|E]�	���\ny�c�e��������L�e�	�UP<�$L�v����^�\Z/�> ����P�����P������x���t�^Ӗ#a^��`-V�J�U.Y����>''�z=��N�8/*�--��k��oCU6S�dY^����%���P���\Z��؇1F�[g�{���f�V��`d����ȯ�]�V�Yp��;��ݮ��:\r��4^���\0�D���v~���.�D�=���?�̻���ȶ�t�2�N+w�r��)�v9I\Z\0��܉6���i)��:l���kB�,-_��\0Z��ۛ�.��\n�-G�z�>QgLe�����)%�e7Oav��Q�ʉ������1�se"�s}6��]P���`����Qu�nUQ4���N1�zz���7�E@�sb�J�ګ���I/k����ޡe��\rI{�����Ai�6����=H�z�R�1��ҕ���UWϚ���5�Di��-H�Ȭ�J�����0̸�� -�.ST�y.�iے�	lߠm�u\\o�V�2Z�_���߃7VJ����b�b�RF������i�b����'';\Z���B�f:�h�=q�K��U�^+譝�GѽCՊՀ>`��e�B��V�\\&b�̧''Lv����C��WUܧې��5�jX�`4H/	��w8�W5^}-?��?\\��o�7��g��،�f''�p��˫����O�m�2���h����~=��|��v��vշ_{D<� �iV�h�I�䃛1��۞�7���չ���}x�X����и6�vG����{�5��.\\܉��)CWz����{�����F�h���?�F���?T��Q�y0����.1�:�����錻��cD����''�=�?�������Y��$\Z:J��6���b��߼J���.O6���	�e����R�߳', '2019-09-17 11:22:40', 1);
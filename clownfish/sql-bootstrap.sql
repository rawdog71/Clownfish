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
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `searchrelevant` tinyint(3) UNSIGNED NOT NULL
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
('cache_folder', '', 1),
('html_compression', 'on', 1),
('html_gzip', 'on', 1),
('mail_password', '', 1),
('mail_sendfrom', '', 1),
('mail_smtp_host', '', 1),
('mail_transport_protocol', 'smtp', 1),
('mail_user', '', 1),
('media_folder', '', 1),
('static_folder', '', 1),
('index_folder', '', 1),
('response_characterencoding', 'UTF-8', 1),
('response_contenttype', 'text/html', 1),
('response_locale', 'de', 1),
('sap_support', 'false', 1),
('error_site', 'error', 1),
('root_site', 'root', 1);

INSERT INTO `cf_user` (`id`, `vorname`, `nachname`, `email`, `passwort`, `salt`) VALUES
(1, 'Admin', 'Istrator', 'admin', 'Ll66CGHeusR7eoQPejg8t3CKkpVdpm2IlN/dZif4aGE=', 'zm85UW0YCIyBCxOXTagQQYcezjLzIQ');

INSERT INTO `cf_site` (`id`, `name`, `templateref`, `parentref`, `stylesheetref`, `javascriptref`, `htmlcompression`, `characterencoding`, `contenttype`, `locale`, `aliaspath`, `gzip`, `title`, `job`, `description`) VALUES
(1, 'root', 1, 0, NULL, NULL, 0, 'UTF-8', 'text/html', 'de', 'root', 0, 'Clownfish root', 0, 'Default root site'),
(2, 'error', 2, 0, NULL, NULL, 0, 'UTF-8', 'text/html', 'de', 'error', 0, 'Clownfish error', 0, 'Default error site');

INSERT INTO `cf_template` (`id`, `name`, `content`, `scriptlanguage`, `checkedoutby`) VALUES
(1, 'root', '<!DOCTYPE html>\r\n<html xmlns="http://www.w3.org/1999/xhtml" lang="de">\r\n   <head>\r\n     <title>${metainfo.title}</title>\r\n     <meta charset="utf-8">\r\n     <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">\r\n     <meta name="description" content="${metainfo.description}"/>\r\n     <link rel="icon" sizes="192x192" href="/images/favicon.ico">\r\n     <link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono" rel="stylesheet">\r\n     <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">\r\n     <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.9/css/mdb.min.css" rel="stylesheet">\r\n     <style>\r\n       ${css}\r\n		main { \r\n          padding-top: 30px; \r\n       	}\r\n		.bg-light {\r\n          background-color: #FFFFFF!important;\r\n          opacity: 1.0!important;\r\n        }\r\n       	.container-fluid {\r\n          padding-bottom: 10px; \r\n       	}\r\n     </style>\r\n   </head>\r\n   <body>\r\n     <nav class="navbar navbar-expand-md navbar-dark sticky-top grey darken-3 bg-dark text-white">\r\n       <div class="container">\r\n         <div class="row w-100">\r\n           <div class="col-1 align-self-center">\r\n             <img src="/images/clownfish.svg" width="72" />\r\n           </div>\r\n           <div class="col-11 align-self-center">\r\n             <h1>Clownfish CMS - Version ${metainfo.version}</h1>\r\n           </div>\r\n         </div>\r\n        </div>\r\n     </nav>\r\n     <main role="main" class="container">\r\n      <div class="jumbotron">\r\n        <h2>Clownfish Content Management System</h2>\r\n        <div class="container-fluid">\r\n          This CMS is used to implement a bandwidth from simple websites to complex portal systems. \r\n        </div>\r\n     	<h2>Features</h2>\r\n        <div class="container-fluid">\r\n          <ul class="list-unstyled">\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/spring-boot.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Spring Boot</h5>\r\n                Based upon Spring Boot framework.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/Tomcat.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Apache Tomcat</h5>\r\n                Uses embedded Apache Tomcat as application server.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/freemarker.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Freemarker</h5>\r\n                Code your templates with Freemarker.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/velocity.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Velocity</h5>\r\n                Code your templates with Velocity.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/MySQL.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">MySQL</h5>\r\n                MySQL database as content storage layer.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/SAP.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">SAP</h5>\r\n                Optional SAP integration via remote function calls.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/database.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">External datasources</h5>\r\n                Connect external databases (MSSQL Server / MySQL).\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/markdown.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Markdown content</h5>\r\n                Write content elements in markdown.\r\n              </div>\r\n            </li>\r\n            <li class="media my-4">\r\n              <img class="mr-3" src="/images/quartz.svg" width="64" />\r\n              <div class="media-body">\r\n                <h5 class="mt-0 mb-1">Quartz job scheduler</h5>\r\n                Trigger your jobs with quartz.\r\n              </div>\r\n            </li>\r\n          </ul>\r\n        </div>\r\n       </div>\r\n     </main>\r\n     <footer class="page-footer font-small grey darken-3">\r\n       <div class="container">\r\n         <div class="row">\r\n           <div class="col-md-12 py-5">\r\n             <div class="mb-5 flex-center">\r\n               <a class="git-ic" href="https://github.com/rawdog71/Clownfish" target="_blank">\r\n                 <i class="fab fa-github fa-lg white-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="tw-ic" href="https://twitter.com/ClownfishCms" target="_blank">\r\n                 <i class="fab fa-twitter fa-lg white-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="li-ic" href="https://www.linkedin.com/in/rainer-sulzbach-a59859151/" target="_blank">\r\n                 <i class="fab fa-linkedin-in fa-lg white-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="xin-ic" href="https://www.xing.com/profile/Rainer_Sulzbach/cv" target="_blank">\r\n                 <i class="fab fa-xing fa-lg white-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n             </div>\r\n           </div>\r\n         </div>\r\n       </div>\r\n       <div class="footer-copyright text-center py-3">&copy; 2019 Copyright:\r\n         <a href="https://rawdog71.github.io/Clownfish/" target="_blank">&nbsp;Clownfish</a>\r\n       </div>\r\n     </footer>\r\n     <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>\r\n     <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>\r\n     <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>\r\n     <script src="/resources/js/clownfish.js"></script>\r\n     <script>${js}</script>\r\n   </body>\r\n</html>', 0, 0),
(2, 'error', '<!DOCTYPE html>\r\n<html xmlns="http://www.w3.org/1999/xhtml" lang="de">\r\n   <head>\r\n     <title>${metainfo.title}</title>\r\n     <meta charset="utf-8">\r\n     <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">\r\n     <meta name="description" content="${metainfo.description}"/>\r\n     <link rel="icon" sizes="192x192" href="/images/favicon.ico">\r\n     <link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono" rel="stylesheet">\r\n     <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">\r\n     <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">\r\n     <link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.9/css/mdb.min.css" rel="stylesheet">\r\n     <style>\r\n       ${css}\r\n		main { \r\n          padding-top: 30px; \r\n       	}\r\n		.bg-light {\r\n          background-color: #FFFFFF!important;\r\n          opacity: 1.0!important;\r\n        }\r\n       	.container-fluid {\r\n          padding-bottom: 10px; \r\n       	}\r\n     </style>\r\n   </head>\r\n   <body>\r\n     <nav class="navbar navbar-expand-md navbar-dark sticky-top grey darken-3 text-white">\r\n       <div class="container">\r\n         <div class="row w-100">\r\n           <div class="col-1 align-self-center">\r\n             <img src="/images/clownfish-error-48.png" width="48" />\r\n           </div>\r\n           <div class="col-11 align-self-center">\r\n             <h1>Clownfish CMS - Version ${metainfo.version}</h1>\r\n           </div>\r\n         </div>\r\n        </div>\r\n     </nav>\r\n     <main role="main" class="container">\r\n      <div class="jumbotron">\r\n        <h2>Clownfish Content Management System</h2>\r\n        <div class="container-fluid text-danger">\r\n          ERROR - The site you are looking for does not exist.\r\n        </div>\r\n       </div>\r\n     </main>\r\n     <footer class="page-footer font-small grey darken-3">\r\n       <div class="container">\r\n         <div class="row">\r\n           <div class="col-md-12 py-5">\r\n             <div class="mb-5 flex-center">\r\n               <a class="git-ic" href="https://github.com/rawdog71/Clownfish" target="_blank">\r\n                 <i class="fab fa-github fa-lg white-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="tw-ic" href="https://twitter.com/ClownfishCms" target="_blank">\r\n                 <i class="fab fa-twitter fa-lg white-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="li-ic" href="https://www.linkedin.com/in/rainer-sulzbach-a59859151/" target="_blank">\r\n                 <i class="fab fa-linkedin-in fa-lg white-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n               <a class="xin-ic" href="https://www.xing.com/profile/Rainer_Sulzbach/cv" target="_blank">\r\n                 <i class="fab fa-xing fa-lg white-text mr-md-5 mr-3 fa-2x"> </i>\r\n               </a>\r\n             </div>\r\n           </div>\r\n         </div>\r\n       </div>\r\n       <div class="footer-copyright text-center py-3">&copy; 2019 Copyright:\r\n         <a href="https://rawdog71.github.io/Clownfish/" target="_blank">&nbsp;Clownfish</a>\r\n       </div>\r\n     </footer>\r\n     <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>\r\n     <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>\r\n     <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>\r\n     <script src="/resources/js/clownfish.js"></script>\r\n     <script>${js}</script>\r\n   </body>\r\n</html>', 0, 0);

INSERT INTO `cf_templateversion` (`templateref`, `version`, `content`, `tstamp`, `commitedby`) VALUES
(1, 1, 'x��Y�S۸�ܝ��A����;]�qB�Є��na����~ّm�V"[�$�Q����y8!�-�f��t�s��<�sh�������C�����SS?�8f�l�R�m�F#kT��m��h�cMc ���e�Āe�fD�_������w1Q�&����]L���4�",$Q-#S��k,O&8&-cH�(�B��"	�����O��#f��;�	U3Sz����;�����T��j%|��>�����<)�_��Dpo��-l�a-�zz��_��4�c�5P$H�2l\Z�H;�CMf�cy��N�-�\0�V�y�N��<۞�8�lҺr�DeoN9hSp�j�Q�\Z�J�+�b<���r9WR	���l>`oY���/Ƭ�����H(��\0��v�̣���箃��m�M��{q{>>���և���YT9�n�n�w�z�=\n;��o����m�.%4�I��	O&1�����Ibi�H�\\%XB�+�֮U�U�<f��`<��E���ͨ+m5�Ja��Y�&���wq��2�]����o���;z}��?���U���|~R��4	!��=T��㷋�W�"�\rMF�H���B{�P�,�M�3.�Я���\Z�HƉz[��)�����cU֒�/�Z:HAV@>`��9�Dv�R<�\rJ]�`��hڋ��t�?����!�����b���I�)�b6�c1@RQo0�x�P�	҃$1k`�	+sQE��\r�>�s�+W�_�|�F�S�,Q���La�LbJ�Ӄ���g���!��[$*�QPYr\Z�ȳ�$4{��\r��"�7�9�3��ഇLtM��t�JYxX���:��ݴ�^�s@���p\\��x�\ne��Y�% ��Dղ"�I�Nq����7��Ġ@��j���^��2�2���|�8�Ha��B.�sk�@��#=�Fĕ�lRSC��1ʣ�!��#-��WZ��*D>U�f�ft�JefIv��@���ħ�s끯�:�f�Xv^��"��Zr���D��ԡ���6m}N��\n�]�1�{97��<�5��am�,W.����1�b`=�aP��?�K{�9�jCv�*�m��\n�gDb��>@��a�p�2\n�:+H" �,v� $��]<~�9�\r�p��	��/�X<\\N"�X��p\r	���������ٺ��t����(�|6@����Ea򗎾iY�.@L(�&/�Q���s�����>�!�@2-���4���"(�/���ɗ�kf�g\0�p�<\r��)!Լ���>\Z��x\n��-�D�9�i7���مW��eAԩԇ�s��,�6\0x%8��'')n��e}Q�>gX�/πW7g���E�~�6�����!�T~\n���0�Ip5�m�8 �Ҙp!Y�\n�\0�9�]SƐ:�k�R8~�d�}ө�tb��ie��fPJl��ψC�Lꭶi`4�ܢ;�G>w{^<Ha���.��`����8�E6�-�Q^b���F���W]?kz�:6��\nt��6~L5Z�ȴ	�k2W� �O�a��T��5J���BA�M@��Y�Nf��q����Sw짩6�߄����kk���0�-<�������\n���i���e���V���X�,�k�OL������&T�:�!���''ߢj�i��<��+s�+�΢֚F2��_�!�%�L��)�t^I����~e�|/N�y{.�V�sF�$7i�j֬��X��8��׶�O�k���D}?}8�]�}�OwN��\r���۸���������m�G�N�ݦG�����6u�.d}\\��;�)OS�&}i;�S�\Zv����:��37�\Z��|r�''jX�Hvnjqx>��\Z''v�����NtE��Z���A�\\�����nu��N�����j_|�6�]l�Rҏ��*U��Qm�<�_@������u��^йI�s�9��ۓ��7;����''�DmlA��U-��߼p��]_�/O6�in��j�j°<', '2019-09-11 07:42:04', 1),
(2, 1, 'xڵW_S�8ng�;��N_��8!�@�M��pP��}�ȶl+�-W�c���V6I�4Ж^3�V�����vםW�>~��8B��?^v��Q��H��f�eF�0�M��n����P�c���D�c�N@�[��BR���w���3\n�}�,7�lj9�Ȯ�JOoi��1�HW�R�%�K\r9,�$挺2�dJ�����TR���!�Zo�8�''�d�Ge7f��D8�&���"�b{��^3"B�8	�\Zu�QA��ծ�𧡀���4�>����̀ڪ��O9[��=0@>c~HpB���t����\rg�K;�e�u�\0M�]�YHD@��6�V�]���qc�fL\n�q�Jق`n5���/iFD�K\r�-�ϩ���\07Z��q܄G�e`av}�۪5[Û�����f�ﯧσ�Q}�q���p�����]�n��vNn"�>gB0N}\Zw5�x�T��TC9gD������0m\Z-�^@�aX\0y*��1D d�녘����fHmaʌJI�^�Xð�����)͑[��2څN�����6_!�����x��E���b~	v]\Z�p��=Ԩ%��r�EqȰ}=�~ �]������Y\Z���B��П����F�&�X�W�Y�ȥ=d��,�K����`+x�S�j��l3)Y����YqD�\\�������[1�"''�n6�ژ��<��.r��	�:����9�!E$��@��R�*���}ǥ�P��U�2��V��±.%�-�!"�.H��T�5��\Z�HpgY� ײأ"�	���-#�}\r��V�ni�\\�k����C����\\;:<!].�ޢJ��$h"���!넕uǄ�-�JzΠ_h�U{"Up�4��P��j�zH�J�����z̈́$\0�WOmJ���.�ƅN������1 �x$A3�"�(dl�<Ƒˈ@1���TH�|���ʃ:C�ܾ��$UduA]M�_���dv��V%3��mUX#[o"/$�c�x��S�Sg��\05H��y��e��e.B�!���F��6�A�\r��\r�k�<��"�[裢�*�(�\nWS=j��k��h?�Df�<����a$���A�o�\r ���$qi9���R���_��:n�[Ͷմ��A��ס�>x�R�����3���\0?�\0���y�����6U�����e庖��dƋI��z��U�*�����5�\ru��o��\r��t~k���L�2�7dț����c�Zy,�]�SŷA�K�s�1��>+BZ��\r�nX�iT�i�S��I��ߝ�������������l��p2�&?j������O�''Y����y�<N���v#�I�����ǧh�x\n[�6���3aI�d,L˰�0w��;''>�����~��{��\Z��ic�^7"�".ۧ�����npI�F{���I�BL�7��z�fo~ӏ~�����hN>\rwF	ۗ���\Z�;����y28���h0�G^�:��ٗ����Ɨ�7�vw.����h�+F��;0��+�2��>����a��1D}��F��o', '2019-09-11 07:42:58', 1);
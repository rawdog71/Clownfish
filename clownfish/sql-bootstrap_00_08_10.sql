USE clownfish;
CREATE TABLE IF NOT EXISTS `cf_foldertrigger` (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `folder` varchar(512) DEFAULT NULL,
  `recursive` tinyint(3) unsigned DEFAULT NULL,
  `active` tinyint(3) unsigned DEFAULT NULL,
  `siteref` bigint(20) DEFAULT NULL,
  `parameter` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

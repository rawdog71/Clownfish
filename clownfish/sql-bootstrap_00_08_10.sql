USE clownfish;
CREATE TABLE IF NOT EXISTS `cf_foldertrigger` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `folder` varchar(512) DEFAULT NULL,
  `rekursiv` tinyint unsigned DEFAULT NULL,
  `active` tinyint unsigned DEFAULT NULL,
  `siteref` bigint DEFAULT NULL,
  `parameter` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;

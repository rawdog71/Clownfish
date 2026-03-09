USE clownfish;
CREATE TABLE IF NOT EXISTS `cf_foldertrigger` (
  `id` bigint NOT NULL,
  `name` varchar(50) NOT NULL,
  `folder` varchar(512) DEFAULT NULL,
  `recursive` tinyint unsigned DEFAULT NULL,
  `active` tinyint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;


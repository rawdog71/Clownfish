USE clownfish;

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('domain', '', 1);

ALTER TABLE `clownfish`.`cf_site` 
ADD COLUMN `sitemap` TINYINT NOT NULL DEFAULT '0' AFTER `hitcounter`;

COMMIT;
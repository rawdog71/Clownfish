USE clownfish;

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('domain', '', 1);

ALTER TABLE `clownfish`.`cf_site` 
ADD COLUMN `sitemap` TINYINT NOT NULL DEFAULT '0' AFTER `hitcounter`;

ALTER TABLE `clownfish`.`cf_asset` 
ADD COLUMN `publicuse` TINYINT NOT NULL DEFAULT '1' AFTER `scrapped`;

ALTER TABLE `clownfish`.`cf_asset` 
ADD COLUMN `uploadtime` DATETIME NULL DEFAULT NULL AFTER `publicuse`;

ALTER TABLE `clownfish`.`cf_user` 
ADD COLUMN `assetref` BIGINT NULL DEFAULT NULL AFTER `salt`;

INSERT INTO `clownfish`.`cf_webservice` (`name`) VALUES ('RestService');

COMMIT;
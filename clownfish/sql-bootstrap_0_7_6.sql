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

INSERT INTO `cf_webservice` (`id`, `name`) VALUES
(23, 'GetTemplates');

INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,23,'+4XDcVioKyA2wrDCd1f2zH5vYZ5JnzvbK5pbtSGouTA=');

COMMIT;
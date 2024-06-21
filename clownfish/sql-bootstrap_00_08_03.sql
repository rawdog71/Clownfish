USE clownfish;

ALTER TABLE `clownfish`.`cf_site`
ADD COLUMN `isloginsite` TINYINT(1) NULL DEFAULT 0 AFTER `invisible`,
ADD COLUMN `loginsiteref` BIGINT(20) NULL DEFAULT NULL AFTER `isloginsite`;

ALTER TABLE `clownfish`.`cf_class`
ADD COLUMN `loginclass` TINYINT(1) NULL DEFAULT 0 AFTER `encrypted`;

UPDATE `clownfish`.`cf_site` SET `loginsiteref` = (SELECT t.id FROM (SELECT id, name FROM `clownfish`.`cf_site`) t WHERE t.`name` = `loginsite`)
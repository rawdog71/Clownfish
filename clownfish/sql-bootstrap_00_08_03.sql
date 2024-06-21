USE clownfish;

ALTER TABLE `clownfish`.`cf_site`
ADD COLUMN `isloginsite` TINYINT(1) NULL DEFAULT 0 AFTER `invisible`,
ADD COLUMN `loginsiteref` BIGINT(20) NULL DEFAULT 0 AFTER `isloginsite`;

ALTER TABLE `clownfish`.`cf_class`
ADD COLUMN `loginclass` TINYINT(1) NULL DEFAULT 0 AFTER `encrypted`;
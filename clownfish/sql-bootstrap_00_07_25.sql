USE clownfish;

ALTER TABLE `clownfish`.`cf_site`
ADD COLUMN `invisible` TINYINT(1) NULL DEFAULT 0 AFTER `testparams`;

ALTER TABLE `clownfish`.`cf_template`
ADD COLUMN `invisible` TINYINT(1) NULL DEFAULT 0 AFTER `type`;

ALTER TABLE `clownfish`.`cf_java`
ADD COLUMN `invisible` TINYINT(1) NULL DEFAULT 0 AFTER `language`;

ALTER TABLE `clownfish`.`cf_javascript`
ADD COLUMN `invisible` TINYINT(1) NULL DEFAULT 0 AFTER `checkedoutby`;

ALTER TABLE `clownfish`.`cf_stylesheet`
ADD COLUMN `invisible` TINYINT(1) NULL DEFAULT 0 AFTER `checkedoutby`;

ALTER TABLE `clownfish`.`cf_user`
ADD COLUMN `superadmin` TINYINT(1) NULL DEFAULT 0 AFTER `assetref`;
UPDATE `clownfish`.`cf_user` SET `superadmin` = 1 WHERE `id` = 1;
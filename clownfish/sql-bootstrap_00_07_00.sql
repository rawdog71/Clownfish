USE clownfish;

ALTER TABLE `clownfish`.`cf_template` 
ADD COLUMN `layout` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `checkedoutby`;

ALTER TABLE `clownfish`.`cf_javascriptversion` 
CHANGE COLUMN `content` `content` LONGBLOB NULL DEFAULT NULL ;

ALTER TABLE `clownfish`.`cf_stylesheetversion` 
CHANGE COLUMN `content` `content` LONGBLOB NULL DEFAULT NULL ;

ALTER TABLE `clownfish`.`cf_templateversion` 
CHANGE COLUMN `content` `content` LONGBLOB NULL DEFAULT NULL ;

CREATE TABLE `clownfish`.`cf_layoutcontent` (
  `siteref` BIGINT UNSIGNED NOT NULL,
  `divref` BIGINT UNSIGNED NOT NULL,
  `contenttype` VARCHAR(2) NOT NULL,
  `lfdnr` INT UNSIGNED NOT NULL,
  `contentref` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `preview_contentref` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`siteref`, `divref`, `contenttype`, `lfdnr`));

COMMIT;

USE clownfish;

CREATE TABLE `clownfish`.`cf_accessmanager` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` INT NOT NULL DEFAULT '0',
  `ref` BIGINT NULL,
  `refclasscontent` BIGINT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

ALTER TABLE `clownfish`.`cf_accessmanager` 
ADD INDEX `idx_type` (`type` ASC) VISIBLE;
ALTER TABLE `clownfish`.`cf_accessmanager` 
ADD INDEX `idx_ref` (`ref` ASC) VISIBLE;
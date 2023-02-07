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

ALTER TABLE `clownfish`.`cf_accessmanager` ADD INDEX `idx_type` (`type` ASC);
ALTER TABLE `clownfish`.`cf_accessmanager` ADD INDEX `idx_ref` (`ref` ASC);
ALTER TABLE `clownfish`.`cf_accessmanager` ADD INDEX `idx_type_ref` (`type` ASC, `ref` ASC);
ALTER TABLE `clownfish`.`cf_accessmanager` ADD INDEX `idx_type_ref_content` (`type` ASC, `ref` ASC, `refclasscontent` ASC);
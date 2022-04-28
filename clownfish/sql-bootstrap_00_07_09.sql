USE clownfish;

ALTER TABLE `clownfish`.`cf_attributcontent` 
CHANGE COLUMN `content_string` `content_string` VARCHAR(512) NULL DEFAULT NULL ;

ALTER TABLE `clownfish`.`cf_class` 
ADD COLUMN `encrypted` TINYINT NOT NULL DEFAULT '0' AFTER `templateref`;

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('aes_key', '!cL0wNf1Sh<34eva', 1);

COMMIT;
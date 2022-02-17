USE clownfish;

ALTER TABLE `clownfish`.`cf_class` 
ADD COLUMN `templateref` BIGINT(20) NULL DEFAULT NULL AFTER `maintenance`;

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('folder_js', '', 1),
('folder_css', '', 1),
('folder_fonts', '', 1);

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('check_authtoken', 'true', 1);

COMMIT;
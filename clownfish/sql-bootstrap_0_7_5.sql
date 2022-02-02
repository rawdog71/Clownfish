USE clownfish;

ALTER TABLE `clownfish`.`cf_class` 
ADD COLUMN `templateref` BIGINT(20) NULL DEFAULT NULL AFTER `maintenance`;

INSERT INTO `cf_property` (`hashkey`, `value`, `nodelete`) VALUES
('folder_js', '', 1),
('folder_css', '', 1),
('folder_fonts', '', 1);

INSERT INTO `cf_webservice` (`id`, `name`) VALUES
(17, 'GetClasses'),
(18, 'GetKeywords'),
(19, 'GetKeywordLibraries'),
(20, 'GetAssets'),
(21, 'GetAssetLibraries');

INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,17,'MTYT/b3vjv9S2lZRcAEjbOWA1xlsctIMuajhPC5IWPU=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,18,'kGHDfDQPDrUgpKlJqhbevUf7Z83fA4JeLg2QeUOHWZ0=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,19,'ej6Ip5X3Al6od15hqBG/wAaOr8ImiMlUrtaJMzzcz/w=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,20,'e6FxlZIcpc/lexYThp2pBv+I5X0rvc5p3Psq5qJEESs=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,21,'Gq33nmeRjW2MYPCA+i354PBsc1pugAp9R7P5QXtAd2Y=');

COMMIT;
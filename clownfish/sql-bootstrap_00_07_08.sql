USE clownfish;

ALTER TABLE `clownfish`.`cf_site` 
ADD COLUMN `searchresult` TINYINT NOT NULL DEFAULT '0' AFTER `sitemap`;

UPDATE `clownfish`.`cf_site` SET `searchresult` = '1' WHERE (`name` = 'searchresult');

COMMIT;
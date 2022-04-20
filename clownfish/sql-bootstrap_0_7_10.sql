USE clownfish;

ALTER TABLE `clownfish`.`cf_site` 
ADD COLUMN `shorturl` VARCHAR(5) NULL AFTER `searchresult`,
ADD UNIQUE INDEX `shorturl_UNIQUE` (`shorturl` ASC);

COMMIT;
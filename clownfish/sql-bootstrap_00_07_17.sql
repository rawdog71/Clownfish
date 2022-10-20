USE clownfish;

UPDATE `clownfish`.`cf_site` SET `parentref` = NULL WHERE (`parentref` = 0);

COMMIT;

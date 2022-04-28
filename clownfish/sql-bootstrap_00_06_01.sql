USE clownfish;

ALTER TABLE `clownfish`.`cf_java` 
ADD COLUMN `language` INT NOT NULL DEFAULT 0 AFTER `checkedoutby`;

COMMIT;
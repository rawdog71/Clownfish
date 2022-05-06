USE clownfish;

ALTER TABLE `clownfish`.`cf_classcontent` 
CHANGE COLUMN `name` `name` VARCHAR(256) NOT NULL ;

ALTER TABLE `clownfish`.`cf_list` 
CHANGE COLUMN `name` `name` VARCHAR(256) NOT NULL ;

COMMIT;

USE clownfish;

ALTER TABLE `clownfish`.`cf_attributcontent` 
ADD INDEX `classcontent` (`classcontentref` ASC);

COMMIT;
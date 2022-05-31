USE clownfish;

CREATE TABLE `clownfish`.`cf_searchdatabase` (
  `datasource_ref` BIGINT NOT NULL,
  `tablename` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`datasource_ref`, `tablename`));

COMMIT;

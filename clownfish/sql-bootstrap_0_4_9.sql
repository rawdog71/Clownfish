USE clownfish;

INSERT INTO `cf_backend` (`id`, `name`) VALUES
(17, 'Reef'),
(18, 'Scrapyard');

INSERT INTO `cf_userbackend` (`userref`, `backendref`) VALUES
(1, 17),
(1, 18);

COMMIT;
USE clownfish;

INSERT INTO `cf_backend` (`id`, `name`) VALUES
(17, 'Reef'),
(18, 'Scrapyard');

INSERT INTO `cf_webservice` (`id`, `name`) VALUES
(11, 'InsertDatalist'),
(12, 'InsertListcontent'),
(13, 'GetDatalist'),
(14, 'UpdateContent'),
(15, 'DeleteContent'),
(16, 'DestroyContent');

INSERT INTO `cf_userbackend` (`userref`, `backendref`) VALUES
(1, 17),
(1, 18);

INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,11,'mIuG5M3jPG/Jf1q3MWe+Xc7l3lBPAmHGb9QvxSsfuGU=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,12,'vv9qPoZ8STG0huRrX+TsbDlDH+vu6NGEdpXKuNE1aA4=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,13,'v/Mz0ukm1Ru2diFZyk8pbdC9WFsE8FGxyzO2eZd1RYQ=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,14,'d+CfnbsuiGC8ofxczlWf1EfA1ZxphlxlemcKSwRqngo=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,15,'HhR1Z6l9vM38pNDQSzvl93vNKV4JxdOloaSv/ezec3g=');
INSERT INTO `cf_webserviceauth` (`user_ref`,`webservice_ref`,`hash`) VALUES (1,16,'i8NGNs1PBDNXSh/8XVamJd0Zkc6EgCsLmIVrp5YsZik=');

COMMIT;
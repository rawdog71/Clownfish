USE clownfish;

INSERT INTO `clownfish`.`cf_template` (`name`,`content`,`scriptlanguage`,`checkedoutby`,`type`,`invisible`) VALUES 
('cf_sendemail',
'<#assign to = parameter.to!>
 <#assign subject = parameter.subject!>
 <#assign body = parameter.body!>
 <#assign sendEmail = emailBean.sendRespondMail(to, subject, body)!>', 0, 0, 0, 1);

INSERT INTO `clownfish`.`cf_site`
(`name`, `templateref`, `parentref`, `stylesheetref`, `javascriptref`, `htmlcompression`, `characterencoding`, `contenttype`, `locale`, `aliaspath`, `gzip`, `title`, `job`, `description`, `staticsite`, `searchrelevant`, `hitcounter`, `sitemap`, `searchresult`, `shorturl`, `testparams`, `invisible`, `loginsite`, `offline`)
VALUES
('cf_sendemail',
(SELECT id from `clownfish`.`cf_template` WHERE name = 'cf_sendemail'),
null,
null,
null,
0,
'UTF-8',
'text/html',
'de',
'cf_sendemail',
0,
'',
1,
'',
0,
0,
0,
0,
0,
null,
'',
1,
'',
0);
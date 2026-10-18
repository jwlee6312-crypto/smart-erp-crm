USE asterisk;

CREATE TABLE IF NOT EXISTS extensions (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  context VARCHAR(40) NOT NULL,
  exten VARCHAR(40) NOT NULL,
  priority INT NOT NULL,
  app VARCHAR(40) NOT NULL,
  appdata VARCHAR(256) NOT NULL,
  UNIQUE KEY context (context, exten, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

TRUNCATE TABLE extensions;

INSERT INTO extensions (context, exten, priority, app, appdata) VALUES
('from-internal', '999', 1, 'NoOp', 'Static IVR Start'),
('from-internal', '999', 2, 'Set', 'CALLERID(num)=01032043901'),
('from-internal', '999', 3, 'Set', 'CALLERID(name)=01032043901'),
('from-internal', '999', 4, 'Answer', ''),
('from-internal', '999', 5, 'Goto', 'ivr-main,s,1'),

('from-internal', '_1XX', 1, 'Dial', 'SIP/${EXTEN},30'),
('from-internal', '_1XX', 2, 'Hangup', ''),

('ivr-main', 's', 1, 'Answer', ''),
('ivr-main', 's', 2, 'Set', 'TIMEOUT(digit)=3'),
('ivr-main', 's', 3, 'Set', 'TIMEOUT(response)=10'),
('ivr-main', 's', 4, 'Background', 'custom/01_welcome&custom/02_select_menu'),
('ivr-main', 's', 5, 'WaitExten', '10'),

('ivr-main', '1', 1, 'NoOp', 'Option 1 Selected'),
('ivr-main', '1', 2, 'Queue', 'sales_group,r'),

('ivr-main', '2', 1, 'NoOp', 'Option 2 Selected'),
('ivr-main', '2', 2, 'Queue', 'support_group,r'),

('ivr-main', '3', 1, 'NoOp', 'Option 3 Selected'),
('ivr-main', '3', 2, 'Queue', 'management_group,r'),

('ivr-main', '4', 1, 'NoOp', 'Option 4 Selected'),
('ivr-main', '4', 2, 'Playback', 'auth-thankyou'),
('ivr-main', '4', 3, 'Playback', 'beep'),
('ivr-main', '4', 4, 'System', 'curl -X POST http://backend:8080/api/crm/inbound/log-callback -H "Content-Type: application/json" -d "{\"interaction_id\":\"${UNIQUEID}\",\"keyword\":\"${CALLERID(num)}\",\"media_type\":\"call\",\"cmpycd\":\"COIT\"}"'),
('ivr-main', '4', 5, 'Hangup', '');

CREATE TABLE IF NOT EXISTS sippeers (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(40) NOT NULL,
  ipaddr VARCHAR(45) DEFAULT NULL,
  port INT DEFAULT NULL,
  regseconds INT DEFAULT NULL,
  defaultuser VARCHAR(40) DEFAULT NULL,
  fullcontact VARCHAR(80) DEFAULT NULL,
  regserver VARCHAR(20) DEFAULT NULL,
  useragent VARCHAR(20) DEFAULT NULL,
  lastms INT DEFAULT NULL,
  host VARCHAR(40) DEFAULT NULL,
  type VARCHAR(10) DEFAULT 'friend',
  context VARCHAR(40) DEFAULT 'from-internal',
  permit VARCHAR(95) DEFAULT NULL,
  deny VARCHAR(95) DEFAULT NULL,
  secret VARCHAR(40) DEFAULT NULL,
  md5secret VARCHAR(40) DEFAULT NULL,
  remotesecret VARCHAR(40) DEFAULT NULL,
  transport VARCHAR(20) DEFAULT 'udp',
  dtmfmode VARCHAR(10) DEFAULT 'auto',
  directmedia VARCHAR(10) DEFAULT 'no',
  nat VARCHAR(20) DEFAULT 'force_rport,comedia',
  callgroup VARCHAR(40) DEFAULT NULL,
  pickupgroup VARCHAR(40) DEFAULT NULL,
  language VARCHAR(5) DEFAULT NULL,
  disallow VARCHAR(200) DEFAULT 'all',
  allow VARCHAR(200) DEFAULT 'ulaw,alaw',
  insecure VARCHAR(40) DEFAULT NULL,
  trustrpid VARCHAR(3) DEFAULT 'no',
  sendrpid VARCHAR(3) DEFAULT 'no',
  callerid VARCHAR(80) DEFAULT NULL,
  amaflags VARCHAR(20) DEFAULT NULL,
  accountcode VARCHAR(20) DEFAULT NULL,
  musiconhold VARCHAR(40) DEFAULT NULL,
  regexten VARCHAR(40) DEFAULT NULL,
  cancallforward VARCHAR(3) DEFAULT 'yes',
  setvar VARCHAR(200) DEFAULT NULL,
  qualify VARCHAR(20) DEFAULT 'yes',
  UNIQUE KEY name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

TRUNCATE TABLE sippeers;

INSERT INTO sippeers (name, host, type, context, secret, dtmfmode, nat, directmedia, allow) VALUES
('101', 'dynamic', 'friend', 'from-internal', '101password', 'auto', 'force_rport,comedia', 'no', 'ulaw,alaw'),
('102', 'dynamic', 'friend', 'from-internal', '102password', 'auto', 'force_rport,comedia', 'no', 'ulaw,alaw'),
('103', 'dynamic', 'friend', 'from-internal', '103password', 'auto', 'force_rport,comedia', 'no', 'ulaw,alaw'),
('121', 'dynamic', 'friend', 'from-internal', '121password', 'auto', 'force_rport,comedia', 'no', 'ulaw,alaw'),
('122', 'dynamic', 'friend', 'from-internal', '122password', 'auto', 'force_rport,comedia', 'no', 'ulaw,alaw'),
('131', 'dynamic', 'friend', 'from-internal', '131password', 'auto', 'force_rport,comedia', 'no', 'ulaw,alaw'),
('132', 'dynamic', 'friend', 'from-internal', '132password', 'auto', 'force_rport,comedia', 'no', 'ulaw,alaw');

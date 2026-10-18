USE asterisk;
DELETE FROM extensions WHERE context IN ('cti-answer-force', 'cti-bridge', 'outbound-call');
INSERT INTO extensions (context, exten, priority, app, appdata) VALUES
('cti-answer-force', 's', 1, 'NoOp', 'Standard CTI Answer with Stereo Recording'),
('cti-answer-force', 's', 2, 'Answer', ''),
('cti-answer-force', 's', 3, 'MixMonitor', '${UNIQUEID}.wav,S'),
('cti-answer-force', 's', 4, 'SIPAddHeader', 'Call-Info:\;answer-after=0'),
('cti-answer-force', 's', 5, 'Dial', 'SIP/${AGENT_EXTEN},15,tT'),
('cti-answer-force', 's', 6, 'Hangup', ''),

('cti-bridge', 's', 1, 'NoOp', 'CTI Bridge to Agent'),
('cti-bridge', 's', 2, 'Answer', ''),
('cti-bridge', 's', 3, 'MixMonitor', '${UNIQUEID}.wav,S'),
('cti-bridge', 's', 4, 'SIPAddHeader', 'Call-Info:\;answer-after=0'),
('cti-bridge', 's', 5, 'Dial', 'SIP/${AGENT_EXTEN},30,tT'),
('cti-bridge', 's', 6, 'Hangup', ''),

('outbound-call', '_X.', 1, 'NoOp', 'Outbound Call for ${EXTEN}'),
('outbound-call', '_X.', 2, 'Set', 'DEST_NUM=${EXTEN}'),
('outbound-call', '_X.', 3, 'ExecIf', '$["${DEST_NUM}" = "01032043901"]?Set(DEST_NUM=103)'),
('outbound-call', '_X.', 4, 'Answer', ''),
('outbound-call', '_X.', 5, 'MixMonitor', '${REC_FILE}.wav,S'),
('outbound-call', '_X.', 6, 'Dial', 'SIP/${DEST_NUM},30,tT'),
('outbound-call', '_X.', 7, 'Hangup', '');

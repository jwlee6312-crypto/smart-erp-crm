USE asterisk;

DELETE FROM queues;
INSERT INTO queues (name, strategy, timeout, ringinuse, context, musiconhold) VALUES
('sales_group', 'ringall', 20, 'no', 'from-internal', 'default'),
('support_group', 'linear', 15, 'no', 'from-internal', 'default'),
('management_group', 'ringall', 20, 'no', 'from-internal', 'default');

DELETE FROM queue_members;
INSERT INTO queue_members (queue_name, interface, penalty, paused, membername) VALUES
('sales_group', 'SIP/101', 0, 0, '이정우님'),
('sales_group', 'SIP/102', 1, 0, '영업상담원102'),
('support_group', 'SIP/121', 0, 0, '지원상담원121'),
('support_group', 'SIP/122', 1, 0, '지원상담원122'),
('management_group', 'SIP/131', 0, 0, '경영상담원131'),
('management_group', 'SIP/132', 1, 0, '경영상담원132');

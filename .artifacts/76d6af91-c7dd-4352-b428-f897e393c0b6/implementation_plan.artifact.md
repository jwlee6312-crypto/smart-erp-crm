# CTI Answer Call Implementation Plan

Fix the CTI [전화받기] (Answer Call) feature so that clicking the answer button successfully answers the incoming call, stops the ringtone, starts recording, and maintains the active connection without dropping the call.

## User Review Required

> [!IMPORTANT]
> This plan addresses the root cause where Asterisk's `cti-answer-force` dialplan either caused a busy loop (when `Dial` was present) or immediate termination (when `Hangup` was present).

## Proposed Changes

### Asterisk Dialplan & Realtime Extensions

#### [MODIFY] [extensions.conf](file:///C:/smart-erp-crm/asterisk_config/extensions.conf)
- Update `[cti-answer-force]` context to execute `Answer()` and `MixMonitor()` cleanly without re-dialing or prematurely hanging up.

#### [MODIFY] [Database Extensions Table](file:///C:/smart-erp-crm/insert_extensions.sql)
- Synchronize MySQL Realtime `extensions` table with the updated `cti-answer-force` context.

### Backend CTI Service

#### [MODIFY] [AsteriskService.java](file:///C:/smart-erp-crm/erp-backend/src/main/java/com/crmbank/erp/crm/service/AsteriskService.java)
- Ensure `answerCall` correctly retrieves the agent's ringing channel and redirects it to `cti-answer-force`.

## Verification Plan

### Automated / Manual Verification
- Deploy backend and Asterisk configurations.
- Reload Asterisk dialplan (`dialplan reload`).
- Test incoming call popup -> Click [전화받기] -> Verify ringtone stops, call connects successfully, and recording starts.

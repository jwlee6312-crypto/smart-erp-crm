# Multi-Instance Asterisk & ERP/CRM Deployment Architecture

This document provides a comprehensive architectural proposal for standardizing and modularizing the multi-instance deployment of Asterisk PBX and the Smart ERP CRM backend. The goal is to allow administrators to deploy a fully isolated, working instance simply by editing a single environment configuration file (`.env`) and configuration templates.

---

## 1. Architectural Strategy: Single `.env` + Template-Driven Provisioning

To avoid hardcoding ports, IP addresses, and database credentials across multiple files, we introduce a centralized **Environment Variables (`.env`)** file combined with Docker Compose variable substitution and SQL bootstrapping.

### Centralized `.env` Template
```env
# ==========================================
# Multi-Instance PBX & CRM Deployment Profile
# ==========================================
INSTANCE_NAME=crm-instance-02
COMPANY_CODE=COIT

# Network & Ports
SIP_EXTERNAL_PORT=5062
SIP_INTERNAL_PORT=5060
AMI_EXTERNAL_PORT=5039
AMI_INTERNAL_PORT=5038
RTP_PORT_START=10200
RTP_PORT_END=10300

# Database Configuration
DB_HOST=172.30.1.11
DB_PORT=3308
DB_NAME_ERP=smartcrm
DB_NAME_ASTERISK=asterisk
DB_USER=root
DB_PASSWORD=Crmbank123!

# Asterisk Manager Credentials
ASTERISK_AMI_USER=admin
ASTERISK_AMI_SECRET=Crmbank123!
```

---

## 2. File-Based Configuration Structure

### A. `docker-compose.yml` (Dynamic Port & Range Binding)
Using environment variables in `docker-compose.yml` allows dynamic port assignment for each instance:

```yaml
services:
  asterisk:
    image: smart-erp-crm-asterisk:latest
    container_name: asterisk-${INSTANCE_NAME}
    ports:
      - "${SIP_EXTERNAL_PORT}:${SIP_INTERNAL_PORT}/udp"
      - "${AMI_EXTERNAL_PORT}:${AMI_INTERNAL_PORT}/tcp"
      - "${RTP_PORT_START}-${RTP_PORT_END}:${RTP_PORT_START}-${RTP_PORT_END}/udp"
    environment:
      - TZ=Asia/Seoul
    volumes:
      - ./asterisk_config:/etc/asterisk
      - /data/${INSTANCE_NAME}/sounds:/var/lib/asterisk/sounds/custom:rw
      - /data/${INSTANCE_NAME}/recordings:/var/spool/asterisk/monitor:rw

  backend:
    image: smart-erp-crm-backend:latest
    container_name: backend-${INSTANCE_NAME}
    environment:
      - ASTERISK_HOST=${DB_HOST}
      - ASTERISK_PORT=${AMI_EXTERNAL_PORT}
      - ASTERISK_USERNAME=${ASTERISK_AMI_USER}
      - ASTERISK_PASSWORD=${ASTERISK_AMI_SECRET}
      - ERP_DB_HOST=${DB_HOST}
      - ERP_DB_PORT=${DB_PORT}
    ports:
      - "8090:8080"
```

### B. `rtp.conf` (Template with Environment Substitution)
```ini
[general]
rtpstart=${RTP_PORT_START}
rtpend=${RTP_PORT_END}
```

### C. `sip.conf` (Template)
```ini
[general]
context=from-internal
bindport=${SIP_INTERNAL_PORT}
bindaddr=0.0.0.0
transport=udp
externaddr=${DB_HOST}:${SIP_EXTERNAL_PORT}
localnet=172.18.0.0/255.255.0.0
nat=force_rport,comedia
directmedia=no
dtmfmode=rfc2833
relaxdtmf=yes
qualify=yes
```

---

## 3. Database-Based Configuration Structure

For dynamic IVR scripts, extensions, and agent endpoints, MySQL database tables (`asterisk` database) serve as the single source of truth.

### A. `extensions` Table (Dynamic Dialplan & IVR Routing)
```sql
CREATE TABLE IF NOT EXISTS `extensions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `context` VARCHAR(40) DEFAULT NULL,
  `exten` VARCHAR(40) DEFAULT NULL,
  `priority` INT DEFAULT NULL,
  `app` VARCHAR(40) DEFAULT NULL,
  `appdata` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- Default IVR and Queue routing
INSERT INTO `extensions` (`context`, `exten`, `priority`, `app`, `appdata`) VALUES
('ivr-main', 's', 1, 'Answer', ''),
('ivr-main', 's', 2, 'Background', '/var/lib/asterisk/sounds/custom/01_welcome'),
('ivr-main', 's', 3, 'Background', '/var/lib/asterisk/sounds/custom/02_select_menu'),
('ivr-main', 's', 4, 'WaitExten', '10'),
('ivr-main', '1', 1, 'NoOp', 'Option 1 Selected'),
('ivr-main', '1', 2, 'Queue', 'sales_group,r');
```

### B. `sippeers` Table (Agent Extension Registry)
```sql
CREATE TABLE IF NOT EXISTS `sippeers` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(40) NOT NULL,
  `host` VARCHAR(40) DEFAULT 'dynamic',
  `type` VARCHAR(20) DEFAULT 'friend',
  `context` VARCHAR(40) DEFAULT 'from-internal',
  `secret` VARCHAR(40) DEFAULT NULL,
  `dtmfmode` VARCHAR(10) DEFAULT 'rfc2833',
  `allow` VARCHAR(100) DEFAULT 'ulaw,alaw',
  PRIMARY KEY (`id`)
);

INSERT INTO `sippeers` (`name`, `secret`, `context`, `dtmfmode`, `allow`) VALUES
('101', '101password', 'from-internal', 'rfc2833', 'ulaw,alaw'),
('102', '102password', 'from-internal', 'rfc2833', 'ulaw,alaw'),
('103', '103password', 'from-internal', 'rfc2833', 'ulaw,alaw');
```

---

## 4. Summary & Implementation Recommendation

1. **Centralized Environment Control**: Use a single `.env` file to manage all unique parameters (ports, RTP ranges, company codes).
2. **Database-Driven Flexibility**: Store extensions, IVR menus, and agent endpoints in MySQL (`asterisk` DB) so that dialplan changes do not require rebuilding containers.
3. **Automated Provisioning Script**: Write a shell provisioning script (`deploy-instance.sh`) that reads the `.env` file, replaces template tokens, initializes MySQL tables, and brings up the Docker Compose stack.

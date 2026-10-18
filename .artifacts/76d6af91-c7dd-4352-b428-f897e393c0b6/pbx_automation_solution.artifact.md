# PBX Multi-Instance Automation Solution (Items 1, 2, & 4)

This document provides a fully automated template-substitution pipeline for Asterisk multi-instance deployments, satisfying your requirements:
1. **Central Environment Variables (`.env`)**
2. **Automatic Template Injection into Asterisk Config Files**
4. **SIP Trunk Parameterization via Variables**
*(Item 3: Database setup is left to manual management as requested).*

---

## 1. Central Environment File (`instance.env`)
Create this file in your project root. It includes all ports, RTP ranges, and **SIP Trunk variables**.

```env
# ==========================================
# PBX Instance Configuration & Trunk Profile
# ==========================================
INSTANCE_NAME=crm-instance-01

# Network & Ports
SIP_EXTERNAL_PORT=5062
SIP_INTERNAL_PORT=5060
AMI_EXTERNAL_PORT=5039
AMI_INTERNAL_PORT=5038
RTP_PORT_START=10200
RTP_PORT_END=10300

# Asterisk Manager Credentials
AMI_USER=admin
AMI_SECRET=Crmbank123!

# SIP Trunk Information (Item 4)
TRUNK_NAME=twilio-trunk
TRUNK_HOST=crm-bank-erp.pstn.tokyo.twilio.com
TRUNK_USER=my_twilio_user
TRUNK_SECRET=my_twilio_secret
TRUNK_OUTBOUND_NUM=+1234567890
```

---

## 2. Configuration Templates (Stored in `asterisk_config/templates/`)

### A. `sip.conf.template` (Including SIP Trunk & Dynamic Ports)
```ini
[general]
context=from-internal
bindport=${SIP_INTERNAL_PORT}
bindaddr=0.0.0.0
transport=udp
externaddr=172.30.1.11:${SIP_EXTERNAL_PORT}
localnet=172.18.0.0/255.255.0.0
nat=force_rport,comedia
directmedia=no
dtmfmode=rfc2833
relaxdtmf=yes
qualify=yes

; --- SIP Trunk Definition (Item 4) ---
[${TRUNK_NAME}]
type=peer
host=${TRUNK_HOST}
username=${TRUNK_USER}
secret=${TRUNK_SECRET}
fromuser=${TRUNK_USER}
context=from-trunk
insecure=invite,port
qualify=yes
```

### B. `rtp.conf.template` (Dynamic RTP Range)
```ini
[general]
rtpstart=${RTP_PORT_START}
rtpend=${RTP_PORT_END}
```

### C. `manager.conf.template` (Dynamic AMI Port & Credentials)
```ini
[general]
enabled = yes
port = ${AMI_INTERNAL_PORT}
bindaddr = 0.0.0.0

[${AMI_USER}]
secret = ${AMI_SECRET}
read = all
write = all
permit = 0.0.0.0/0
```

---

## 3. Automation Script (`provision-pbx.sh`) (Item 2)

This script reads `instance.env`, processes the templates using `envsubst` (or Python), and writes the final configuration files directly into `/etc/asterisk/`.

```bash
#!/bin/bash
# Load environment variables
set -a
source instance.env
set +a

CONFIG_DIR="./asterisk_config"
TEMPLATE_DIR="$CONFIG_DIR/templates"

echo "🚀 [PBX Provisioning] Generating configs for instance: $INSTANCE_NAME"

# Process templates using python for cross-platform robustness
python3 - <<EOF
import os

env = os.environ
template_dir = "$TEMPLATE_DIR"
config_dir = "$CONFIG_DIR"

templates = {
    "sip.conf.template": "sip.conf",
    "rtp.conf.template": "rtp.conf",
    "manager.conf.template": "manager.conf"
}

for tmpl, dest in templates.items():
    tmpl_path = os.path.join(template_dir, tmpl)
    dest_path = os.path.join(config_dir, dest)

    if not os.path.exists(tmpl_path):
        print(f"⚠️ Template not found: {tmpl_path}")
        continue

    with open(tmpl_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Replace \${VAR} placeholders with environment variables
    for key, val in env.items():
        content = content.replace(f"\${{ {key} }}", val).replace(f"\${{{key}}}", val)

    with open(dest_path, "w", encoding="utf-8") as f:
        f.write(content)

    print(f"✅ Generated: {dest} from {tmpl}")
EOF

echo "✨ PBX configuration provisioning completed successfully!"
```

---

## 4. Integration with Docker Compose

You can invoke `provision-pbx.sh` automatically before bringing up the container stack:

```yaml
  asterisk:
    build:
      context: .
      dockerfile: Dockerfile.asterisk
    image: smart-erp-crm-asterisk:latest
    container_name: asterisk-${INSTANCE_NAME}
    env_file:
      - instance.env
    ports:
      - "${SIP_EXTERNAL_PORT}:${SIP_INTERNAL_PORT}/udp"
      - "${AMI_EXTERNAL_PORT}:${AMI_INTERNAL_PORT}/tcp"
      - "${RTP_PORT_START}-${RTP_PORT_END}:${RTP_PORT_START}-${RTP_PORT_END}/udp"
    volumes:
      - ./asterisk_config:/etc/asterisk
```

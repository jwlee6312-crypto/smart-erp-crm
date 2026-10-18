# Smart ERP CRM - PBX Multi-Instance Operation Manual

This manual outlines the standard operating procedure (SOP) for provisioning, configuring, and managing multiple isolated Asterisk PBX instances within a single codebase using centralized environment profiles and automated scripts.

---

## 1. Architecture Overview

Instead of hardcoding configurations across multiple `.conf` files, the system uses a **Template-Driven Provisioning Pipeline**:
1. **Environment Profile (`*.env`)**: Centralizes all ports, RTP ranges, AMI credentials, and SIP Trunk settings.
2. **Configuration Templates (`asterisk_config/templates/*.template`)**: Reusable skeleton files with `${VARIABLE}` placeholders.
3. **Provisioning Script (`provision-pbx.sh`)**: Automatically injects environment variables into templates to generate final Asterisk configuration files (`sip.conf`, `rtp.conf`, `manager.conf`).

---

## 2. Directory Structure

```text
/smart-erp-crm
├── instance.env                  # Default instance environment variables
├── instance-02.env               # Optional: Additional instance profile
├── provision-pbx.sh              # Automated configuration provisioning script
├── docker-compose.yml            # Multi-container orchestration
└── asterisk_config/
    ├── templates/                # Skeleton configuration templates
    │   ├── sip.conf.template
    │   ├── rtp.conf.template
    │   └── manager.conf.template
    ├── sip.conf                  # Generated automatically
    ├── rtp.conf                  # Generated automatically
    └── manager.conf              # Generated automatically
```

---

## 3. Step-by-Step Operation Guide

### Step 1: Define Environment Variables (`instance.env`)
Create or modify an environment file in the project root. For instance, `instance.env`:

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

# SIP Trunk Information
TRUNK_NAME=twilio-trunk
TRUNK_HOST=crm-bank-erp.pstn.tokyo.twilio.com
TRUNK_USER=my_twilio_user
TRUNK_SECRET=my_twilio_secret
```

### Step 2: Run the Provisioning Script
Execute the automation script, optionally passing a custom environment profile as an argument:

```bash
# Using default 'instance.env'
./provision-pbx.sh

# Using a custom profile (e.g., for multi-instance scaling)
./provision-pbx.sh instance-02.env
```

### Step 3: Restart the Asterisk Container
Apply the newly generated configurations to the PBX container:

```bash
sudo docker compose restart asterisk
```

---

## 4. Scaling to Multiple Instances (Multi-Instance Workflow)

To deploy an additional isolated PBX instance without creating a separate project folder:
1. Copy the default environment template:
   ```bash
   cp instance.env instance-02.env
   ```
2. Edit `instance-02.env` and assign **non-overlapping ports and RTP ranges**:
   ```env
   INSTANCE_NAME=crm-instance-02
   SIP_EXTERNAL_PORT=5064
   AMI_EXTERNAL_PORT=5040
   RTP_PORT_START=10400
   RTP_PORT_END=10500
   ```
3. Run the provisioning script with the new profile:
   ```bash
   ./provision-pbx.sh instance-02.env
   ```
4. Update `docker-compose.yml` (or spin up a separate compose stack) for the second instance and restart.

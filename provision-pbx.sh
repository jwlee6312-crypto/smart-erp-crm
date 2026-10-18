#!/bin/bash
# Accept custom env file as argument, default to 'instance.env'
ENV_FILE=${1:-instance.env}

if [ ! -f "$ENV_FILE" ]; then
    echo "❌ Error: Environment file '$ENV_FILE' not found!"
    exit 1
fi

# Load environment variables
set -a
source "$ENV_FILE"
set +a

CONFIG_DIR="./asterisk_config"
TEMPLATE_DIR="$CONFIG_DIR/templates"

echo "🚀 [PBX Provisioning] Generating configs for instance: $INSTANCE_NAME using profile: $ENV_FILE"

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

    for key, val in env.items():
        content = content.replace(f"\${{{key}}}", val)

    with open(dest_path, "w", encoding="utf-8") as f:
        f.write(content)

    print(f"✅ Generated: {dest} from {tmpl}")
EOF

echo "✨ PBX configuration provisioning completed successfully!"

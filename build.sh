#!/bin/sh
set -e
cd "$(dirname "$0")"
RTR_HOME="$HOME/.local/share/Steam/steamapps/common/Rise to Ruins"
mvn package -q "-Drtr.home=$RTR_HOME" && echo "[RtRQoL] Build OK"

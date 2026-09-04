#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up bash script
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ]; then
  if [ -f /etc/mavenrc ]; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ]; then
    . "$HOME/.mavenrc"
  fi
fi

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

if [ -f "/usr/bin/mvn" ]; then
    exec /usr/bin/mvn "$@"
fi

if [ -f "/usr/local/bin/mvn" ]; then
    exec /usr/local/bin/mvn "$@"
fi

echo "Maven executable not found on system PATH."
exit 1

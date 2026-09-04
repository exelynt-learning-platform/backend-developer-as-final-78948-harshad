#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up bash script
# ----------------------------------------------------------------------------
DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_DIR="$DIR/.mvn/apache-maven-3.9.6"

if [ -f "$MAVEN_DIR/bin/mvn" ]; then
    exec "$MAVEN_DIR/bin/mvn" "$@"
fi

echo "Downloading Apache Maven..."
mkdir -p "$DIR/.mvn"
curl -sSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip" -o "$DIR/.mvn/maven.zip"
unzip -q "$DIR/.mvn/maven.zip" -d "$DIR/.mvn"
rm "$DIR/.mvn/maven.zip"
chmod +x "$MAVEN_DIR/bin/mvn"

exec "$MAVEN_DIR/bin/mvn" "$@"

#!/bin/sh
MAVEN_HOME="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists/apache-maven-3.9.11"
MAVEN_DIR="$MAVEN_HOME/apache-maven-3.9.11"
if [ ! -x "$MAVEN_DIR/bin/mvn" ]; then
  echo "Maven Wrapper: download Maven 3.9.11 on first run."
  mkdir -p "$MAVEN_HOME"
  curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip" -o "$MAVEN_HOME/maven.zip" || exit 1
  unzip -q -o "$MAVEN_HOME/maven.zip" -d "$MAVEN_HOME" || exit 1
  rm "$MAVEN_HOME/maven.zip"
fi
exec "$MAVEN_DIR/bin/mvn" "$@"

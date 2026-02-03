#!/bin/sh
set -e

echo "=== ci_post_clone.sh ==="

# Xcode CloudにはJDKが入っていないため、Homebrewでインストール
# Shared Framework (Kotlin) のビルドにGradle → JDKが必要
echo "=== Installing JDK 17 ==="
brew install --quiet openjdk@17
export JAVA_HOME=$(brew --prefix openjdk@17)
export PATH="$JAVA_HOME/bin:$PATH"
echo "JAVA_HOME=$JAVA_HOME"
java -version

echo "=== ci_post_clone.sh done ==="

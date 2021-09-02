#!/bin/bash

cd /inspector/
echo "$(pwd -P)"

echo "INPUT_ROOT=$INPUT_ROOT)"
echo "GITHUB_WORKSPACE=$GITHUB_WORKSPACE)"

/inspector/gradlew run -q

echo 'REPORT<<EOF' >> $GITHUB_ENV
cat /inspector/report.md >> $GITHUB_ENV
echo 'EOF' >> $GITHUB_ENV

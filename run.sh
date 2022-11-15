#!/bin/bash

cd /inspector/
echo "$(pwd -P)"

echo "INPUT_ROOT=$INPUT_ROOT)"
echo "GITHUB_WORKSPACE=$GITHUB_WORKSPACE)"

java -jar /inspector/build/libs/Inspector.jar

echo 'REPORT<<EOF' >> $GITHUB_ENV
cat /inspector/report.md >> $GITHUB_ENV
echo 'EOF' >> $GITHUB_ENV

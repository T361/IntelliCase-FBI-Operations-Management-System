#!/usr/bin/env bash
set -euo pipefail

# IntelliCase full run sequence
mvn -q -DskipTests org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=com.intellicase.data.SchemaInitializer
mvn -q test
mvn -q javafx:run

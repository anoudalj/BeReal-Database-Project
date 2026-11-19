#!/bin/bash
# Run from anywhere: ./java/run.sh
cd "$(dirname "$0")"
if [ -z "$DB_PASSWORD" ]; then
  read -s -p "MySQL root password: " DB_PASSWORD; echo
  export DB_PASSWORD
fi
rm -f *.class
javac *.java && java Main

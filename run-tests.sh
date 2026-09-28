#!/bin/sh
# Compila e executa os testes. Requer JDK 17+ (javac).
mkdir -p out && javac -encoding UTF-8 -d out src/blockcert/*.java test/blockcert/*.java && java -cp out blockcert.TestRunner

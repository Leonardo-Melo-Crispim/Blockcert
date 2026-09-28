#!/bin/sh
# Compila e inicia o servidor em http://localhost:8080 (execute a partir da pasta BlockCert). Requer JDK 17+.
mkdir -p out && javac -encoding UTF-8 -d out src/blockcert/*.java && java -cp out blockcert.BlockCertServer

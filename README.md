# BlockCert — Certificação Acadêmica com Blockchain local (Java)

## Requisitos
JDK 17 ou superior (com `javac`). Nenhuma dependência externa (sem Maven, sem banco de dados).

## Executar
Linux/Mac:

    ./run.sh          # abre em http://localhost:8080

Windows:

    mkdir out
    javac -encoding UTF-8 -d out src/blockcert/*.java
    java -cp out blockcert.BlockCertServer

## Testes
    ./run-tests.sh

## Estrutura
    src/blockcert/Block.java                    (professor)  bloco, SHA-256, nonce, PoW
    src/blockcert/Blockchain.java               (professor)  cadeia, gênesis, validação
    src/blockcert/Utils.java                    (criada)     zeros(n) para o PoW
    src/blockcert/Certificate.java              modelo do certificado
    src/blockcert/CertificateSmartContract.java SMART CONTRACT (regras + estado)
    src/blockcert/BlockCertServer.java          backend HTTP (API REST)
    web/index.html                              interface
    test/blockcert/TestRunner.java              testes automatizados

## API
    GET  /api/certificates            GET  /api/certificates/{id}
    POST /api/certificates            POST /api/certificates/{id}/cancel
    GET  /api/blockchain              GET  /api/blockchain/valid      GET /api/summary

Alterações nas classes do professor: apenas `getNonce()` em `Block` e `getBlocks()` (somente leitura) em `Blockchain`, para a interface exibir os dados.

# ENTENDENDO O PROJETO

1. **Blockchain**: lista de registros ligados em ordem, em que cada um "amarra" o anterior por um hash. Alterar um registro antigo quebra a ligação e é detectado.
2. **Bloco** (`Block`): guarda `index`, `timestamp`, `data`, `previousHash`, `hash` e `nonce`. No BlockCert, `data` guarda a operação, ex.: `CERTIFICATE_REGISTERED;id=CERT-001;...`.
3. **Hash**: "impressão digital" SHA-256 do bloco (`calculateHash`), calculada sobre index + timestamp + previousHash + data + nonce. Qualquer mudança nesses campos muda o hash por completo.
4. **previousHash**: hash do bloco anterior; é o elo da corrente. O bloco #2 guarda o hash do #1.
5. **nonce**: número que o Proof of Work incrementa até o hash sair no formato exigido.
6. **Proof of Work** (`proofOfWork`): repete o cálculo aumentando o nonce até o hash começar com N zeros (aqui N = 3, em `new Blockchain(3)`). Dá trabalho criar um bloco, mas é fácil verificá-lo.
7. **Bloco gênesis**: bloco 0, criado no construtor da `Blockchain`, com previousHash `null` e data "Block gênesis".
8. **Integridade** (`isBlockChainValid`): valida o gênesis e, para cada bloco, confere (a) índice sequencial, (b) `previousHash` igual ao hash do anterior, (c) hash recalculado igual ao hash guardado.
9. **Smart Contract**: programa com regras de negócio que decide quais operações são aceitas e mantém o estado. Aqui é uma classe Java, não Solidity.
10. **Por que `CertificateSmartContract` é o Smart Contract**: é a única porta de entrada para alterar certificados; aplica as regras, mantém o estado e só grava na Blockchain o que é válido.
11. **Regras**: (1) só emissor autorizado (`ADMIN`) registra; (2) ID único; (3) qualquer pessoa consulta; (4) só emissor autorizado cancela; (5) cancelado não volta a VALID (não existe método para isso; cancelar de novo dá erro); (6) não cancela inexistente; (7) violação retorna erro claro. Extra: campos vazios ou com `;` são rejeitados, para ninguém forjar campos dentro do texto do bloco.
12. **Estado**: um `Map<String, Certificate>` em memória dentro do contrato, com o status atual (VALID/CANCELLED). É reiniciado quando o servidor reinicia.
13. **Operação vira bloco**: o contrato valida, atualiza o estado, monta o texto da operação, chama `blockchain.newBlock(data)` e `addBlock` (que faz o PoW e adiciona).
14. **Rejeição**: o método retorna um `Result` de erro e para. Nenhum bloco é criado e o estado não muda.
15. **Cancelamento**: muda o status para CANCELLED e cria um NOVO bloco `CERTIFICATE_CANCELLED;id=CERT-001;issuer=ADMIN`. O bloco do registro continua lá.
16. **Na Blockchain ficam**: as operações (registro e cancelamento) com id, aluno, curso, instituição, data e emissor, mais index, timestamp, hashes e nonce. O status "atual" é mantido pelo contrato e deduzido do histórico.
17. **Blockchain x Smart Contract**: a Blockchain é o livro-razão que guarda e valida o histórico. O Smart Contract é o "porteiro" com as regras (decide o que entra).
18. **Backend** (`BlockCertServer`): recebe requisições HTTP, chama o contrato e devolve JSON. Não tem regra de negócio.
19. **Interface** (`web/index.html`): formulários e visualização; só chama a API.
20. **Comunicação**: Navegador → HTTP → `BlockCertServer` → `CertificateSmartContract` → `Blockchain.addBlock()` → `Block` (hash + PoW) → resposta JSON → interface atualiza.

**Sobre o hash do documento**: não foi adicionado. Só seria necessário para provar a autenticidade de um arquivo (ex.: o PDF); aqui verificamos o registro, não um arquivo.

# POSSÍVEIS PERGUNTAS DO PROFESSOR

1. **Por que usar Blockchain?** O histórico de emissões e cancelamentos fica encadeado por hashes; alterar um registro antigo é detectável.
2. **Por que não só um banco de dados?** Em um banco, quem tem acesso altera ou apaga uma linha sem deixar rastro. Na Blockchain, mudar um bloco invalida a cadeia (o teste 10 mostra isso). Limitação honesta: nossa cadeia é local, de um único nó; a confiança total exigiria vários nós.
3. **O que é o Smart Contract?** A camada de regras de negócio que valida operações e mantém o estado.
4. **Onde está no código?** `CertificateSmartContract.java`.
5. **Quais são as regras?** As listadas no item 11.
6. **Alguém sem permissão tenta registrar?** Retorna `ERRO: Usuário não autorizado.` (HTTP 403) e nenhum bloco é criado.
7. **Alguém altera um bloco?** O hash recalculado deixa de bater com o guardado, ou o previousHash do bloco seguinte deixa de bater; `isBlockChainValid()` retorna `false`.
8. **O que é hash?** O SHA-256 dos dados do bloco; identifica o conteúdo de forma única.
9. **Para que serve o previousHash?** Liga cada bloco ao anterior e impede trocar um bloco no meio da cadeia.
10. **Para que serve o nonce?** É o valor que o Proof of Work varia até achar um hash com os zeros exigidos.
11. **O que é Proof of Work?** O loop em `Block.proofOfWork` que procura o nonce até o hash começar com 3 zeros.
12. **O que fica na Blockchain?** O texto das operações no campo `data`, mais índice, timestamp, hash, previousHash e nonce.
13. **O que acontece no cancelamento?** O estado vira CANCELLED e um novo bloco de cancelamento é adicionado.
14. **Por que não apagamos o certificado?** Para preservar o histórico auditável; blocos não são apagados nem editados.
15. **Como a Blockchain sabe que está válida?** `isBlockChainValid()` confere gênesis, índices, previousHash e hash recalculado de cada bloco.
16. **Por que a operação inválida não gera bloco?** O contrato rejeita antes de chamar `addBlock`; só operações válidas chegam à Blockchain.
17. **Os dados persistem?** Não. Contrato e Blockchain ficam em memória; ao reiniciar o servidor, tudo recomeça do bloco gênesis (escolha de simplicidade).
18. **A autorização é segura?** É uma simulação didática: o emissor é um texto (`ADMIN`), sem senha nem assinatura digital. Em um sistema real usaríamos chaves criptográficas.

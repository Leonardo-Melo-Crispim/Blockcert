package blockcert;

import java.lang.reflect.Field;

/** Testes automatizados (sem dependências externas). Execute com run-tests.sh. */
public class TestRunner {
    static int ok = 0, fail = 0;

    static void check(String name, boolean cond) {
        System.out.println((cond ? "[PASSOU] " : "[FALHOU] ") + name);
        if (cond) ok++; else fail++;
    }

    static CertificateSmartContract novo(Blockchain bc) { return new CertificateSmartContract(bc); }

    static CertificateSmartContract.Result reg(CertificateSmartContract c, String id, String issuer) {
        return c.register(id, "Joao Silva", "Sistemas de Informacao", "BlockCert Academy", "2026-09-27", issuer);
    }

    public static void main(String[] args) throws Exception {
        Blockchain bc = new Blockchain(2);
        CertificateSmartContract c = novo(bc);

        var r = reg(c, "CERT-001", "ADMIN");
        check("1 Registro válido: SUCESSO + novo bloco", r.success() && bc.getBlocks().size() == 2);

        int blocos = bc.getBlocks().size();
        r = reg(c, "CERT-001", "ADMIN");
        check("2 ID duplicado: ERRO: Certificado já existe.", !r.success() && r.message().equals("ERRO: Certificado já existe."));

        r = reg(c, "CERT-002", "HACKER");
        check("3 Registro sem permissão: ERRO: Usuário não autorizado.", !r.success() && r.message().equals("ERRO: Usuário não autorizado."));
        check("3b Operação rejeitada NÃO gera bloco", bc.getBlocks().size() == blocos && c.query("CERT-002") == null);

        check("4 Consulta válida: encontrado com VALID", c.query("CERT-001") != null && c.query("CERT-001").getStatus() == Certificate.Status.VALID);
        check("5 Consulta inexistente: null (API: ERRO: Certificado não encontrado.)", c.query("CERT-999") == null);

        r = c.cancel("CERT-001", "ADMIN");
        check("6 Cancelamento autorizado: SUCESSO + CANCELLED + novo bloco",
                r.success() && c.query("CERT-001").getStatus() == Certificate.Status.CANCELLED && bc.getBlocks().size() == blocos + 1);

        r = c.cancel("CERT-001", "HACKER");
        check("7 Cancelamento sem permissão: ERRO: Usuário não autorizado.", !r.success() && r.message().equals("ERRO: Usuário não autorizado."));

        r = c.cancel("CERT-001", "ADMIN");
        check("8 Cancelamento duplicado: ERRO: Certificado já está cancelado.", !r.success() && r.message().equals("ERRO: Certificado já está cancelado."));

        r = c.cancel("CERT-999", "ADMIN");
        check("8b Cancelar inexistente: ERRO: Certificado não encontrado.", !r.success() && r.message().equals("ERRO: Certificado não encontrado."));

        r = c.register("", "A", "B", "C", "2026-01-01", "ADMIN");
        check("8c Entrada inválida (ID vazio) rejeitada", !r.success() && r.code() == 400);

        check("8d Histórico preservado: 3 blocos (gênesis, registro, cancelamento)", bc.getBlocks().size() == 3
                && bc.getBlocks().get(1).getData().startsWith("CERTIFICATE_REGISTERED")
                && bc.getBlocks().get(2).getData().startsWith("CERTIFICATE_CANCELLED"));

        check("9 Blockchain válida: isBlockChainValid() == true", bc.isBlockChainValid());

        // Extra: adulteração de um bloco deve invalidar a cadeia (usa reflection só no teste)
        Field f = Block.class.getDeclaredField("data");
        f.setAccessible(true);
        f.set(bc.getBlocks().get(1), "CERTIFICATE_REGISTERED;id=CERT-001;student=FALSIFICADO");
        check("10 Bloco adulterado: isBlockChainValid() == false", !bc.isBlockChainValid());

        System.out.println("\nResultado: " + ok + " passaram, " + fail + " falharam.");
        System.exit(fail == 0 ? 0 : 1);
    }
}

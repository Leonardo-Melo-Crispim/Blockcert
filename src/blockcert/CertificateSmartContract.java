package blockcert;

import java.util.*;

/**
 * SMART CONTRACT do BlockCert.
 * Recebe operações, aplica as regras de negócio, mantém o estado dos certificados
 * e só grava na Blockchain as operações válidas.
 */
public class CertificateSmartContract {

    /** Emissores autorizados (Regra 1 e 4). */
    public static final Set<String> AUTHORIZED_ISSUERS = Set.of("ADMIN");

    /** Resultado de uma operação; code segue o padrão HTTP (200, 400, 403, 404, 409). */
    public record Result(boolean success, int code, String message) {
        static Result ok(String m) { return new Result(true, 200, m); }
        static Result error(int c, String m) { return new Result(false, c, m); }
    }

    private final Blockchain blockchain;
    private final Map<String, Certificate> state = new LinkedHashMap<>(); // estado atual

    public CertificateSmartContract(Blockchain blockchain) { this.blockchain = blockchain; }

    public synchronized Result register(String id, String student, String course,
                                        String institution, String date, String issuer) {
        if (!isAuthorized(issuer)) return Result.error(403, "ERRO: Usuário não autorizado.");        // Regra 1
        if (invalid(id) || invalid(student) || invalid(course) || invalid(institution) || invalid(date))
            return Result.error(400, "ERRO: Campos obrigatórios ausentes ou com caracteres inválidos (; ou quebra de linha).");
        id = id.trim();
        if (state.containsKey(id)) return Result.error(409, "ERRO: Certificado já existe.");        // Regra 2

        state.put(id, new Certificate(id, student.trim(), course.trim(), institution.trim(), date.trim(), issuer.trim()));
        writeToBlockchain("CERTIFICATE_REGISTERED;id=" + id + ";student=" + student.trim() + ";course=" + course.trim()
                + ";institution=" + institution.trim() + ";date=" + date.trim() + ";issuer=" + issuer.trim());
        return Result.ok("Certificado registrado com sucesso.");
    }

    /** Regra 3: qualquer pessoa pode consultar. Retorna null se não existir. */
    public synchronized Certificate query(String id) {
        return id == null ? null : state.get(id.trim());
    }

    public synchronized Result cancel(String id, String issuer) {
        if (!isAuthorized(issuer)) return Result.error(403, "ERRO: Usuário não autorizado.");        // Regra 4
        Certificate c = query(id);
        if (c == null) return Result.error(404, "ERRO: Certificado não encontrado.");                 // Regra 6
        if (c.getStatus() == Certificate.Status.CANCELLED)
            return Result.error(409, "ERRO: Certificado já está cancelado.");                         // Regra 5

        c.cancel();
        writeToBlockchain("CERTIFICATE_CANCELLED;id=" + c.getId() + ";issuer=" + issuer.trim());
        return Result.ok("Certificado cancelado com sucesso.");
    }

    public synchronized List<Certificate> list() { return new ArrayList<>(state.values()); }

    public synchronized int count(Certificate.Status s) {
        return (int) state.values().stream().filter(c -> c.getStatus() == s).count();
    }

    // Cria bloco -> Proof of Work (dentro de addBlock) -> adiciona à cadeia
    private void writeToBlockchain(String data) {
        blockchain.addBlock(blockchain.newBlock(data));
    }

    private boolean isAuthorized(String issuer) { return issuer != null && AUTHORIZED_ISSUERS.contains(issuer.trim()); }
    private boolean invalid(String s) { return s == null || s.isBlank() || s.contains(";") || s.contains("\n") || s.contains("\r"); }
}

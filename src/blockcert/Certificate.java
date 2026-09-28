package blockcert;

/** Modelo do certificado (estado atual guardado pelo Smart Contract). */
public class Certificate {
    public enum Status { VALID, CANCELLED }

    private final String id, student, course, institution, issueDate, issuer;
    private Status status = Status.VALID;

    public Certificate(String id, String student, String course, String institution, String issueDate, String issuer) {
        this.id = id; this.student = student; this.course = course;
        this.institution = institution; this.issueDate = issueDate; this.issuer = issuer;
    }

    public String getId() { return id; }
    public String getStudent() { return student; }
    public String getCourse() { return course; }
    public String getInstitution() { return institution; }
    public String getIssueDate() { return issueDate; }
    public String getIssuer() { return issuer; }
    public Status getStatus() { return status; }

    /** Só o Smart Contract (mesmo pacote) altera o status; não existe caminho CANCELLED -> VALID. */
    void cancel() { this.status = Status.CANCELLED; }
}

package blockcert;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Backend HTTP simples (API REST + arquivos estáticos) usando apenas o JDK. */
public class BlockCertServer {
    private final Blockchain blockchain = new Blockchain(3);           // dificuldade 3
    private final CertificateSmartContract contract = new CertificateSmartContract(blockchain);

    public static void main(String[] args) throws IOException {
        new BlockCertServer().start(8080);
    }

    void start(int port) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress(port), 0);
        s.createContext("/", this::handle);
        s.start();
        System.out.println("BlockCert rodando em http://localhost:" + port);
    }

    private void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath(), m = ex.getRequestMethod();
            if (!path.startsWith("/api/")) { serveIndex(ex); return; }
            String[] p = path.substring(5).split("/");             // ex: ["certificates","CERT-001","cancel"]
            Map<String, String> f = m.equals("POST") ? form(ex) : Map.of();

            if (p[0].equals("certificates")) {
                if (p.length == 1 && m.equals("GET")) {
                    StringJoiner j = new StringJoiner(",", "[", "]");
                    contract.list().forEach(c -> j.add(cert(c)));
                    send(ex, 200, j.toString());
                } else if (p.length == 1 && m.equals("POST")) {
                    result(ex, contract.register(f.get("id"), f.get("student"), f.get("course"),
                            f.get("institution"), f.get("date"), f.get("issuer")));
                } else if (p.length == 2 && m.equals("GET")) {
                    Certificate c = contract.query(dec(p[1]));
                    if (c == null) send(ex, 404, "{\"success\":false,\"message\":\"ERRO: Certificado não encontrado.\"}");
                    else send(ex, 200, "{\"success\":true,\"certificate\":" + cert(c) + "}");
                } else if (p.length == 3 && p[2].equals("cancel") && m.equals("POST")) {
                    result(ex, contract.cancel(dec(p[1]), f.get("issuer")));
                } else send(ex, 404, "{\"message\":\"Rota não encontrada\"}");
            } else if (p[0].equals("blockchain")) {
                if (p.length == 2 && p[1].equals("valid")) {
                    send(ex, 200, "{\"valid\":" + blockchain.isBlockChainValid() + "}");
                } else {
                    StringJoiner j = new StringJoiner(",", "[", "]");
                    for (Block b : blockchain.getBlocks())
                        j.add("{\"index\":" + b.getIndex() + ",\"timestamp\":" + b.getTimestamp() + ",\"data\":" + q(b.getData())
                                + ",\"hash\":" + q(b.getHash()) + ",\"previousHash\":" + q(b.getPreviousHash()) + ",\"nonce\":" + b.getNonce() + "}");
                    send(ex, 200, "{\"valid\":" + blockchain.isBlockChainValid() + ",\"difficulty\":" + blockchain.getDifficulty()
                            + ",\"blocks\":" + j + "}");
                }
            } else if (p[0].equals("summary")) {
                send(ex, 200, "{\"total\":" + contract.list().size() + ",\"valid\":" + contract.count(Certificate.Status.VALID)
                        + ",\"cancelled\":" + contract.count(Certificate.Status.CANCELLED) + ",\"blocks\":" + blockchain.getBlocks().size()
                        + ",\"chainValid\":" + blockchain.isBlockChainValid() + "}");
            } else send(ex, 404, "{\"message\":\"Rota não encontrada\"}");
        } catch (Exception e) {
            send(ex, 500, "{\"success\":false,\"message\":" + q("Erro interno: " + e.getMessage()) + "}");
        }
    }

    private void result(HttpExchange ex, CertificateSmartContract.Result r) throws IOException {
        send(ex, r.code(), "{\"success\":" + r.success() + ",\"message\":" + q(r.message()) + "}");
    }

    private String cert(Certificate c) {
        return "{\"id\":" + q(c.getId()) + ",\"student\":" + q(c.getStudent()) + ",\"course\":" + q(c.getCourse())
                + ",\"institution\":" + q(c.getInstitution()) + ",\"date\":" + q(c.getIssueDate())
                + ",\"issuer\":" + q(c.getIssuer()) + ",\"status\":" + q(c.getStatus().name()) + "}";
    }

    private void serveIndex(HttpExchange ex) throws IOException {
        byte[] b = Files.readAllBytes(Path.of("web", "index.html"));
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    }

    private void send(HttpExchange ex, int code, String json) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    }

    private Map<String, String> form(HttpExchange ex) throws IOException {
        Map<String, String> m = new HashMap<>();
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        for (String kv : body.split("&")) {
            String[] a = kv.split("=", 2);
            if (a.length == 2) m.put(dec(a[0]), dec(a[1]));
        }
        return m;
    }

    private static String dec(String s) { return URLDecoder.decode(s, StandardCharsets.UTF_8); }

    private static String q(String s) {
        if (s == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') b.append('\\').append(c);
            else if (c < 32) b.append(' ');
            else b.append(c);
        }
        return b.append('"').toString();
    }
}

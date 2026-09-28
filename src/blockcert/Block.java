package blockcert;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Bloco fornecido pelo professor (lógica preservada; só foi adicionado getNonce()). */
public class Block {
    private int index;
    private long timestamp;
    private String hash;
    private String previousHash;
    private String data;
    private int nonce;

    public Block(int index, long timestamp, String previousHash, String data) {
        this.index = index;
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.data = data;
        this.nonce = 0;
        this.hash = calculateHash(this);
    }

    public int getIndex() { return this.index; }
    public long getTimestamp() { return this.timestamp; }
    public String getHash() { return this.hash; }
    public String getPreviousHash() { return this.previousHash; }
    public String getData() { return this.data; }
    public int getNonce() { return this.nonce; } // adicionado para a interface exibir o nonce

    public String str() {
        return (long) this.index + this.timestamp + this.previousHash + this.data + this.nonce;
    }

    public static String calculateHash(Block block) {
        if (block != null) {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
            String txt = block.str();
            byte[] bytes = digest.digest(txt.getBytes());
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(255 & b);
                if (hex.length() == 1) builder.append('0');
                builder.append(hex);
            }
            return builder.toString();
        }
        return null;
    }

    public void proofOfWork(int difficulty) {
        for (this.nonce = 0;
             !this.getHash().substring(0, difficulty).equals(Utils.zeros(difficulty));
             this.hash = calculateHash(this)) {
            ++this.nonce;
        }
    }
}

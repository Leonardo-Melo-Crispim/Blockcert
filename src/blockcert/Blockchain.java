package blockcert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Blockchain fornecida pelo professor (lógica preservada; só foi adicionado getBlocks()). */
public class Blockchain {
    private int difficulty;
    private List<Block> blocks;

    public Blockchain(int difficulty) {
        this.difficulty = difficulty;
        this.blocks = new ArrayList<>();
        Block b = new Block(0, System.currentTimeMillis(), null, "Block gênesis");
        b.proofOfWork(difficulty);
        this.blocks.add(b);
    }

    public int getDifficulty() { return this.difficulty; }
    public List<Block> getBlocks() { return Collections.unmodifiableList(blocks); } // adicionado (somente leitura)
    public Block latestBlock() { return this.blocks.get(this.blocks.size() - 1); }

    public Block newBlock(String data) {
        Block latestBlock = this.latestBlock();
        return new Block(latestBlock.getIndex() + 1, System.currentTimeMillis(), latestBlock.getHash(), data);
    }

    public void addBlock(Block b) {
        if (b != null) {
            b.proofOfWork(this.difficulty);
            this.blocks.add(b);
        }
    }

    public boolean isFirstBlockValid() {
        Block firstBlock = this.blocks.get(0);
        if (firstBlock.getIndex() != 0) return false;
        if (firstBlock.getPreviousHash() != null) return false;
        return firstBlock.getHash() != null && Block.calculateHash(firstBlock).equals(firstBlock.getHash());
    }

    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (newBlock == null || previousBlock == null) return false;
        if (previousBlock.getIndex() + 1 != newBlock.getIndex()) return false;
        if (newBlock.getPreviousHash() != null && newBlock.getPreviousHash().equals(previousBlock.getHash())) {
            return newBlock.getHash() != null && Block.calculateHash(newBlock).equals(newBlock.getHash());
        }
        return false;
    }

    public boolean isBlockChainValid() {
        if (!this.isFirstBlockValid()) return false;
        for (int i = 1; i < this.blocks.size(); ++i) {
            if (!this.isValidNewBlock(this.blocks.get(i), this.blocks.get(i - 1))) return false;
        }
        return true;
    }
}

package Huffman;

/**
 * Created by jihadbadran on 10/27/17.
 */
public class HuffmanNode implements Comparable<HuffmanNode>{

    private byte c;
    private int occ;

    private HuffmanNode right;
    private HuffmanNode left;

    public HuffmanNode() {

    }

    public HuffmanNode(byte c, int occ, HuffmanNode right, HuffmanNode left) {
        this.c = c;
        this.occ = occ;
        this.right = right;
        this.left = left;
    }

    public byte getC() {
        return c;
    }

    public void setC(byte c) {
        this.c = c;
    }

    public int getOcc() {
        return occ;
    }

    public void setOcc(int occ) {
        this.occ = occ;
    }

    public HuffmanNode getRight() {
        return right;
    }

    public void setRight(HuffmanNode right) {
        this.right = right;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public void setLeft(HuffmanNode left) {
        this.left = left;
    }

    public boolean isLeaf() {
        return (right == null && left == null);
    }

    @Override
    public String toString() {
        return getC() + "," + getOcc();
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return this.getOcc() - o.getOcc();
    }
}
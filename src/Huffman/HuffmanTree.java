package Huffman;

import java.util.HashMap;

/**
 * Created by jihadbadran on 10/21/17.
 *
 */

public class HuffmanTree {

    private HuffmanNode root = new HuffmanNode();

    public HuffmanNode getRoot() {
        return root;
    }

    public void setRoot(HuffmanNode root) {
        this.root = root;
    }

    /**
     *
     *
     * @param node
     * @param code
     * @param map
     */
    private void makeMap(HuffmanNode node, String code, HashMap<Byte, String> map){

        if(node == null) return;
        if(node.isLeaf() ){
            map.put(node.getC(), code);
        } else{
            makeMap(node.getLeft(), code + "0", map);
            makeMap(node.getRight(), code + "1", map);
        }

    }

    /**
     *
     * @return map
     */
    public HashMap<Byte, String> makeMap(){
        HashMap<Byte, String> map = new HashMap<>();
        makeMap(root, "", map);
        return map;
    }

    private void print(HuffmanNode node){
        if(node == null) return;
        if(node.isLeaf())
            System.out.println((char)node.getC());
        else{
            print(node.getLeft());
            print(node.getRight());
        }
    }
    public void print(){
        print(this.root);
    }
}
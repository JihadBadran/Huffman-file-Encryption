package Huffman;

import java.io.*;
import java.util.*;

/**
 * Created by jihadbadran on 10/21/17.
 * it Encodes the file to Huffman code, and write produce .huff file
 */
public class HuffmanEncoder {


    private int SIZE = 8 * 1024;

    // HashMap to Store the byte-huffmanCode pairs
    private HashMap<Byte, String> map = new HashMap<>();

    // number of distinct bytes used in the file
    private int numberOfByteValuesUsed = 0;

    // bitSet object that contains longs[] and manipulate its bits, will be used to manipulate bits
    private BitSet bitSet = new BitSet();

    // the file that will be encoded
    private File sourceFile;

    // the file that will be written
    private File distFile;

    // the huffman tree that will be built
    private HuffmanTree huffmanTree = new HuffmanTree();


    /**
     * the only constructor
     *
     * @param fileToEncode File
     * @param outputFile   File
     * @throws IOException I/O
     */
    public HuffmanEncoder(File fileToEncode, File outputFile) throws IOException {
        this.sourceFile = fileToEncode;
        this.distFile = outputFile;
    }

    /**
     * method to build MinHeap which is a PriorityQueue from a TreeMap
     *
     * @param map that will be maped to the heap
     * @return PriorityQueue
     */
    private PriorityQueue<HuffmanNode> buildHeap(TreeMap<Byte, Integer> map) {
        // make a new heap
        PriorityQueue<HuffmanNode> heap = new PriorityQueue<>();

        // loop through map, and put in the heap
        map.forEach((b, integer) -> heap.offer(new HuffmanNode(b, integer, null, null)));

        return heap;
    }

    /**
     * read the file, and compute the frequency of every byte value
     *
     * @return TreeMap
     * @throws IOException I/O
     */
    private TreeMap<Byte, Integer> readAndTreeMap() throws IOException {

        // instantiate new TreeMap object
        // the reason to use TreeMap not HashMap, is that TreeMap stores the records in ascending order of values
        TreeMap<Byte, Integer> treeMap = new TreeMap<>();

        // open a stream to read the file
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));

        // make a buffer, of size SIZE, see value of SIZE in class head
        byte[] bytes = new byte[SIZE];

        // to store the number of read bytes from stream
        int numberOfBytes;

        // loop through buffers in file
        while ((numberOfBytes = inputStream.read(bytes, 0, SIZE)) != -1) {
            //loop through bytes in each buffer
            for (int j = 0; j < numberOfBytes; j++) {

                // merge the frequency of every byte value in treeMap
                treeMap.merge(bytes[j], 1, (a, b) -> a + b);
            }
        }

        // close the stream
        inputStream.close();

        return treeMap;
    }


    private void buildHuffmanTree(PriorityQueue<HuffmanNode> heap) {
        // building the huffman tree
        while (heap.size() > 1) {

            HuffmanNode first = heap.remove();
            HuffmanNode second = heap.remove();

            HuffmanNode temp = new HuffmanNode((byte) 0, first.getOcc() + second.getOcc(), first, second);
            heap.add(temp);
        }

        // check if there is a remaining item in the binary heap
        huffmanTree.setRoot(heap.remove());
    }


    /**
     * @throws IOException Input/Output Exception
     */
    public void encode() throws IOException {


        // build the huffman tree, from heap, and the heap is built from TreeMap
        buildHuffmanTree(buildHeap(readAndTreeMap()));


        // generate the map of bytes to its huffman codes
        map = huffmanTree.makeMap();

        // build huffman tree string from huffman tree
        buildHuffmanTreeHeading();


        // open an inputStream to read the file and encode
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));

        // open an outputStream to write the file "distFile"
        FileOutputStream outputStream = new FileOutputStream(distFile);

        // write the number of distinct byte values in huffman tree in the .huff file
        numberOfByteValuesUsed = numberOfByteValuesUsed > 255 ? 0 : numberOfByteValuesUsed;
        outputStream.write(numberOfByteValuesUsed);

        // write the tree to the head of the file
        outputStream.write(bitSet.toByteArray());

        // clear the bitSet to write the content, and the index to start from zero to know where to put the ones
        bitSet.clear();

        // index of current bit in bitSet
        ////// p.s: bitSet has a method length(), but this method count 1s, so if there is zeros at the end it will not count it
        //////      so its a must to use a counter
        //////      later on we will put a 1 as a delimiter in the bitSet if the bitSet length is not of size SIZE defined in the class head
        int index = 0;

        // buffer of bytes, to read from file
        byte[] bytes = new byte[SIZE];

        // number of bytes read from file in buffer
        int numberOfBytes;

        // for each buffer size of bytes in file read
        while ((numberOfBytes = inputStream.read(bytes, 0, SIZE)) != -1) {
            // loop through bytes in each buffer
            for (int j = 0; j < numberOfBytes; j++) {

                // loop through bits of huffman code for the current byte
                for (char c : map.get(bytes[j]).toCharArray()) {

                    // if bit is 1 then write in bitSet, if not, then its already 0
                    if (c == '1') bitSet.set(index);

                    // increment the counter of bits
                    index++;

                    // if the bitSet length equals or larger than 1kb then right it to the file
                    if (index / 8 == SIZE) {
                        bitSet.set(index);
                        // write the bitSet, and reset index and bitSet
                        outputStream.write(bitSet.toByteArray(), 0, index / 8);
                        index = 0;
                        bitSet.clear();
                    }
                }

            }
        }

        // put a delimiter at the end for the reason explained in line 148
        bitSet.set(index);


        // if the bitSet is not empty, write it to the file
        if (index != 0) {
            outputStream.write(bitSet.toByteArray());
            bitSet.clear();
        }


        // write the number of leftovers bits after writing
        outputStream.write(8 - (index % 8));


        // close the streams
        outputStream.close();
        inputStream.close();

    }

    /**
     * @see BitSet
     */
    private void buildHuffmanTreeHeading() {

        StringBuilder tree = new StringBuilder();                       // hold the binary coded tree
        Stack<HuffmanNode> stack = new Stack<>();                       // stack to go through the binary tree
        stack.push(huffmanTree.getRoot());                              // push the head to the stack
        HuffmanNode temp;                                               // make a temp node to hold current node in tree

        // while the stack NOT empty, to loop through huffman tree inorder
        while (!stack.isEmpty()) {
            temp = stack.pop();

            // if node is leaf, then print 1 and add the byte bits to the tree String code
            if (temp.isLeaf()) {
                tree.append("1").append(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(temp.getC()))).replace(' ', '0'));
            }

            // if not, then add 0 to the string code
            else {
                tree.append("0");
            }


            // if code has right, then push to the stack
            if (temp.getRight() != null) {
                stack.push(temp.getRight());
            }
            // if code has left, then push to the stack
            if (temp.getLeft() != null) {
                stack.push(temp.getLeft());
            }

        }


        // Writing the tree to the bitSet object
        for (int i = 0; i < tree.length(); i++) {
            if (tree.charAt(i) == '1')
                bitSet.set(i);
            else {
                bitSet.set(i, false);
            }
        }

        this.numberOfByteValuesUsed = map.size();
    }

}
package Huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Stack;

/**
 * Created by jihadbadran on 10/27/17.
 * Class to Decode a huffman decoded file
 */
public class HuffmanDecoder {

    // constant SIZE of buffer that holds read and write data in file
    private final int SIZE = 8 * 1024;

    // file that will be read
    private File inputFile;

    // file that will be written
    private File outputFile;

    // huffman tree object
    private HuffmanTree huffmanTree = new HuffmanTree();

    // actual tree bit length, maximum 2559
    private int treeLength = 0;


    /**
     * the only constructor
     *
     * @param fileToDecode File
     * @param output       File
     */
    public HuffmanDecoder(File fileToDecode, File output) {
        this.inputFile = fileToDecode;
        this.outputFile = output;
    }


    /**
     * decode the file
     *
     * @throws Exception
     */
    public void decode() throws Exception {

        // open a stream to read the file
        FileInputStream inputStream = new FileInputStream(this.inputFile);

        // the bytes array to hold at max the buffer size of data from file
        byte[] bytes = new byte[SIZE];

        // the stream and the buffer
        FileOutputStream writer = new FileOutputStream(this.outputFile, false);

        // buffer of data to be written
        byte[] encodedData = new byte[SIZE];

        // build the huffman tree from the header read from file
        buildHuffmanTree(readHeaderFromFile(inputStream));

        // holds the number of bytes read from file
        int temp;

        // make a cursor to traverse the huffman tree according to the code read, to encode content
        HuffmanNode cursor = huffmanTree.getRoot();

        // counter for encoded bytes
        int encodedDataCursor = 0;

        // 2 bytes reserved to hold the last and before last bytes
        byte lastContentByte = 0;          // last byte of content, that might be padded
        byte Padding = 0;                  // the padding value

        // loop through buffers of file
        while ((temp = inputStream.read(bytes, 0, SIZE)) != -1) {

            // check if this is the last buffer to read
            // then store the last two bytes, because the last byte is the padding value
            // and the byte before the last is the padded byte
            if (temp < SIZE) {
                Padding = bytes[temp - 1];
                lastContentByte = bytes[temp - 2];
                temp = temp - 2;
            }

            // loop through bytes of buffer
            for (int i = 0; i < temp; i++) {
                // loop through bits of current byte
                for (int j = 1; j < 256; j <<= 1) {

                    // navigate the cursor over tree according to bits
                    // 0 to left, 1 to right
                    if ((bytes[i] & j) == 0) {
                        cursor = cursor.getLeft();
                    } else {
                        cursor = cursor.getRight();
                    }

                    // if leaf in tree is reached, then insert the byte value from leaf in encoded data buffer
                    if (cursor.isLeaf()) {
                        encodedData[encodedDataCursor++] = cursor.getC();

                        // reset cursor to tree root
                        cursor = huffmanTree.getRoot();
                    }

                    // if writing buffer is full, then dump in file and reset
                    if (encodedDataCursor == SIZE - 1) {

                        // write in file
                        writer.write(encodedData, 0, encodedDataCursor);

                        // reset buffer
                        encodedData = new byte[SIZE];

                        // reset Buffer Indexer
                        encodedDataCursor = 0;
                    }
                }
            }

            // check if this is the last buffer to read
            // then parse the last byte according to the padding value
            if (temp < SIZE) {
                // loop through bits of last byte, without padded 0s
                for (int j = 1; j < (1 << (8 - Padding)); j <<= 1) {

                    // keep navigating in tree
                    if ((lastContentByte & j) == 0) {
                        cursor = cursor.getLeft();
                    } else
                        cursor = cursor.getRight();

                    // if leaf then decode and reset tree cursor
                    if (cursor.isLeaf()) {
                        encodedData[encodedDataCursor++] = cursor.getC();
                        cursor = huffmanTree.getRoot();
                    }

                }

                // if there is something to write, then write
                if (encodedDataCursor > 0) {
                    writer.write(encodedData, 0, encodedDataCursor);
                }
            }

        }


        // close streams
        inputStream.close();
        writer.close();
    }


    /**
     * build the huffman tree from the header information as a BitSet
     *
     * @param treeBits BitSet, the header bits
     * @see BitSet
     */
    private void buildHuffmanTree(BitSet treeBits) {

        // make a stack build the tree
        Stack<HuffmanNode> stack = new Stack<>();

        // make a root
        HuffmanNode root = new HuffmanNode();

        // push the root in the stack
        stack.push(root);

        // make a current to hold the current
        HuffmanNode curr;

        // loop through bits in bitSet
        for (int i = 0; i < treeLength; i++) {
            // pop the stack and put in current
            curr = stack.pop();

            // if bit is 0 then build a subtree and push right and left of it to stack
            if (!treeBits.get(i)) {
                curr.setRight(new HuffmanNode());
                curr.setLeft(new HuffmanNode());

                stack.push(curr.getRight());
                stack.push(curr.getLeft());
            }
            // if bit is 1, then make the current node a leaf, and put the byte value
            else {
                curr.setC(bitSetNextByte(treeBits, i + 1));
                i += 8;
            }
        }

        // put the root as class global huffman tree root
        huffmanTree.setRoot(root);
    }


    /**
     * to get the next byte from a bitSet object,
     * starting from the offset
     *
     * @param bitSet to get the next byte from
     * @param offset the offset of the reading
     * @return byte
     */
    private byte bitSetNextByte(BitSet bitSet, int offset) {
        byte result = 0;
        for (int i = offset; i < offset + 8; i++) {
            result = (byte) (result << 1);
            if (bitSet.get(i))
                result |= 1;
        }
        return result;
    }

    /**
     * read the header from FileInputStream given,
     *
     * @param stream FileInputStream
     * @return BiSet
     * @throws Exception I/O
     * @see BitSet
     */
    private BitSet readHeaderFromFile(FileInputStream stream) throws Exception {

        // read the first byte, which is the number of distinct bytes used in encoded file
        int treeLength = Math.abs(stream.read());

        // if there is 256 chars used then it will be written 0 in file, so we want to detect if it is 0
        treeLength = treeLength == 0 ? 256 : treeLength;

        // then calculate the tree actual length
        treeLength = treeLength * 10 - 1;

        int treeFullLength = (int) Math.ceil(treeLength / 8.0);
        int treePadding = treeLength % 8;

        // a buffer to hold read data from file
        byte[] bytes = new byte[SIZE];

        // check if the read data is not the expected length, then throw IOException
        if (stream.read(bytes, 0, treeFullLength) != treeFullLength)
            throw new IOException("Error:Could not read header in file.");

        // index of bits in BitSet
        int index = 0;
        // make the BitSet
        BitSet header = new BitSet();

        // loop throw the read header
        for (int i = 0; i < treeFullLength; i++) {

            // loop through bits of current byte
            for (int j = 1; j < 256; j <<= 1) {

                // if bit=1 then assign it in bitSet
                if ((bytes[i] & j) != 0) {
                    header.set(index);
                }
                // increment index
                index++;
            }
        }

        // actual length of tree bits assigned
        this.treeLength = index - (8 - treePadding);

        // return the header BitSet
        return header;
    }
}

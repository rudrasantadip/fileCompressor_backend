import java.io.*;
import java.util.*;

class Node {
    char data;
    int freq;
    String code;
    Node left, right;

    Node(char data, int freq) {
        this.data = data;
        this.freq = freq;
        this.code = "";
        this.left = null;
        this.right = null;
    }
}

class Compare implements Comparator<Node> {
    public int compare(Node x, Node y) {
        return x.freq - y.freq;
    }
}

public class Huffman {
    String inFileName;
    String outFileName;
    Node[] arr;
    PriorityQueue<Node> minHeap;
    Node root;
    FileInputStream inFile;
    FileOutputStream outFile;

    Huffman(String inFileName, String outFileName) {
        this.inFileName = inFileName;
        this.outFileName = outFileName;
        arr = new Node[128];
        minHeap = new PriorityQueue<>(new Compare());
        root = null;
    }

    void createArr() {
        for (int i = 0; i < 128; i++) {
            arr[i] = new Node((char) i, 0);
        }
    }

    void traverse(Node r, String str) {
        if (r.left == null && r.right == null) {
            r.code = str;
            return;
        }

        traverse(r.left, str + '0');
        traverse(r.right, str + '1');
    }

    int binToDec(String inStr) {
        int res = 0;
        for (char c : inStr.toCharArray()) {
            res = res * 2 + c - '0';
        }
        return res;
    }

    String decToBin(int inNum) {
        StringBuilder temp = new StringBuilder();
        String res = "";
        while (inNum > 0) {
            temp.append(inNum % 2);
            inNum /= 2;
        }
        res = temp.reverse().toString();
        res = String.format("%8s", res).replace(' ', '0');
        return res;
    }

    void buildTree(char a_code, String path) {
        Node curr = root;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '0') {
                if (curr.left == null) {
                    curr.left = new Node('\0', 0);
                }
                curr = curr.left;
            } else if (path.charAt(i) == '1') {
                if (curr.right == null) {
                    curr.right = new Node('\0', 0);
                }
                curr = curr.right;
            }
        }
        curr.data = a_code;
    }

    void createMinHeap() throws IOException {
        inFile = new FileInputStream(inFileName);
        int id = inFile.read();
        while (id != -1) {
            arr[id].freq++;
            id = inFile.read();
        }
        inFile.close();
        for (int i = 0; i < 128; i++) {
            if (arr[i].freq > 0) {
                minHeap.offer(arr[i]);
            }
        }
    }

    void createTree() {
        Node left, right;
        PriorityQueue<Node> tempPQ = new PriorityQueue<>(minHeap);
        while (tempPQ.size() != 1) {
            left = tempPQ.poll();
            right = tempPQ.poll();
            root = new Node('\0', left.freq + right.freq);
            root.left = left;
            root.right = right;
            tempPQ.offer(root);
        }
    }

    void createCodes() {
        traverse(root, "");
    }

    void saveEncodedFile() throws IOException {
        inFile = new FileInputStream(inFileName);
        outFile = new FileOutputStream(outFileName);
        StringBuilder in = new StringBuilder();
        StringBuilder s = new StringBuilder();
        int id;

        in.append((char) minHeap.size());
        PriorityQueue<Node> tempPQ = new PriorityQueue<>(minHeap);
        while (!tempPQ.isEmpty()) {
            Node curr = tempPQ.poll();
            in.append(curr.data);
            String code = curr.code;
            String paddedCode = String.format("%127s", code).replace(' ', '0');
            s.append('1').append(paddedCode);
            for (int i = 0; i < 16; i++) {
                in.append((char) binToDec(s.substring(0, 8)));
                s.delete(0, 8);
            }
        }

        while ((id = inFile.read()) != -1) {
            s.append(arr[id].code);
            while (s.length() >= 8) {
                in.append((char) binToDec(s.substring(0, 8)));
                s.delete(0, 8);
            }
        }

        int count = 8 - s.length();
        if (s.length() < 8) {
            s.append("0".repeat(count));
        }
        in.append((char) binToDec(s.toString()));
        in.append((char) count);

        outFile.write(in.toString().getBytes());
        inFile.close();
        outFile.close();
    }

    void saveDecodedFile() throws IOException {
        try (FileInputStream inFile = new FileInputStream(inFileName);
             FileOutputStream outFile = new FileOutputStream(outFileName)) {
    
            int size = inFile.read();
            if (size <= 0) {
                System.err.println("Invalid metadata size: " + size);
                return;
            }
    
            Node root = new Node('\0', 0);
            for (int i = 0; i < size; i++) {
                char aCode = (char) inFile.read();
                byte[] hCodeC = new byte[16];
                if (inFile.read(hCodeC) != 16) {
                    System.err.println("Unexpected end of input: Huffman code data");
                    return;
                }
                StringBuilder hCodeStr = new StringBuilder();
                for (byte b : hCodeC) {
                    hCodeStr.append(decToBin(b));
                }
                // Ensure 128 bits are read and remove padding
                hCodeStr.setLength(128);
                int startIndex = hCodeStr.indexOf("1");
                if (startIndex == -1) {
                    System.err.println("Invalid Huffman code: No '1' character found");
                    return;
                }
                hCodeStr = new StringBuilder(hCodeStr.substring(startIndex + 1));
                buildTree(aCode, hCodeStr.toString());
            }
    
            StringBuilder decodedText = new StringBuilder();
            Node current = root;
            int bit;
            while ((bit = inFile.read()) != -1) {
                if (current == null) {
                    System.err.println("Unexpected end of input: Huffman tree traversal");
                    return;
                }
                if (bit == '0') {
                    current = current.left;
                } else {
                    current = current.right;
                }
                if (current.left == null && current.right == null) {
                    decodedText.append(current.data);
                    current = root; // Reset to the root for next character decoding
                }
            }
    
            outFile.write(decodedText.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Error during decompression: " + e.getMessage());
            throw e; // Re-throw the exception to indicate decompression failure
        }
    }
    
    

    void getTree() throws IOException {
        FileInputStream inFile = null;
        try {
            inFile = new FileInputStream(inFileName);
            int size = inFile.read();
            root = new Node('\0', 0);
            for (int i = 0; i < size; i++) {
                char aCode = (char) inFile.read();
                byte[] hCodeC = new byte[16];
                if (inFile.read(hCodeC) != 16) {
                    System.err.println("Unexpected end of input: Huffman code data");
                    return;
                }
                StringBuilder hCodeStr = new StringBuilder();
                for (byte b : hCodeC) {
                    hCodeStr.append(decToBin(b));
                }
                // Padding with '0's if necessary to ensure a length of 128 characters
                while (hCodeStr.length() < 128) {
                    hCodeStr.insert(0, '0');
                }
                // Trimming to ensure a length of 128 characters
                hCodeStr.setLength(128);
                // Removing padding
                int startIndex = hCodeStr.indexOf("1");
                if (startIndex == -1) {
                    System.err.println("Invalid Huffman code: No '1' character found");
                    return;
                }
                hCodeStr = new StringBuilder(hCodeStr.substring(startIndex + 1));
                buildTree(aCode, hCodeStr.toString());
            }
        } finally {
            if (inFile != null) {
                inFile.close();
            }
        }
    }
    
    

    void compress() {
        try {
            createArr();
            createMinHeap();
            createTree();
            createCodes();
            saveEncodedFile();
            System.out.println("Compression completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void decompress() {
        try {
            getTree();
            saveDecodedFile();
            System.out.println("Decompression completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String inputFileName = "inputFile.txt";
        String compressedFileName = "compressed.huf";
        String decompressedFileName = "decompressed.txt";

        Huffman huffman = new Huffman(inputFileName, compressedFileName);
        huffman.compress();

        // Decompressing the file
        // Huffman decompressor = new Huffman(compressedFileName, decompressedFileName);
        // decompressor.decompress();
    }
}

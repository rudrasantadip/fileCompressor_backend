package com.project.compressor.fileCompressor.utils;

import java.io.*;
import java.util.*;

class HuffmanNode {
    int frequency;
    char data;
    HuffmanNode left, right;

    public HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
    }
}

class HuffmanComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.frequency - y.frequency;
    }
}

public class Huffman
 {
    
    private static Map<Character, String> huffmanCodes = new HashMap<>();

    // public static void main(String[] args) throws IOException, ClassNotFoundException {
    //     String inputFile = "inputFile.txt";
    //     String compressedFile = "compressed.bin";
    //     String decompressedFile = "decompressed.txt";

    //     compress(inputFile, compressedFile);
    //     decompress(compressedFile, decompressedFile);
    // }

    public static void compress(String inputFile, String compressedFile) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        FileOutputStream fos = new FileOutputStream(compressedFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        Map<Character, Integer> frequencyMap = new HashMap<>();
        int ch;
        while ((ch = fis.read()) != -1) {
            char c = (char) ch;
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }
        fis.close();

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(new HuffmanComparator());
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode newNode = new HuffmanNode('$', left.frequency + right.frequency);
            newNode.left = left;
            newNode.right = right;
            pq.offer(newNode);
        }

        HuffmanNode root = pq.peek();
        generateCodes(root, "");

        fis = new FileInputStream(inputFile);
        StringBuilder encodedString = new StringBuilder();
        while ((ch = fis.read()) != -1) {
            char c = (char) ch;
            encodedString.append(huffmanCodes.get(c));
        }
        fis.close();

        oos.writeObject(huffmanCodes);
        byte[] encodedBytes = getBytes(encodedString.toString());
        oos.writeObject(encodedBytes);

        oos.close();
    }

    public static void decompress(String compressedFile, String decompressedFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(compressedFile);
        ObjectInputStream ois = new ObjectInputStream(fis);

        Map<Character, String> codes = (Map<Character, String>) ois.readObject();
        byte[] encodedBytes = (byte[]) ois.readObject();
        ois.close();

        StringBuilder encodedString = new StringBuilder();
        for (byte b : encodedBytes) {
            encodedString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        Map<String, Character> reverseCodes = new HashMap<>();
        for (Map.Entry<Character, String> entry : codes.entrySet()) {
            reverseCodes.put(entry.getValue(), entry.getKey());
        }

        StringBuilder decodedString = new StringBuilder();
        String temp = "";
        for (int i = 0; i < encodedString.length(); i++) {
            temp += encodedString.charAt(i);
            if (reverseCodes.containsKey(temp)) {
                decodedString.append(reverseCodes.get(temp));
                temp = "";
            }
        }

        FileWriter fw = new FileWriter(decompressedFile);
        fw.write(decodedString.toString());
        fw.close();
    }

    public static void generateCodes(HuffmanNode root, String code) {
        if (root == null)
            return;

        if (root.data != '$')
            huffmanCodes.put(root.data, code);

        generateCodes(root.left, code + "0");
        generateCodes(root.right, code + "1");
    }

    public static byte[] getBytes(String encodedString) {
        int length = (int) Math.ceil(encodedString.length() / 8.0);
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            String chunk = encodedString.substring(i * 8, Math.min((i + 1) * 8, encodedString.length()));
            bytes[i] = (byte) Integer.parseInt(chunk, 2);
        }
        return bytes;
    }
    
    

}

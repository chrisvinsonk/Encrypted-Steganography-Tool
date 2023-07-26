package com.example.demo.steganography;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Arrays;
import java.security.MessageDigest;
import javax.imageio.ImageIO;

@Service
public class SteganographyService {
    private static final String AES_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";

    public String encrypt(String message, String secretKey) throws Exception {
        SecretKeySpec keySpec = generateKey(secretKey);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedMessage, String secretKey) throws Exception {
        SecretKeySpec keySpec = generateKey(secretKey);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    private String generateSecretKeyFromMessage(String message) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] keyBytes = sha.digest(message.getBytes());
        keyBytes = Arrays.copyOf(keyBytes, 16); // Use only the first 16 bytes for 128-bit AES key
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    private SecretKeySpec generateKey(String secretKey) throws Exception {
        byte[] keyBytes = secretKey.getBytes();
        MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16); // Use only the first 16 bytes for 128-bit AES key
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    private static final String END_DELIMITER = "1111111111111110";
    private static final String KEY_DELIMITER = "0000000000000001";

    public byte[] encodeMessage(MultipartFile inputImage, String message) throws IOException, Exception {
        // Encrypt the message using the message itself as the secret key
        String secretKey = message;
        String encryptedMessage = encrypt(message, secretKey);
        String combinedMessage = encryptedMessage + KEY_DELIMITER + secretKey;

        BufferedImage image = ImageIO.read(inputImage.getInputStream());
        int width = image.getWidth();
        int height = image.getHeight();

        StringBuilder binaryMessage = new StringBuilder();
        for (char c : combinedMessage.toCharArray()) {
            String binaryChar = String.format("%8s", Integer.toBinaryString(c))
                    .replace(' ', '0');
            binaryMessage.append(binaryChar);
        }
        binaryMessage.append(END_DELIMITER); // Add end of message delimiter

        if (binaryMessage.length() > width * height * 3) {
            throw new IllegalArgumentException("Message is too large for the image.");
        }

        BufferedImage encodedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int messageIndex = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (messageIndex < binaryMessage.length()) {
                    int rgb = image.getRGB(i, j);

                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    r = (r & 0xFE) | Character.getNumericValue(binaryMessage.charAt(messageIndex++));
                    if (messageIndex < binaryMessage.length()) {
                        g = (g & 0xFE) | Character.getNumericValue(binaryMessage.charAt(messageIndex++));
                    }
                    if (messageIndex < binaryMessage.length()) {
                        b = (b & 0xFE) | Character.getNumericValue(binaryMessage.charAt(messageIndex++));
                    }

                    int encodedRGB = (r << 16) | (g << 8) | b;
                    encodedImage.setRGB(i, j, encodedRGB);
                } else {
                    encodedImage.setRGB(i, j, image.getRGB(i, j));
                }
            }
        }

        // Save the encoded image to a temporary file
        File tempFile = File.createTempFile("encoded_image", ".png");
        ImageIO.write(encodedImage, "png", tempFile);

        // Convert the temporary file to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        } finally {
            // Delete the temporary file
            tempFile.delete();
        }

        return baos.toByteArray();
    }

    public String decodeMessage(MultipartFile encodedImage) throws IOException, Exception {
        BufferedImage image = ImageIO.read(encodedImage.getInputStream());
        int width = image.getWidth();
        int height = image.getHeight();

        StringBuilder binaryMessage = new StringBuilder();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                binaryMessage.append(r & 1);
                binaryMessage.append(g & 1);
                binaryMessage.append(b & 1);
            }
        }

        int endIndex = binaryMessage.indexOf(END_DELIMITER);
        if (endIndex == -1) {
            throw new IllegalArgumentException("No hidden message found in the image.");
        }

        StringBuilder combinedMessage = new StringBuilder();
        for (int i = 0; i < endIndex; i += 8) {
            String binaryChar = binaryMessage.substring(i, i + 8);
            int charCode = Integer.parseInt(binaryChar, 2);
            combinedMessage.append((char) charCode);
        }

        // Separate the encoded secret key from the message
        int keyDelimiterIndex = combinedMessage.indexOf(KEY_DELIMITER);
        if (keyDelimiterIndex == -1) {
            throw new IllegalArgumentException("No encoded secret key found in the image.");
        }

        String encryptedMessage = combinedMessage.substring(0, keyDelimiterIndex);
        String secretKey = combinedMessage.substring(keyDelimiterIndex + KEY_DELIMITER.length());

        // Decrypt the message using the extracted secret key
        String decryptedMessage = decrypt(encryptedMessage, secretKey);
        return decryptedMessage;
    }
}
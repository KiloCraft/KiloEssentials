package org.kilocraft.essentials.votifier.crypto;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Static RSA utility methods for encrypting and decrypting blocks of
 * information.
 *
 * @author Blake Beaupain
 */
public class RSA {

    /**
     * Encrypts a block of data.
     *
     * @param data
     *            The data to encrypt
     * @param key
     *            The key to encrypt with
     * @return The encrypted data
     * @throws Exception
     *             If an error occurs
     */
    public static byte[] encrypt(byte[] data, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts a block of data.
     *
     * @param data
     *            The data to decrypt
     * @param key
     *            The key to decrypt with
     * @return The decrypted data
     * @throws Exception
     *             If an error occurs
     */
    public static byte[] decrypt(byte[] data, PrivateKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

}

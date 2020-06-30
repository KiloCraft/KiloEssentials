package org.kilocraft.essentials.votifier.crypto;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Static utility methods for saving and loading RSA key pairs.
 *
 * @author Blake Beaupain
 */
public class RSAIO {

    /**
     * Saves the key pair to the disk.
     *
     * @param directory
     *            The directory to save to
     * @param keyPair
     *            The key pair to save
     * @throws Exception
     *             If an error occurs
     */
    public static void save(File directory, KeyPair keyPair) throws Exception {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store the public key.
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        FileOutputStream out = new FileOutputStream(directory + "/public.key");
        out.write(DatatypeConverter.printBase64Binary(publicSpec.getEncoded())
                .getBytes());
        out.close();

        // Store the private key.
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
        out = new FileOutputStream(directory + "/private.key");
        out.write(DatatypeConverter.printBase64Binary(privateSpec.getEncoded())
                .getBytes());
        out.close();
    }

    /**
     * Loads an RSA key pair from a directory. The directory must have the files
     * "public.key" and "private.key".
     *
     * @param directory
     *            The directory to load from
     * @return The key pair
     * @throws Exception
     *             If an error occurs
     */
    public static KeyPair load(File directory) throws Exception {
        // Read the public key file.
        File publicKeyFile = new File(directory + "/public.key");
        FileInputStream in = new FileInputStream(directory + "/public.key");
        byte[] encodedPublicKey = new byte[(int) publicKeyFile.length()];
        in.read(encodedPublicKey);
        encodedPublicKey = DatatypeConverter.parseBase64Binary(new String(
                encodedPublicKey));
        in.close();

        // Read the private key file.
        File privateKeyFile = new File(directory + "/private.key");
        in = new FileInputStream(directory + "/private.key");
        byte[] encodedPrivateKey = new byte[(int) privateKeyFile.length()];
        in.read(encodedPrivateKey);
        encodedPrivateKey = DatatypeConverter.parseBase64Binary(new String(
                encodedPrivateKey));
        in.close();

        // Instantiate and return the key pair.
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        return new KeyPair(publicKey, privateKey);
    }

}

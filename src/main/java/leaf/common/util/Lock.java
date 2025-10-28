package leaf.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 加解密操作类
 */
public class Lock {
    private static String AES_KEY = "1234567887654321";// AES加密算法,key的大小必须是16个字节
    /**
     * 公钥
     */
    private static String RSA_PUBLIC_KEY = "" +
            "-----BEGIN PUBLIC KEY-----\n" +
            "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMF4B4aDnV6j+yXiiXBYJjHM8sEgRicQ\n" +
            "TsRndPKocf4PyNTcd9D1046wRMdtV5cijT3oVzBXQYupN+VXmMiM7MMCAwEAAQ==\n" +
            "-----END PUBLIC KEY-----";
    /**
     * 私钥
     */
    private static String RSA_PRIVATE_KEY = "" +
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAwXgHhoOdXqP7JeKJ\n" +
            "cFgmMczywSBGJxBOxGd08qhx/g/I1Nx30PXTjrBEx21XlyKNPehXMFdBi6k35VeY\n" +
            "yIzswwIDAQABAkA+Zcj/kFlkGb05pcuwCS4gZ7pvoUoe9TqCS9/DF6LUTpFgsDlj\n" +
            "6AiXRng6BzlWqdn7//E/+BIInuh7Wn0q/j0hAiEA4xrWytU7EFCfilvy63oXzem2\n" +
            "um9fSqa4fksezyXtERECIQDaFZ0nIDdcACabh5JD7dEseqw85IMKUyfFNtLKaqog\n" +
            "kwIgKvg5C8eslTmr9hHPtJ41QtClskDAVu+UmNC905PpdwECIQCv4u60N49ua9C3\n" +
            "b0fP8WXacbWoBsSI9zgEHoszJYPAcQIhAIdENiYBXqHxVQByKZoRS4uG0UrRskxI\n" +
            "zMnAPlDWNOap\n" +
            "-----END PRIVATE KEY-----\n";

    /**
     * 设置AES密钥
     * @param str 密钥
     */
    public static void setAesKey(String str) {
        Lock.AES_KEY = str;
    }
    /**
     * 设置RSA公钥
     * @param str 密钥
     */
    public static void setRsaPublicKeyKEY(String str) {
        Lock.RSA_PUBLIC_KEY = str;
    }
    /**
     * 设置RSA私钥
     * @param str 密钥
     */
    public static void setRsaPrivateKey(String str) {
        Lock.RSA_PRIVATE_KEY = str;
    }

//    private static String RSA_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
//            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwfT/JOidbl4K8f+Xp6lOcrRCccpcFRugin69/EJTffqAOvPi7lh6o9qGG3+" +
//            "7S6m2O5zM5uRXiQFdtVSptgXFOonMNi6trM2s8g8CkG5C0cC2TOQkIkj7QUMxZmOJeN3rMDFYCq8zxHgcOys54tEX8ZuV0rtEIDNAk4" +
//            "UA6FhkW5YnY9PI73kb2x6OLGPTJoADPuAFCum3YtJV+U/v5bF+gtBUE3Z67NUajeAP+xAOaVCl2Se2plkCSu3rVr+GPtgVtgWJIeniFf" +
//            "l99//A5iA9jT96wikXrGvYHYxyk3H5D0NjIVbTnkNvt1tEYvcbcedvs+lG8mhotO785wsxjPK16wIDAQAB\n" +
//            "-----END PUBLIC KEY-----";
//
//    private static String RSA_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
//            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDB9P8k6J1uXgrx/5enqU5ytEJxylwVG6CKfr38QlN9+oA68+LuWHqj2" +
//            "oYbf7tLqbY7nMzm5FeJAV21VKm2BcU6icw2Lq2szazyDwKQbkLRwLZM5CQiSPtBQzFmY4l43eswMVgKrzPEeBw7Kzni0Rfxm5XSu0QgM0" +
//            "CThQDoWGRblidj08jveRvbHo4sY9MmgAM+4AUK6bdi0lX5T+/lsX6C0FQTdnrs1RqN4A/7EA5pUKXZJ7amWQJK7etWv4Y+2BW2BYkh6eI" +
//            "V+X33/8DmID2NP3rCKResa9gdjHKTcfkPQ2MhVtOeQ2+3W0Ri9xtx52+z6UbyaGi07vznCzGM8rXrAgMBAAECggEARpNGcACwoGNZ6jrN" +
//            "zTU/FV/gOZWfshKOdTavHOZpiRKorEroTUAmyA6Q89qjpWGIrOJecLmv3GuB1KSM6G3rGWTgyOptSc7BWob5sOJeMALGku4E8/DIdDPl+" +
//            "tLHSBLALJYX6jNcS+ZEp4cQFidtZf3uI3L+1BSyL7ua2/Cl++jiG+mfpD9YbuhieNOeh0ortpNy89UwnvIq53mC/UUfVziWXVVnoo3nPt" +
//            "mASNigaoU10yOK9p7FsAwdIAALS9bieL04dWYq+OQfXzc+ggs/hhMtVhq/IiGK4/1HhSVPOO9M7dFPcGXi21fm88Zb8erfFyGvlUj1x2J" +
//            "YFAzTAOm04QKBgQDfqFZ2Cp+brqSZBUOXVNMIehSlQDHW+D8uqHaD63xYvOfqYYd+EQz1WGcmoRioOYGV7jG9fB0YaPqR8TdVV9lVjdG7" +
//            "YbRPUuq3+19hnBCmyYIOg/gW7LK4bcgim6HIgh+gN9ZoLn3RX8UEaPe0TprhuiLABz1UXm2nt2z2z+kpUQKBgQDeASnJu6CALodcmBGmz" +
//            "lneTUzdtit05F3ouShn24EHBiiBgaDIaEFuxLloFhmb75LxIDP3qQo3ro2UHcxYUqfQfTpJHKn7EwXAyZMNjJQFlGE/L0eLSWeBKpXYat" +
//            "d5juJHWY1UzKYIekZNa51Z+UMpSob/742YRMT9u88QFzccewKBgFrbKJx1Pldg/86Fu+p68+uqpD5pMJXybMBIgTSf8fiebNvi2MbWqFv" +
//            "2mJixsrGMAt9kTuJ5y9wBsWCeC+tob4p4vS2QyE29EkrBHAMsAfuvOl0pMRwm9YdEWZvtezBX4/8TD89sq4HllMRw28dsczCli75Uco6W" +
//            "eZc7RVGSLlaxAoGASD6aqbJsPapmlGRMQjselbAUFoZkx+pVARnYI0wURgPkBJj5iJ4cxP4x08R1WwBwkhVHtA2HbK1aF1xHFmr3f2pxI" +
//            "D+6tSDwm1c9iog310T9DIHHR3WAwFDqaH4EVuN/kXZ7/p+GXHXmEOW3VANArtw46Gq9alLexC9s57CWV6cCgYEAybPCr2sZfvlJePipHW" +
//            "6KQayi4ENcSnI5uvbU6U6U/kMRB41fyAE5whRpWZkx/LGEB3uZaGknUNOosEXpfyXkMhHozXjgCHL3AHyHnwbeV0R6R/DqCRzIgxVMWqj" +
//            "owxUSeymvEvH0Z0i3qj7lyX5jtU4mCD0JY3VOylOjnzj8Wmk=\n" +
//            "-----END PRIVATE KEY-----";
    /**
     * md5 加密
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");// 生成一个MD5加密计算摘要
            md.update(str.getBytes());// 计算md5函数
            return new BigInteger(1, md.digest()).toString(16);// 16是表示转换为16进制数
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * AES 加密
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String aesEncrypt(String str) {
        try {
            Cipher cipher = Cipher.getInstance("AES");// 获取加密对象
            // 创建加密规则
            // 第一个参数key的字节
            // 第二个参数表示加密算法
            SecretKeySpec sks = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            // ENCRYPT_MODE：加密模式
            // DECRYPT_MODE: 解密模式
            // 初始化加密模式和算法
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            byte[] bytes = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));// 加密
            return Base64.getEncoder().encodeToString(bytes); // 返回加密后的数据
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES 解密
     * @param str 需要解密的字符串
     * @return 解密后的明文
     */
    public static String aesDncrypt(String str) {
        try {
            Cipher cipher = Cipher.getInstance("AES");// 获取Cipher对象
            SecretKeySpec sks = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");// 指定密钥规则
            cipher.init(Cipher.DECRYPT_MODE, sks);
            // 解密
            byte[] bytes = Base64.getDecoder().decode(str);
            byte[] decryptedBytes = cipher.doFinal(bytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * RSA 加密
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String rsaEncrypt(String str) {
        try {
            PublicKey publicKey = getPublicKey(RSA_PUBLIC_KEY);
            byte[] encryptedBytes = encrypt(str, publicKey);
            return bytesToHex(encryptedBytes);
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * RSA解密
     * @param str 需要解密的字符串
     * @return 解密后的明文
     */
    public static String rsaDecrypt(String str) {
        try {
            PrivateKey privateKey = getPrivateKey(RSA_PRIVATE_KEY);
            byte[] decryptedBytes = decrypt(hexToBytes(str), privateKey);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成 RSA 签名
     * @param plaintext 需要签名的明文
     * @return 签名字符串
     */
    public static String signature(String plaintext) {
        try {
            PrivateKey signPrivateKey = getPrivateKey(RSA_PRIVATE_KEY);
            return generateSignature(plaintext, signPrivateKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * RSA 验签
     * @param signature 签名
     * @param plaintext 明文
     * @return true验证成功
     */
    public static boolean verifySignature(String signature, String plaintext) {
        try {
            PublicKey verifyPublicKey = getPublicKey(RSA_PUBLIC_KEY);
            return verifySignature(plaintext, signature, verifyPublicKey);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成密钥对
     * @param keysize 密钥长度
     *                keysize=1024--117
     *                keysize=2048--245
     *                keysize=3072--373
     *                keysize=4096--501
     * @return Map 类型
     * {
     *     "PUBLIC_KEY": "公钥",
     *     "PRIVATE_KEY": "私钥"
     * }
     */
    public Map<String , String> generateRSAKey(int keysize) {
        Map<String, String> map = new HashMap<>();
        try {
            // 生成密钥对
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keysize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            // 获取公钥和私钥
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            // 将公钥和私钥转换为PEM格式的字符串
            X509EncodedKeySpec spec1 = new X509EncodedKeySpec(publicKey.getEncoded());
            byte[] encodedKey1 = Base64.getEncoder().encode(spec1.getEncoded());
            String publicKeyStr = "-----BEGIN PUBLIC KEY-----\n" +
                    new String(encodedKey1) +
                    "\n-----END PUBLIC KEY-----";
            PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            byte[] encodedKey2 = Base64.getEncoder().encode(spec2.getEncoded());

            String privateKeyStr = "-----BEGIN PRIVATE KEY-----\n" +
                    new String(encodedKey2) +
                    "\n-----END PRIVATE KEY-----";
            map.put("PUBLIC_KEY",publicKeyStr);
            map.put("PRIVATE_KEY",privateKeyStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 将 PEM 格式的公钥字符串转换为 PublicKey 对象
     * @param publicKeyStr 公钥
     * @return PublicKey 对象
     * @throws Exception 转换发生异常
     */
    private static PublicKey getPublicKey(String publicKeyStr) throws Exception {
        publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----\n", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "");
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 将 PEM 格式的私钥字符串转换为 PrivateKey 对象
     * @param privateKeyStr 私钥
     * @return PrivateKey 对象
     * @throws Exception 转换发生异常
     */
    private static PrivateKey getPrivateKey(String privateKeyStr) throws Exception {
        privateKeyStr = privateKeyStr.replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "");
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 使用公钥进行加密
     * @param plaintext 明文
     * @param publicKey 公钥
     * @return 加密后的字节数组
     * @throws Exception 转换发生异常
     */
    private static byte[] encrypt(String plaintext, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用私钥进行解密
     * @param ciphertext 密文
     * @param privateKey 私钥
     * @return 加密后的字节数组
     * @throws Exception 转换发生异常
     */
    private static byte[] decrypt(byte[] ciphertext, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(ciphertext);
    }

    /**
     * 使用私钥生成数字签名
     * @param plaintext 明文
     * @param privateKey 私钥
     * @return 生成后的数字签名
     * @throws Exception 转换发生异常
     */
    private static String generateSignature(String plaintext, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = signature.sign();
        return bytesToHex(signBytes);
    }

    /**
     * 使用公钥验证数字签名
     * @param plaintext 明文
     * @param signature 签名
     * @param publicKey 公钥
     * @return true验证成功
     * @throws Exception 转换发生异常
     */
    private static boolean verifySignature(String plaintext, String signature, PublicKey publicKey) throws Exception {
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = hexToBytes(signature);
        return verifier.verify(signatureBytes);
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 需要转换的字节数组
     * @return 转换后的十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) { sb.append('0'); }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     * @param hex 需要转换的十六进制字符串
     * @return 转换后的字节数组
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
}

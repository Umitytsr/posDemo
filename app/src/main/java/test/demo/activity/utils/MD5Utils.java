package test.demo.activity.utils;


import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 提供md5相关的函数
 */
public final class MD5Utils {
    private MD5Utils() {
        throw new AssertionError("create instance of MD5Utils is prohibited");
    }

    /**
     * 对字符串进行md5加密
     *
     * @param src 要计算的数据
     * @return MD5签名后的数据
     */
    public static String md5(String src) {
        if (TextUtils.isEmpty(src)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(src.getBytes("UTF-8"));
            return bytes2HexStr(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对字符串进行md5加密
     *
     * @param src 要计算的数据
     * @return MD5签名后的数据
     */
    public static byte[] md5Bytes(String src) {
        if (TextUtils.isEmpty(src)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return digest.digest(src.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成文件的MD5值
     *
     * @param file 源文件
     * @return 生成的MD5值(16进制)
     */
    public static String getFileMD5(File file) {
        byte[] hash = getFileHash(file, "MD5");
        return bytes2HexStr(hash);
    }

    /**
     * 获取文件的SHA-1值
     *
     * @param file 源文件
     * @return 生成的MD5值(16进制)
     */
    public static String getFileSHA1(File file) {
        byte[] hash = getFileHash(file, "SHA-1");
        return bytes2HexStr(hash);
    }

    public static String getFile1SHA1(File file, String info) {
        byte[] hash = getFileHash1(file, info,"SHA-1");
        return bytes2HexStr(hash);
    }

    /**
     * 计算文件的Hash值
     *
     * @param file      文件
     * @param algorithm 算法名称
     * @return 文件的hash值
     */
    private static byte[] getFileHash(File file, String algorithm) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        InputStream is = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            is = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            return digest.digest();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(is);
        }
        return null;
    }

    /**
     * 计算文件+字符串的Hash值
     *
     * @param file      文件
     * @param info      附加信息
     * @param algorithm 算法名称
     * @return hash值
     */
    private static byte[] getFileHash1(File file, String info, String algorithm) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        InputStream is = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            is = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            int lenInfo = info.length();
            if(lenInfo < 1000) {
                if (lenInfo % 2 == 1) {
                    info = info + "0";
                    lenInfo ++;
                }
                String tempString = Utils.bytes2HexString(buffer);
                tempString = tempString.replace(tempString.substring(0, lenInfo), info);
                digest.update(Utils.hexStr2Bytes(tempString));
            }
            return digest.digest();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(is);
        }
        return null;
    }

    /** 将字节数组转换成16进制字符串 */
    private static String bytes2HexStr(byte[] src) {
        if (src == null || src.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(src.length * 2);
        for (byte b : src) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() < 2) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString().toUpperCase();
    }
}

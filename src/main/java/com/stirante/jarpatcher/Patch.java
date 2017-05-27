package com.stirante.jarpatcher;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Patch implements Serializable {

    private HashMap<String, Action> actions = new HashMap<>();

    private Patch() {

    }

    public void applyPatch(File source, File destination) throws IOException {
        destination.createNewFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination));
        out.setMethod(ZipOutputStream.STORED);
        ZipFile src = new ZipFile(source);
        Enumeration<? extends ZipEntry> entries = src.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (actions.containsKey(e.getName())) {
                Action a = actions.get(e.getName());
                if (a.getType() == Action.Type.REMOVE || a.getType() == Action.Type.REMOVE_DIR) {
                    System.out.println("Removing " + e.getName());
                    continue;
                }
                if (a.getType() == Action.Type.CHANGE) {
                    System.out.println("Changing " + e.getName());
                    if (e.isDirectory()) addToZip(out, e.getName());
                    else addToZip(out, a.getFilename(), a.getData());
                }
            } else {
                System.out.println("Leaving " + e.getName());
                if (!e.isDirectory()) {
                    addToZip(out, e.getName(), toByteArray(src.getInputStream(e)));
                } else {
                    addToZip(out, e.getName());
                }
            }
        }
        for (Action a : actions.values()) {
            if (a.getType() == Action.Type.ADD_DIR) {
                System.out.println("Adding " + a.getFilename());
                addToZip(out, a.getFilename());
            } else if (a.getType() == Action.Type.ADD) {
                System.out.println("Adding " + a.getFilename());
                addToZip(out, a.getFilename(), a.getData());
            }
        }
        out.flush();
        out.close();
    }

    private void addToZip(ZipOutputStream out, String name, byte[] data) throws IOException {
        ZipEntry e = new ZipEntry(name);
        e.setSize(data.length);
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        e.setCrc(crc32.getValue());
        e.setCompressedSize(data.length);
        out.putNextEntry(e);
        out.write(data);
        out.closeEntry();
    }

    private void addToZip(ZipOutputStream out, String name) throws IOException {
        ZipEntry e = new ZipEntry(name);
        e.setSize(0);
        CRC32 crc32 = new CRC32();
        e.setCrc(crc32.getValue());
        e.setCompressedSize(0);
        out.putNextEntry(e);
        out.closeEntry();
    }

    public static Patch createPatch(File source, File target) throws IOException {
        Patch p = new Patch();
        ZipFile src = new ZipFile(source);
        ZipFile trg = new ZipFile(target);
        Enumeration<? extends ZipEntry> entries = trg.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            Enumeration<? extends ZipEntry> entries1 = src.entries();
            boolean found = false;
            while (entries1.hasMoreElements()) {
                ZipEntry e1 = entries1.nextElement();
                if (e1.getName().equals(e.getName())) {
                    found = true;
                    if (!e.isDirectory()) {
                        byte[] b = toByteArray(trg.getInputStream(e));
                        byte[] b1 = toByteArray(src.getInputStream(e1));
                        if (!hash(b).equalsIgnoreCase(hash(b1))) {
                            p.actions.put(e.getName(), new Action(Action.Type.CHANGE, e.getName(), b));
                        }
                    }
                    break;
                }
            }
            if (!found)
                p.actions.put(e.getName(), new Action(e.isDirectory() ? Action.Type.ADD_DIR : Action.Type.ADD, e.getName(), toByteArray(trg.getInputStream(e))));
        }
        entries = src.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            Enumeration<? extends ZipEntry> entries1 = trg.entries();
            boolean found = false;
            while (entries1.hasMoreElements()) {
                ZipEntry e1 = entries1.nextElement();
                if (e1.getName().equals(e.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found)
                p.actions.put(e.getName(), new Action(e.isDirectory() ? Action.Type.REMOVE_DIR : Action.Type.REMOVE, e.getName(), null));
        }
        System.out.println("Actions:");
        for (Action action : p.actions.values()) {
            System.out.println(action.getType().name() + " " + action.getFilename());
        }
        return p;
    }

    private static String hash(byte[] b) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(b);
            StringBuilder hexString = new StringBuilder();
            for (byte aByte : bytes) {
                String hex = Integer.toHexString(0xFF & aByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static byte[] toByteArray(InputStream in) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int i;
            while ((i = in.read(buffer)) > 0) {
                baos.write(buffer, 0, i);
            }
            in.close();
            buffer = baos.toByteArray();
            baos.close();
            return buffer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}

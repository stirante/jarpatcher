package com.stirante.jarpatcher;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Main {

    public static void main(String[] args) {
        if (args.length < 4 || args.length > 4) {
            showUsage();
        } else {
            if (args[0].equalsIgnoreCase("create")) {
                File source = new File(args[1]);
                File target = new File(args[2]);
                File patchFile = new File(args[3]);
                try {
                    Patch patch = Patch.createPatch(source, target);
                    ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(patchFile)));
                    oos.writeObject(patch);
                    oos.flush();
                    oos.close();
                    System.out.println("Patch saved at " + patchFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("apply")) {
                File source = new File(args[1]);
                File target = new File(args[2]);
                File patchFile = new File(args[3]);
                try {
                    ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(patchFile)));
                    Patch patch = (Patch) ois.readObject();
                    patch.applyPatch(source, target);
                    System.out.println("Patched jar saved at " + target.getAbsolutePath());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("\tcreate <old> <new> <patch>");
        System.out.println("\tapply <old> <new> <patch>");
    }

}

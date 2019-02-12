package org.wso2.update.descriptor.tester;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.apache.commons.io.FilenameUtils;
import org.wso2.update.descriptor.tester.exceptions.FileNotDeletedException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 */
public class Utils {

    public static String unzip(String zipName, String extractDir) throws IOException, FileNotDeletedException {

        if (!Constants.ZIP.equals(FilenameUtils.getExtension(zipName))) {
            throw new FileNotFoundException(zipName + " is not a zip file");
        }

        try (ZipFile zipFile = new ZipFile(zipName)) {
            FileSystem fileSystem = FileSystems.getDefault();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            System.out.println(FilenameUtils.getExtension(zipName));

            Path extractFilePath =
                    Paths.get(extractDir, FilenameUtils.getName(zipName).replace("." + Constants.ZIP, ""));
            System.out.println(extractFilePath);

            if (Files.exists(extractFilePath)) {
                System.out.println(true);
                deleteDir(extractFilePath);
            }

            //Iterate over entries
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                //If directory then create a new directory in uncompressed folder
                if (entry.isDirectory()) {
                    System.out.println("Creating Directory:" + extractDir + entry.getName());
                    Files.createDirectories(fileSystem.getPath(extractDir + entry.getName()));
                } else {
                    InputStream is = zipFile.getInputStream(entry);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    String uncompressedFileName = extractDir + entry.getName();
                    Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                    Files.createFile(uncompressedFilePath);
                    FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                    while (bis.available() > 0) {
                        fileOutput.write(bis.read());
                    }
                    fileOutput.close();
                    System.out.println("Written :" + entry.getName());
                }
            }

            return extractFilePath.toString();
        }

    }

    /**
     * Delete directory structure.
     *
     * @param dirPath path to the directory which should be deleted.
     */
    private static void deleteDir(Path dirPath) throws FileNotDeletedException {

        Stack<File> directoryStack = new Stack<>();
        File index = new File(dirPath.toUri());
        String[] entries = index.list();

        if (entries == null) {
            throw new FileNotDeletedException("Could not find any file to delete in '" + dirPath + "'");
        }

        for (String entry : entries) {
            File currentFile = new File(index.getPath(), entry);
            if (currentFile.isDirectory()) {
                directoryStack.push(currentFile);
            } else {
                if (!currentFile.delete()) {
                    throw new FileNotDeletedException(currentFile);
                }
            }
        }

        while (!directoryStack.isEmpty()) {
            deleteDir(directoryStack.pop().toPath());
        }
    }

}

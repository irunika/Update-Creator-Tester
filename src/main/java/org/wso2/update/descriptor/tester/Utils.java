package org.wso2.update.descriptor.tester;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {

    public static void unzip(String zipName, String extractDir) throws IOException {
        //Open the file
        try(ZipFile zipFile = new ZipFile(zipName)) {
            FileSystem fileSystem = FileSystems.getDefault();
            //Get zipFile entries
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            System.out.println(Paths.get(extractDir, zipFile.getName().replace(".zip", "")));

            if (Files.exists(Paths.get(extractDir, zipFile.getName().replace(".zip", "")))) {
                System.out.println(true);
            }

            //Iterate over entries
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                //If directory then create a new directory in uncompressed folder
                if (entry.isDirectory())
                {
                    System.out.println("Creating Directory:" + extractDir + entry.getName());
                    Files.createDirectories(fileSystem.getPath(extractDir + entry.getName()));
                }
                //Else create the zipFile
                else
                {
                    InputStream is = zipFile.getInputStream(entry);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    String uncompressedFileName = extractDir + entry.getName();
                    Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                    Files.createFile(uncompressedFilePath);
                    FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                    while (bis.available() > 0)
                    {
                        fileOutput.write(bis.read());
                    }
                    fileOutput.close();
                    System.out.println("Written :" + entry.getName());
                }
            }
        }
    }

}

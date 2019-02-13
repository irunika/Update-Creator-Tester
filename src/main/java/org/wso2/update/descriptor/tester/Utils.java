package org.wso2.update.descriptor.tester;

import org.apache.commons.io.FilenameUtils;
import org.wso2.update.descriptor.tester.exceptions.FileNotDeletedException;
import org.wso2.update.descriptor.tester.model.CompatibleProduct;
import org.wso2.update.descriptor.tester.model.UpdateDescriptor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 */
public class Utils {

    public static boolean checkFileAvailability(String zipHome, UpdateDescriptor updateDescriptor) {
        List<String> filesList = new ArrayList<>();
        CompatibleProduct compatibleProduct = updateDescriptor.getCompatible_products().get(0);
        filesList.addAll(compatibleProduct.getAdded_files());
        filesList.addAll(compatibleProduct.getRemoved_files());
        filesList.addAll(compatibleProduct.getModified_files());

        AtomicBoolean status = new AtomicBoolean(true);
        filesList.forEach(filePath -> {
            Path path = Paths.get(zipHome, Constants.UPDATE_CARBON_HOME, filePath);
            File file = new File(path.toUri());
            if (!file.exists()) {
                status.set(false);
                String message = String.format("%s file is not found in the update zip", filePath);
                System.err.println(message);
            }
        });

        return status.get();
    }


    public static boolean compareUpdates(UpdateDescriptor originalDescriptor, UpdateDescriptor newDescriptor) {
        boolean status = true;

        // Compare platform names
        if (!originalDescriptor.getPlatform_name().equals(newDescriptor.getPlatform_name())) {
            String message = String.format("Platform names are not equal original: %s , new: %s",
                    originalDescriptor.getPlatform_name(), newDescriptor.getPlatform_name());
            System.out.println(message);
            status = false;
        }

        // Compare platform versions
        if (!originalDescriptor.getPlatform_version().equals(newDescriptor.getPlatform_version())) {
            String message = String.format("Platform versions are not equal original: %s , new: %s",
                    originalDescriptor.getPlatform_version(), newDescriptor.getPlatform_version());
            System.out.println(message);
            status = false;
        }

        CompatibleProduct originalCompatibleProduct = originalDescriptor.getCompatible_products().get(0);
        CompatibleProduct newCompatibleProduct = newDescriptor.getCompatible_products().get(0);

        System.out.println("\nComparing modified files...");
        status = status && compareFilesLists(originalCompatibleProduct.getModified_files(),
                newCompatibleProduct.getModified_files(), "modified");

        System.out.println("\nComparing added files...");
        status = status && compareFilesLists(originalCompatibleProduct.getAdded_files(),
                newCompatibleProduct.getAdded_files(), "added");

        System.out.println("\nComparing removed files...");
        status = status && compareFilesLists(originalCompatibleProduct.getRemoved_files(),
                newCompatibleProduct.getRemoved_files(), "modified");

        return status;
    }

    private static boolean compareFilesLists(List<String> originalList, List<String> newList, String fileStatus) {
        Map<String, Boolean> originalFileMap = createFileMap(originalList);
        Map<String, Boolean> newFileMap = createFileMap(newList);

        originalFileMap.keySet().forEach(key -> {
            if (newFileMap.containsKey(key)) {
                originalFileMap.put(key, true);
                newFileMap.put(key, true);
            }
        });

        AtomicBoolean availableAllOriginalFiles = new AtomicBoolean(true);
        originalFileMap.forEach((file, found) -> {
            if (!found) {
                availableAllOriginalFiles.set(false);
                String message = String.format("%s file: %s is missing in the new update", fileStatus, file);
                System.err.println(message);
            }
        });

        AtomicBoolean availableAllNewFiles = new AtomicBoolean(true);
        newFileMap.forEach((file, found) -> {
            if (!found) {
                availableAllNewFiles.set(false);
                String message = String.format("%s file: %s is not available in original update", fileStatus, file);
                System.err.println(message);
            }
        });

        return availableAllOriginalFiles.get() && availableAllNewFiles.get();
    }

    private static Map<String, Boolean> createFileMap(List<String> filesList) {
        Map<String, Boolean> filesMap = new HashMap<>();
        filesList.forEach(file -> filesMap.put(file, false));
        return filesMap;
    }

    public static UpdateDescriptor loadUpdateDescriptor(String pathToUpdateDir) throws FileNotFoundException {

        Path pathToDescriptorYaml = Paths.get(pathToUpdateDir, Constants.UPDATE_DESCRIPTOR_FILE_NAME);
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(UpdateDescriptor.class), representer);
        InputStream inputStream = new FileInputStream(new File(pathToDescriptorYaml.toUri()));
        return yaml.load(inputStream);
    }


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
                    Files.createDirectories(Paths.get(extractDir, entry.getName()));
                } else {
                    InputStream is = zipFile.getInputStream(entry);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    String uncompressedFileName = Paths.get(extractDir, entry.getName()).toString();
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
    public static void deleteDir(Path dirPath) throws FileNotDeletedException {

        File unzipDir = new File(dirPath.toUri());
        String[] entries = unzipDir.list();

        if (entries == null) {
            throw new FileNotDeletedException("Could not find any file to delete in '" + dirPath + "'");
        }

        Stack<File> directoryStack = new Stack<>();
        for (String entry : entries) {
            File currentFile = new File(unzipDir.getPath(), entry);
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

        unzipDir.delete();
    }

}

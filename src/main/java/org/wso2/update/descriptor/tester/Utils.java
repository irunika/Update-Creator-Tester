package org.wso2.update.descriptor.tester;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Utils of the comparision.
 */
public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * Check the availability of the files inside the extracted zip home.
     *
     * @param zipHome          extracted zip home.
     * @param updateDescriptor {@link UpdateDescriptor} of the particular update zip.
     * @return true if all the files are available in the unzipped location.
     */
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
                log.error("{} file is not found in the update zip", filePath);
            }
        });

        return status.get();
    }

    /**
     * Compare the update descriptors of the original and new update zip files.
     *
     * @param originalDescriptor {@link UpdateDescriptor} of the original update zip.
     * @param newDescriptor      {@link UpdateDescriptor} of the new update zip.
     * @return true if all of files in the original descriptor is available in the new descriptor.
     */
    public static boolean compareUpdates(UpdateDescriptor originalDescriptor, UpdateDescriptor newDescriptor) {

        boolean status = true;

        // Compare platform names
        if (!originalDescriptor.getPlatform_name().equals(newDescriptor.getPlatform_name())) {
            log.error("Platform names are not equal original: {} , new: {}", originalDescriptor.getPlatform_name(),
                    newDescriptor.getPlatform_name());
            status = false;
        }

        // Compare platform versions
        if (!originalDescriptor.getPlatform_version().equals(newDescriptor.getPlatform_version())) {
            log.error("Platform versions are not equal original: {} , new: {}",
                    originalDescriptor.getPlatform_version(), newDescriptor.getPlatform_version());
            status = false;
        }

        CompatibleProduct originalCompatibleProduct = originalDescriptor.getCompatible_products().get(0);
        CompatibleProduct newCompatibleProduct = newDescriptor.getCompatible_products().get(0);

        log.info("Comparing modified files...");
        status = status && compareFilesLists(originalCompatibleProduct.getModified_files(),
                newCompatibleProduct.getModified_files(), "modified");

        log.info("Comparing added files...");
        status = status && compareFilesLists(originalCompatibleProduct.getAdded_files(),
                newCompatibleProduct.getAdded_files(), "added");

        log.info("Comparing removed files...");
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
                log.error("{} file: {} is missing in the new update", fileStatus, file);
            }
        });

        AtomicBoolean availableAllNewFiles = new AtomicBoolean(true);
        newFileMap.forEach((file, found) -> {
            if (!found) {
                availableAllNewFiles.set(false);
                log.error("{} file: {} is not available in original update", fileStatus, file);
            }
        });

        return availableAllOriginalFiles.get() && availableAllNewFiles.get();
    }

    private static Map<String, Boolean> createFileMap(List<String> filesList) {

        Map<String, Boolean> filesMap = new HashMap<>();
        filesList.forEach(file -> filesMap.put(file, false));
        return filesMap;
    }

    /**
     * Load update descriptor yaml from the file.
     *
     * @param pathToUpdateDir path to the extracted update directory.
     * @return the loaded {@link UpdateDescriptor}.
     * @throws FileNotFoundException if the update-descriptor3.yaml is not found in the update directory.
     */
    public static UpdateDescriptor loadUpdateDescriptor(String pathToUpdateDir) throws FileNotFoundException {

        Path pathToDescriptorYaml = Paths.get(pathToUpdateDir, Constants.UPDATE_DESCRIPTOR_FILE_NAME);
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(UpdateDescriptor.class), representer);
        InputStream inputStream = new FileInputStream(new File(pathToDescriptorYaml.toUri()));
        return yaml.load(inputStream);
    }

    /**
     * Unzip the given product directory.
     *
     * @param pathToZip  path to the zip file.
     * @param extractDir directory which the zip should be extracted to.
     * @return the location of the extracted directory.
     * @throws IOException             if any IO issue occurred.
     * @throws FileNotDeletedException if the given zip file is not found.
     */
    public static String unzip(String pathToZip, String extractDir) throws IOException, FileNotDeletedException {

        log.info("Extracting {}...", pathToZip);
        if (!Constants.ZIP.equals(FilenameUtils.getExtension(pathToZip))) {
            throw new FileNotFoundException(pathToZip + " is not a zip file");
        }

        try (ZipFile zipFile = new ZipFile(pathToZip)) {
            FileSystem fileSystem = FileSystems.getDefault();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            Path extractFilePath =
                    Paths.get(extractDir, FilenameUtils.getName(pathToZip).replace("." + Constants.ZIP, ""));
            if (Files.exists(extractFilePath)) {
                deleteDir(extractFilePath);
            }

            //Iterate over entries
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                //If directory then create a new directory in uncompressed folder
                if (entry.isDirectory()) {
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
                }
            }

            log.info("Successfully extracted to {}", extractFilePath);
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

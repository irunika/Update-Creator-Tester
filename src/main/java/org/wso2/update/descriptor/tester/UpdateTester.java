package org.wso2.update.descriptor.tester;

import org.wso2.update.descriptor.tester.exceptions.FileNotDeletedException;
import org.wso2.update.descriptor.tester.model.UpdateDescriptor;

import java.io.IOException;
import java.nio.file.Paths;

public class UpdateTester {

    public static void main(String[] args) throws IOException, FileNotDeletedException {

        String originalZip = "/home/irunika/Desktop/WSO2-CARBON-UPDATE-4.4.0-3550.zip";
        String newZip = "/home/irunika/Desktop/WSO2-CARBON-UPDATE-4.4.0-3550.zip";
        String extractDirOld = "/tmp/old/";
        String extractDirNew = "/tmp/new/";

        String originalZipExtractLocation = Utils.unzip(originalZip, extractDirOld);
        String newZipExtractLocation = Utils.unzip(newZip, extractDirNew);

        try {
            UpdateDescriptor originalUpdateDescriptor = Utils.loadUpdateDescriptor(originalZipExtractLocation);
            UpdateDescriptor newUpdateDescriptor = Utils.loadUpdateDescriptor(newZipExtractLocation);

            boolean comparisionSuccessful = Utils.compareUpdates(originalUpdateDescriptor, newUpdateDescriptor);

            System.out.println();
            if (comparisionSuccessful) {
                System.out.println("Comparision is successful!");
            } else {
                System.out.println("Comparision is failed!");
            }
        } finally {
            Utils.deleteDir(Paths.get(originalZipExtractLocation));
            Utils.deleteDir(Paths.get(newZipExtractLocation));
        }
    }

}

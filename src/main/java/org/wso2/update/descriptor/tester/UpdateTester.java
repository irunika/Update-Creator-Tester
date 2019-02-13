package org.wso2.update.descriptor.tester;

import org.wso2.update.descriptor.tester.exceptions.FileNotDeletedException;
import org.wso2.update.descriptor.tester.model.UpdateDescriptor;

import java.io.IOException;

public class UpdateTester {

    public static void main(String[] args) throws IOException, FileNotDeletedException {

        String originalZip = "/home/irunika/Desktop/WSO2-CARBON-UPDATE-4.4.0-3550.zip";
        String newZip = "/home/irunika/Desktop/WSO2-CARBON-UPDATE-4.4.0-3550.zip";

        String originalZipExtractLocation = Utils.unzip(originalZip, Constants.EXTRACT_DIR_ORIGINAL);
        String newZipExtractLocation = Utils.unzip(newZip, Constants.EXTRACT_DIR_NEW);

        UpdateDescriptor originalUpdateDescriptor = Utils.loadUpdateDescriptor(originalZipExtractLocation);
        UpdateDescriptor newUpdateDescriptor = Utils.loadUpdateDescriptor(newZipExtractLocation);

        if (Utils.compareUpdates(originalUpdateDescriptor, newUpdateDescriptor)) {
            System.out.println("\nComparision is successful!");

            System.out.println("\nChecking files availability in the update");

            if (Utils.checkFileAvailability(newZipExtractLocation, originalUpdateDescriptor)) {
                System.out.println("\nChecking files availability is successful!");
            } else {
                System.err.println("\nChecking files availability is failed!");
            }
        } else {
            System.out.println("\nComparision is failed!");
        }
    }

}

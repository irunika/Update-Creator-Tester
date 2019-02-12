package org.wso2.update.descriptor.tester;

import org.wso2.update.descriptor.tester.exceptions.FileNotDeletedException;
import org.wso2.update.descriptor.tester.model.UpdateDescriptor;

import java.io.IOException;

public class UpdateTester {

    public static void main(String[] args) throws IOException, FileNotDeletedException {

        String originalZip = "/home/irunika/Desktop/WSO2-CARBON-UPDATE-4.4.0-3550.zip";
        String newZip = "/home/irunika/Desktop/WSO2-CARBON-UPDATE-4.4.0-3550.zip";
        String extractDirOld = "/tmp/old/";
        String extractDirNew = "/tmp/new/";

        String originalZipExtractLocation = Utils.unzip(originalZip, extractDirOld);
        String newZipExtractLocation = Utils.unzip(newZip, extractDirNew);

        System.out.println(originalZipExtractLocation);
        System.out.println(newZipExtractLocation);

        UpdateDescriptor originalUpdateDescriptor = Utils.loadUpdateDescriptor(originalZipExtractLocation);
        UpdateDescriptor newUpdateDescriptor = Utils.loadUpdateDescriptor(newZipExtractLocation);

        System.out.println(originalUpdateDescriptor.getPlatform_version());
    }

}

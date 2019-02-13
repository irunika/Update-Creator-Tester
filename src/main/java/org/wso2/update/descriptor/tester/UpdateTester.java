package org.wso2.update.descriptor.tester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.update.descriptor.tester.exceptions.FileNotDeletedException;
import org.wso2.update.descriptor.tester.model.UpdateDescriptor;

import java.io.IOException;

/**
 * Main class of the project.
 */
public class UpdateTester {

    private static final Logger log = LoggerFactory.getLogger(UpdateTester.class);

    public static void main(String[] args) throws IOException, FileNotDeletedException {

        if (args.length != 2) {
            throw new IllegalArgumentException("Give exactly two arguments!");
        }

        String originalZip = args[0];
        String newZip = args[1];

        String originalZipExtractLocation = Utils.unzip(originalZip, Constants.EXTRACT_DIR_ORIGINAL);
        String newZipExtractLocation = Utils.unzip(newZip, Constants.EXTRACT_DIR_NEW);

        UpdateDescriptor originalUpdateDescriptor = Utils.loadUpdateDescriptor(originalZipExtractLocation);
        UpdateDescriptor newUpdateDescriptor = Utils.loadUpdateDescriptor(newZipExtractLocation);

        if (Utils.compareUpdates(originalUpdateDescriptor, newUpdateDescriptor)) {
            log.info("Comparision is successful!");
            log.info("Checking files availability in the update");
            if (Utils.checkFileAvailability(newZipExtractLocation, originalUpdateDescriptor)) {
                log.info("Checking files availability is successful!");
            } else {
                log.error("Checking files availability is failed!");
            }
        } else {
            log.error("Comparision is failed!");
        }
    }

}

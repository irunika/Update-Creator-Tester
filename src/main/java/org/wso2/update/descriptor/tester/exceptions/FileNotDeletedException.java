package org.wso2.update.descriptor.tester.exceptions;

import java.io.File;

/**
 * Exception class to identify if any file could not be deleted.
 */
public class FileNotDeletedException extends Exception {

    public FileNotDeletedException(File file) {

        super("Could not delete file `" + file.getName() + "'");
    }

    public FileNotDeletedException(String message) {

        super(message);
    }
}

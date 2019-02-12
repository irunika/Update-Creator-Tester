package org.wso2.update.descriptor.tester.exceptions;

import java.io.File;

public class FileNotDeletedException extends Exception {

    public FileNotDeletedException(File file) {
        super("Could not delete file `" + file.getName() + "'");
    }

    public FileNotDeletedException(String message) {
        super(message);
    }
}

package org.wso2.update.descriptor.tester;

import java.nio.file.Paths;

/**
 * Holds all the constants of the project.
 */
public class Constants {

    public static final String ZIP = "zip";
    public static final String UPDATE_DESCRIPTOR_FILE_NAME = "update-descriptor3.yaml";
    public static final String UPDATE_CARBON_HOME = "carbon.home";
    public static final String EXTRACT_DIR = Paths.get("/tmp", "update-tester").toString();
    public static final String EXTRACT_DIR_ORIGINAL = Paths.get(EXTRACT_DIR, "original").toString();
    public static final String EXTRACT_DIR_NEW = Paths.get(EXTRACT_DIR, "new").toString();

}

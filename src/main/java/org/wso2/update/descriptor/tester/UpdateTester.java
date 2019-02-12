package org.wso2.update.descriptor.tester;

import org.wso2.update.descriptor.tester.exceptions.FileNotDeletedException;

import java.io.IOException;

public class UpdateTester {

    public static void main(String[] args) throws IOException, FileNotDeletedException {

        String unzipLocation =
                Utils.unzip("/home/irunika/Desktop/WSO2-CARBON-UPDATE-4.4.0-3550.zip", "/tmp/");

        System.out.println(unzipLocation);
    }

}

package org.wso2.update.descriptor.tester.model;

import java.util.List;

/**
 * Class to load compatible_products from the update-descriptor3.yaml.
 */
public class Product {

    private String product_name;
    private String product_version;
    private List<String> added_files;
    private List<String> removed_files;
    private List<String> modified_files;

    public String getProduct_name() {

        return product_name;
    }

    public void setProduct_name(String product_name) {

        this.product_name = product_name;
    }

    public String getProduct_version() {

        return product_version;
    }

    public void setProduct_version(String product_version) {

        this.product_version = product_version;
    }

    public List<String> getAdded_files() {

        return added_files;
    }

    public void setAdded_files(List<String> added_files) {

        this.added_files = added_files;
    }

    public List<String> getRemoved_files() {

        return removed_files;
    }

    public void setRemoved_files(List<String> removed_files) {

        this.removed_files = removed_files;
    }

    public List<String> getModified_files() {

        return modified_files;
    }

    public void setModified_files(List<String> modified_files) {

        this.modified_files = modified_files;
    }
}

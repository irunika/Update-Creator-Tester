package org.wso2.update.descriptor.tester.model;

import java.util.List;

/**
 * Class to load update-descriptor3.yaml
 */
public class UpdateDescriptor {

    private String update_number;
    private String platform_version;
    private String platform_name;
    private List<Product> compatible_products;
    private List<Product> partially_applicable_products;

    public String getUpdate_number() {

        return update_number;
    }

    public void setUpdate_number(String update_number) {

        this.update_number = update_number;
    }

    public String getPlatform_version() {

        return platform_version;
    }

    public void setPlatform_version(String platform_version) {

        this.platform_version = platform_version;
    }

    public String getPlatform_name() {

        return platform_name;
    }

    public void setPlatform_name(String platform_name) {

        this.platform_name = platform_name;
    }

    public List<Product> getCompatible_products() {

        return compatible_products;
    }

    public void setCompatible_products(List<Product> compatible_products) {

        this.compatible_products = compatible_products;
    }

    public List<Product> getPartially_applicable_products() {

        return partially_applicable_products;
    }

    public void setPartially_applicable_products(List<Product> partially_applicable_products) {

        this.partially_applicable_products = partially_applicable_products;
    }
}

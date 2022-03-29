package com.techvg.inventory.management.web.rest.vm;

public class UploadFileResponse {

    private String fileName;
    private Long productId;

    //	  private  String dateOfCreation;

    public UploadFileResponse(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}

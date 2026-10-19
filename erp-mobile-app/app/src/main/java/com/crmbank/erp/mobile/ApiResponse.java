package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("status")
    public int status;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public T data;

    @SerializedName("timestamp")
    public String timestamp;

    public boolean isSuccess() {
        return status == 200;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
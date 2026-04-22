package com.my.ai.cursor.interfaces.pojo.vo;

import lombok.Data;

/**
 * Response
 *
 * @author 刘强
 * @version 2026/04/15 17:24
 **/
@Data
public class Response<T> {

    private int responseCode;

    private String message;

    private T data;

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setResponseCode(200);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> Response<T> error(int responseCode, String message) {
        Response<T> response = new Response<>();
        response.setResponseCode(responseCode);
        response.setMessage(message);
        return response;
    }
}

package com.myproject.microservice.service;

import com.squareup.okhttp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RestServiceComponent {
    private static final Logger log = LoggerFactory.getLogger(RestServiceComponent.class);
   // private static final String TOKEN = "perm:c2FuamF5ZGE=.c2FuamF5ZGE=.OAOvylljZWi0CwSM3NJrLFhnB9cjAG"; // AS sanjayda
    private String TOKEN = "perm:bG9ncmVwb3J0ZXI=.bG9ncmVwb3J0ZXI=.AQWRAebetpVCRrrjksAcXsLwIkPOI9"; // AS logreporter
    OkHttpClient client;

    public RestServiceComponent() {
        client = new OkHttpClient();
        log.info("RestServiceComponent is up now");
    }

    public void setTOKEN(String TOKEN) {
        this.TOKEN = TOKEN;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public Request.Builder buildRequest(String url) {
        RequestBody body = RequestBody.create(null, new byte[0]);
        Request.Builder request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + TOKEN)
                .url(url); // "http://localhost:8081/clients"
        return request;
    }
    public Response runPutWithEmptyBOdy(String url)
    {
        Request.Builder builder = buildRequest(url);
        builder.put(RequestBody.create(null, new byte[0]));
        Request request = builder.build();
        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            log.error("IOException "+e.getMessage());
            return null;
        }
    }
    public Response runPost(String url, String body)
    {
        MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        Request.Builder builder = buildRequest(url);
        builder.post(RequestBody.create(FORM, body));
        Request request = builder.build();
        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            log.error("IOException "+e.getMessage());
            return null;
        }
    }
    public Response runGet(String url)
    {
        Request.Builder builder = buildRequest(url);
        Request request = builder.build();
        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            log.error("IOException "+e.getMessage());
            return null;
        }
    }
}

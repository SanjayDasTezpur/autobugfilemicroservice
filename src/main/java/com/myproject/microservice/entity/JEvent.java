package com.myproject.microservice.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sanjayda on 12/11/2018 at 6:02 PM
 */
public class JEvent
{

    public String timeStamp;
    public String threadName;
    public String event;
    public String packageName;
    public String message;
    public List<String> stackTrace = new ArrayList<>();

}

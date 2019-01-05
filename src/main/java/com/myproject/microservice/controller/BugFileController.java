package com.myproject.microservice.controller;

import com.myproject.microservice.entity.EEvent;
import com.myproject.microservice.entity.Event;
import com.myproject.microservice.entity.JEvent;
import com.myproject.microservice.service.AutoBugFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sanjayda on 12/11/2018 at 5:35 PM
 */
@RestController
@RequestMapping("/bug")
public class BugFileController
{
    private static final Logger log = LoggerFactory.getLogger(BugFileController.class);

    @Autowired
    AutoBugFileService abs;

    @RequestMapping(value = "/file/{hostname}/{product}", method = RequestMethod.POST)
    public String fileBugByLoggerUser(@PathVariable("hostname") String hostname, @PathVariable("product") String product, @RequestBody JEvent jevent)
    {
        try {
            Event event = new Event(jevent);
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jevent.timeStamp);
            event.setTimeStamp(date);
            event.setEvent(EEvent.valueOf(jevent.event));
            abs.fileBugAsLogReporter(event,product,hostname);
            return HttpStatus.OK.toString();
        } catch (Exception e){
            log.error(e.getMessage());
            return HttpStatus.EXPECTATION_FAILED.toString();
        }
    }
}

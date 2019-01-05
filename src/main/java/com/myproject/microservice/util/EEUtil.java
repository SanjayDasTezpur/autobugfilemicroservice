package com.myproject.microservice.util;

import com.myproject.microservice.entity.ElasticEvent;
import com.myproject.microservice.entity.Event;

/**
 * Created by sanjayda on 12/11/2018 at 5:09 PM
 */
public class EEUtil {
    public static ElasticEvent makeElasticEvent(Event event)
    {
        ElasticEvent eevent = new ElasticEvent();

        eevent.setId(event.getUniqueID());
        eevent.setEvent(event.getEvent());
        eevent.setMessage(event.getMessage());
        eevent.setPackageName(event.getPackageName());
        eevent.setThreadName(event.getThreadName());
        eevent.setStackTrace(event.getStackTrace());
        eevent.setTimeStamp(event.getTimeStamp());
        /*try {
            ElasticEvent retEvent = eservice.save(eevent);
        }catch (Exception e){
            logger.error("Exception found in Elastic saving operation \n"+e.getMessage());
        }*/
        return eevent;
    }
}

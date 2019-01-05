package com.myproject.microservice.service;

import com.myproject.microservice.entity.Event;
import com.myproject.microservice.youtrack.YouTrackFileBug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by sanjayda on 12/11/2018 at 5:15 PM
 */

@Service
public class AutoBugFileService {

    @Autowired
    YouTrackFileBug youTrackFileBug;

    @Autowired
    RestServiceComponent restServiceComponent;

    public void fileBugAsLogReporter(Event event, String sProductName, String sClientHostName)
    {
        youTrackFileBug.fileBugOnYoutrack(event,sProductName,sClientHostName);
    }

    @Deprecated
    public void fileBugByUser(Event event, String sProductName, String sClientHostName, String userToken)
    {
        String token = restServiceComponent.getTOKEN();
        restServiceComponent.setTOKEN(userToken);
        youTrackFileBug.fileBugOnYoutrack(event,sProductName,sClientHostName);
        restServiceComponent.setTOKEN(token);
    }

}

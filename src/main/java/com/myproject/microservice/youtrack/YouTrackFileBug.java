package com.myproject.microservice.youtrack;

import com.myproject.microservice.entity.ElasticEvent;
import com.myproject.microservice.entity.Event;
import com.myproject.microservice.service.RestServiceComponent;
import com.myproject.microservice.service.YTProjectMap;
import com.myproject.microservice.util.EEUtil;
import com.squareup.okhttp.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class YouTrackFileBug {
    private static final Logger log = LoggerFactory.getLogger(YouTrackFileBug.class);

    @Autowired
    YoutrackConfiguration  yConf;

    @Autowired
    RestServiceComponent restServiceComponent;

    @Autowired
    YTProjectMap map;


    public void  fileBugOnYoutrack(Event event, String sProduct, String sHost)
    {

        if(event.getEvent().name().equalsIgnoreCase("ERROR") && !event.getStackTrace().isEmpty() )
        {
            ElasticEvent elasticEvent = EEUtil.makeElasticEvent(event);
            if(decideToFileBug(elasticEvent.toStackTrace() + elasticEvent.getMessage()) && decideToFileBug(elasticEvent)) {
                String url = makeYTBugFileAPI(sProduct, elasticEvent.getMessage(), elasticEvent.toStackTrace(),sHost);
                if(null == url)
                    return;
                Response response = restServiceComponent.runPutWithEmptyBOdy(url);
                String[] locations = response.header("Location").split("/");
                String newBugID = locations[locations.length-1];
                updateBug(newBugID);
               // map.getBugMap().put(makeHash(elasticEvent),locations[locations.length-1]);
                map.registerBug(makeHash(elasticEvent),newBugID);
            }
            else {
                log.info("Error/Exception caught but not auto filed in youtrack, because Loggy decided not to file bug");
            }
        }
    }

    private String makeYTBugFileAPI(String sProject, String summary, String description, String host){
        if(null == map.getProjectMap().get(sProject)){
            log.info("Project not configured in loggy to file bug in youtrack");
            return null;
        }
        summary = "Exception found, Message - " + summary ;
        description = "Found in host " + host + "\n" + description;
        String sApi = "/rest/issue?project=";
        String youtrackEndPoint = yConf.getYoutrackEndPoint();
        String sSummary = "&summary=" + summary.replaceAll(" ","+") + "&";
        String sDescription = "description=" + description.replaceAll(" ","+");
        String finalURL = youtrackEndPoint+sApi + map.getProjectMap().get(sProject)+sSummary+sDescription;
        return finalURL;
    }
    private void updateBug(String BugID){
        String youtrackEndPoint = yConf.getYoutrackEndPoint();
        String sApi = "/rest/issue/" + BugID + "/execute";
        String finalURL = youtrackEndPoint + sApi;
        String toBugCommand = "command=Subsystem%20backend%20Found%20In%20Integration%20Enviroment%20Found%20in%20Version%20None";
        Response response = restServiceComponent.runPost(finalURL, toBugCommand);
        restServiceComponent.runPost(finalURL,"command=Type%20Bug");
        try {
            if(response.isSuccessful())
                log.info("Issue Updated to Bug " + response.body().string());
            else
                log.error("Issue Update to Bug is failed " + response.body().string());
        } catch (IOException e){
            log.error("Issue Update to Bug is failed  (IOException caught)" + e.getMessage());
        }
    }

    private boolean decideToFileBug(String stack){
        boolean bVal = false;
        bVal = ExceptionChecker.checkAllowedException(stack);
        if(bVal == false){
            log.info("The caught Exception is not listed in ExceptionChecker List");
        }
        return bVal;
    }

    private boolean decideToFileBug(ElasticEvent eevent){
        String hash = makeHash(eevent);
        if(map.getBugMap().isEmpty())
        {
            return true;
        }
        String bugID = map.getBugMap().containsKey(hash) ? map.getBugMap().get(hash) : null;
        if( null == bugID){
            return true;
        }
        String finalUrl = yConf.getYoutrackEndPoint()+"/rest/issue/" + bugID;
        Response response = restServiceComponent.runGet(finalUrl);
        if(response.code()!= 200)
        {
            log.error("Bug Search in Youtrack is not successfull, Code :- "+response.code());
            return true;
        }
        else {
            log.info("Bug Search in Youtrack is successfull, Code :- "+response.code());
        }
        String xml = resultResponsBody(response);
        String state = findStateOfBug(xml);

        if (state.equalsIgnoreCase("Fixed"))
        {
            return true;
        }
        return false;
    }

    private String makeHash(ElasticEvent eevent) {
        String javaFilenames ="";
        String strace = eevent.toStackTrace();
        String[] traceLine = strace.split("\n");
        for(String str : traceLine){
            String[] className = str.split("\\(");
            if(className.length>1) {
                String[] fileName = className[1].split(":");
                javaFilenames = javaFilenames + fileName[0];
            }
        }
        String hashID = ( eevent.getMessage() + javaFilenames ).replaceAll(" ", "").replaceAll("\n", "").replaceAll("\t","");
        if(hashID.length() >250) {
            hashID = hashID.substring(0, 250);
        }
        return hashID;
    }

    private String resultResponsBody(Response response )
    {
        String xml = null;
        try {
            xml = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Got IOException In getting Response In string"+e.getMessage());
        }
        return xml;
    }

    private String findStateOfBug(String xml)
    {
        String state = "";
        try {
            JSONObject jsonObject = XML.toJSONObject(xml);
            JSONObject issue = (JSONObject) jsonObject.get("issue");
            JSONArray field = (JSONArray) issue.get("field");
            for (Object attr : field)
            {
                String name = (String) ((JSONObject) attr).get("name");
                if(name.equalsIgnoreCase("State"))
                {
                    state = (String) ((JSONObject) attr).get("value");
                }

            }
        } catch (JSONException e){
            log.error("JSONException found "+e.getMessage());
        } catch (Exception e){
            log.error("Exception found "+e.getMessage());
        }
        return state;
    }


}

package nl.ou.testar.HtmlReporting;

import nl.ou.testar.a11y.reporting.HTMLReporter;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import java.io.File;
import java.io.PrintWriter;
import java.util.Set;

public class HtmlSequenceReport {

    private boolean firstStateAdded = false;
    private boolean firstActionsAdded = false;

    private static final String[] HEADER = new String[] {
            "<!DOCTYPE html>",
            "<html>",
            "<head>",
            "<title>TESTAR execution sequence report</title>",
            "</head>",
            "<body>"
    };

    private PrintWriter out;
    private static final String REPORT_FILENAME_PRE = "output/HTMLreports/TESTAR_run_";
    private static final String REPORT_FILENAME_MID ="_sequence_";
    private static final String REPORT_FILENAME_AFT = ".html";

    private int sequenceCounter = 0;
    private int scenarioCount = 1;
    public int getScenarioCount() {
        return scenarioCount;
    }

    public HtmlSequenceReport(int sequenceNumber) {
        // checking whether HTMLreports folder exists in output folder, if not, creating it:
        try{
            File folder = new File("output/HTMLreports");
            if(folder.exists()&&folder.isDirectory()){
                System.out.println("DEBUG: HTMLreports folder exists already");
                // all good, folder is already there
            }else{
                System.out.println("DEBUG: HTMLreports folder does not exist, creating");
                //folder is missing, let's create it
                folder.mkdir();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            //TODO put filename into settings, name with sequence number
            // creating a new file for the report:
            String filename = REPORT_FILENAME_PRE + "1_sequence_1.html"; // will be replaced
            // finding the first unused scenario number when sequence number is 1:
            boolean newFilenameFound = false;
            while(!newFilenameFound){
                filename = REPORT_FILENAME_PRE+scenarioCount+REPORT_FILENAME_MID+sequenceNumber+REPORT_FILENAME_AFT;
                File file = new File(filename);
                if(file.exists()){
                    scenarioCount++;
                }else{
                    newFilenameFound = true;
                }
            }
//            System.out.println("Starting sequence report into file: "+filename);
            out = new PrintWriter(filename, HTMLReporter.CHARSET);
            for(String s:HEADER){
                write(s);
            }
            write("<h1>TESTAR execution sequence report for sequence "+sequenceNumber+"</h1>");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public HtmlSequenceReport(int scenarioCount, int sequenceNumber) {
        // checking whether HTMLreports folder exists in output folder, if not, creating it:
        try{
            File folder = new File("output/HTMLreports");
            if(folder.exists()&&folder.isDirectory()){
                System.out.println("DEBUG: HTMLreports folder exists already");
                // all good, folder is already there
            }else{
                System.out.println("DEBUG: HTMLreports folder does not exist, creating");
                //folder is missing, let's create it
                folder.mkdir();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            //TODO put filename into settings, name with sequence number
            // creating a new file for the report:
            String filename = REPORT_FILENAME_PRE + "_1_sequence_1.html"; // will be replaced
            // finding the first unused scenario number when sequence number is 1:
            boolean newFilenameFound = false;
            while(!newFilenameFound){
                filename = REPORT_FILENAME_PRE+scenarioCount+REPORT_FILENAME_MID+sequenceNumber+REPORT_FILENAME_AFT;
                File file = new File(filename);
                if(file.exists()){
                    scenarioCount++;
                }else{
                    newFilenameFound = true;
                }
            }
            //updating the scenarioCount:
            this.scenarioCount = scenarioCount;
//            System.out.println("Starting sequence report into file: "+filename);
            out = new PrintWriter(filename, HTMLReporter.CHARSET);
            for(String s:HEADER){
                write(s);
            }
            write("<h1>TESTAR execution sequence report for sequence "+sequenceNumber+"</h1>");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addTitle(int h, String text){
        write("<h"+h+">"+text+"</h"+h+">");
    }

    public void addSequenceStep(State state, String actionImagePath){
        String imagePath = state.get(Tags.ScreenshotPath);
        // repairing the file paths:
        if(imagePath.contains("./output")){
            imagePath = imagePath.replace("./output","../");
        }
        write("<h4>State:</h4>");
        write("<p><img src=\""+imagePath+"\"></p>");
        write("<h4>Action:</h4>");
        write("<p><img src=\""+actionImagePath+"\"></p>");
    }

    public void addState(State state){
        if(firstStateAdded){
            if(firstActionsAdded){
                writeStateIntoReport(state);
            }else{
                //don't write the state as it is the same - getState is run twice in the beginning, before the first action
            }
        }else{
            firstStateAdded = true;
            writeStateIntoReport(state);
        }
    }

    private void writeStateIntoReport(State state){
        String imagePath = state.get(Tags.ScreenshotPath);
        if(imagePath.contains("./output")){
            imagePath = imagePath.replace("./output","../");
        }
        write("<h2>State "+sequenceCounter+"</h2>");
        write("<h4>concreteID="+state.get(Tags.ConcreteID)+"</h4>");
        try{if(state.get(Tags.AbstractID)!=null) write("<h4>abstractID="+state.get(Tags.AbstractID)+"</h4>");}catch(Exception e){}
//        try{if(state.get(Tags.Abstract_R_ID)!=null) write("<h4>Abstract_R_ID="+state.get(Tags.Abstract_R_ID)+"</h4>");}catch(Exception e){}
//        try{if(state.get(Tags.Abstract_R_T_ID)!=null) write("<h4>Abstract_R_T_ID="+state.get(Tags.Abstract_R_T_ID)+"</h4>");}catch(Exception e){}
//        try{if(state.get(Tags.Abstract_R_T_P_ID)!=null) write("<h4>Abstract_R_T_P_ID="+state.get(Tags.Abstract_R_T_P_ID)+"</h4>");}catch(Exception e){}
        write("<p><img src=\""+imagePath+"\"></p>"); //<img src="smiley.gif" alt="Smiley face" height="42" width="42">
        // file:///E:/TESTAR/TESTAR_dev/testar/target/install/testar/bin/output/output/scrshots/sequence1/SC1padzu12af1193500371.png
        // statePath=./output\scrshots\sequence1\SC1y2bsuu2b02920826651.png
        sequenceCounter++;
    }


    public void addActions(Set<Action> actions){
        if(!firstActionsAdded) firstActionsAdded = true;
        write("<h4>Set of actions:</h4><ul>");
        for(Action action:actions){
            write("<li>");
//            try{if(action.get(Tags.Role)!=null) write("--Role="+action.get(Tags.Role));}catch(Exception e){}
//            try{if(action.get(Tags.Targets)!=null) write("--Targets="+action.get(Tags.Targets));}catch(Exception e){}
            try{if(action.get(Tags.Desc)!=null) write("<b>"+action.get(Tags.Desc)+"</b>  || ");}catch(Exception e){}
            write(action.toString());
            write(" || ConcreteId="+action.get(Tags.ConcreteID));
            try{if(action.get(Tags.AbstractID)!=null) write(" || AbstractId="+action.get(Tags.AbstractID));}catch(Exception e){}
            try{if(action.get(Tags.Abstract_R_ID)!=null) write(" || Abstract_R_ID="+action.get(Tags.Abstract_R_ID));}catch(Exception e){}
            try{if(action.get(Tags.Abstract_R_T_ID)!=null) write(" || Abstract_R_T_ID="+action.get(Tags.Abstract_R_T_ID));}catch(Exception e){}
            try{if(action.get(Tags.Abstract_R_T_P_ID)!=null) write(" || Abstract_R_T_P_ID="+action.get(Tags.Abstract_R_T_P_ID));}catch(Exception e){}
            write("</li>");
        }
        write("</ul>");
    }

    public void addActionsAndUnvisitedActions(Set<Action> actions, Set<String> concreteIdsOfUnvisitedActions){
        if(!firstActionsAdded) firstActionsAdded = true;
        if(actions.size()==concreteIdsOfUnvisitedActions.size()){
            write("<h4>Set of actions (all unvisited - a new state):</h4><ul>");
            for(Action action:actions){
                write("<li>");
                try{if(action.get(Tags.Desc)!=null) write("<b>"+action.get(Tags.Desc)+"</b>");}catch(Exception e){}
                write(" || ConcreteID="+action.get(Tags.ConcreteID)+" || "+action.toString());
                write("</li>");
            }
            write("</ul>");
        }else if(concreteIdsOfUnvisitedActions.size()==0){
            write("<h4>All actions have been visited, set of available actions:</h4><ul>");
            for(Action action:actions){
                write("<li>");
                try{if(action.get(Tags.Desc)!=null) write("<b>"+action.get(Tags.Desc)+"</b>");}catch(Exception e){}
                write(" || ConcreteID="+action.get(Tags.ConcreteID)+" || "+action.toString());
                write("</li>");
            }
            write("</ul>");
        }else{
            write("<h4>"+concreteIdsOfUnvisitedActions.size()+" out of "+actions.size()+" actions have not been visited yet:</h4><ul>");
            for(Action action:actions){
                if(concreteIdsOfUnvisitedActions.contains(action.get(Tags.ConcreteID))){
                    //action is unvisited -> showing:
                    write("<li>");
                    try{if(action.get(Tags.Desc)!=null) write("<b>"+action.get(Tags.Desc)+"</b>");}catch(Exception e){}
                    write(" || ConcreteID="+action.get(Tags.ConcreteID)+" || "+action.toString());
                    write("</li>");
                }
            }
            write("</ul>");
        }
    }

    public void addSelectedAction(String state_path, Action action){
//        System.out.println("path="+state_path);
        String actionPath = state_path.substring(0,state_path.indexOf(".png"));
//        System.out.println("path="+actionPath);
        actionPath = actionPath+"_"+action.get(Tags.ConcreteID)+".png";
//        System.out.println("path="+actionPath);
        write("<h2>Selected Action "+sequenceCounter+" leading to State "+sequenceCounter+"\"</h2>");
        write("<h4>concreteID="+action.get(Tags.ConcreteID));
        try{if(action.get(Tags.Desc)!=null) write(" || "+action.get(Tags.Desc));}catch(Exception e){}
        write("</h4>");
        if(actionPath.contains("./output")){
            actionPath = actionPath.replace("./output","..");
        }
        write("<p><img src=\""+actionPath+"\"></p>"); //<img src="smiley.gif" alt="Smiley face" height="42" width="42">
    }
    
    public void close() {
        for(String s:HTMLReporter.FOOTER){
            write(s);
        }
        out.close();
    }

    private void write(String s) {
        out.println(s);
        out.flush();
    }

    private String start(String tag) {
        return "<" + tag + ">";
    }

    private String end(String tag) {
        return "</" + tag + ">";
    }
}
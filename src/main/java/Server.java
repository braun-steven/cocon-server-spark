import database.DatabaseHelper;
import objects.Course;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JSONParser;

import java.util.List;
import java.util.UUID;

import static spark.Spark.*;

public class Server {
    /**
     * Logger instance
     */
    private final static Logger logger = LoggerFactory.getLogger(Server.class);
    
    /**
     * Server port
     */
    private static int PORT = 9000;
    
    /**
     * Main
     *
     * @param args cmd args
     */
    public static void main(String[] args) {
    
        if (args.length == 1) {
            PORT = Integer.parseInt(args[0]);
        }
        
        logger.info("Started server!!!!");
        DatabaseHelper.init();
        port(PORT);
    
        /*
         * Post for courses
         */
        post("/updateCourse", "application/json", (req, res) -> {
    
            logger.info("Entered /updateCourse");
    
            // Get params
            JSONObject body = new JSONObject(req.body());
            final JSONObject jsonCourse = body.getJSONObject("course");
            final String userId = body.getString("userId");
    
            logger.info("JSONCOURSE: " + jsonCourse);
            logger.info("USERID: " + userId);
    
            // Exec update
            final Course course = JSONParser.jsonToCourse(jsonCourse.toString());
            DatabaseHelper.insertCourse(course, userId);
    
            return "";
        });

        
        /*
         * Gets all courses
         */
        get("/getAllCourses", (req, res) -> {
    
            logger.info("Entered /all");
    
            final String userId = req.queryParams("userId");
            logger.info("UserID: " + userId);
            final List<Course> allCourses = DatabaseHelper.getAllCourses(userId);
            logger.info("Found " + allCourses.size() + " courses");
            return JSONParser.courseArrayToJSONArray(allCourses);
        });
    
        /*
         * Deletes a specific course
         */
        delete("/deleteCourse", (req, res) -> {
        
            logger.info("Entered /deleteCourse");
        
            // Get params
            JSONObject body = new JSONObject(req.body());
            final UUID courseId = UUID.fromString(body.getString("courseId"));
            final UUID userId = UUID.fromString(body.getString("userId"));
            DatabaseHelper.deleteCourse(courseId, userId);
            return "";
        });
    
        /*
         * Deletes a specific assignment
         */
        delete("/deleteAssignment", (req, res) -> {
        
            logger.info("Entered /deleteAssignment");
        
            // Get params
            JSONObject body = new JSONObject(req.body());
            final UUID assignmentId = UUID.fromString(body.getString
                    ("assignmentId"));
            final UUID userId = UUID.fromString(body.getString("userId"));
            DatabaseHelper.deleteAssignment(assignmentId);
            return "";
        });
        
    }
}
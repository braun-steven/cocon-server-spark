import database.DatabaseHelper;
import objects.Course;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import utils.JSONParser;

import java.util.List;

import static spark.Spark.*;

public class Server {
    private final static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        logger.info("Started server!!!!");
        DatabaseHelper.init();
        port(9000);
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

//        post("/updateAssignment", "application/json", (req, res) -> {
//
//            // Get params
//            String jsonAssignment = req.attribute("assignment");
//
//            // Exec update
//            final Assignment assignment = JSONParser.jsonToAssignment(jsonAssignment);
//            DatabaseHelper.insertAssignment(assignment);
//
//            return "";
//        });

        get("/getAllCourses", (req, res) -> {

            logger.info("Entered /all");

            final String userId = req.queryParams("userId");
            logger.info("UserID: " + userId);
            final List<Course> allCourses = DatabaseHelper.getAllCourses(userId);
            logger.info("Found " + allCourses.size() + " courses");
            return JSONParser.courseArrayToJSONArray(allCourses);
        });

    }
}
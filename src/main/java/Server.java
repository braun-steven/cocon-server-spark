import database.DatabaseHelper;
import objects.Assignment;
import objects.Course;
import spark.Request;
import spark.Response;
import spark.Route;
import utils.JSONParser;

import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

public class Server {
    public static void main(String[] args) {
        DatabaseHelper.init();

        post("/updateCourse", (req, res) -> {

            // Get params
            final String jsonCourse = req.attribute("course");
            final String userId = req.attribute("userId");

            // Exec update
            final Course course = JSONParser.jsonToCourse(jsonCourse);
            DatabaseHelper.insertCourse(course, userId);

            return "";
        });

        post("/updateAssignment", (req, res) -> {

            // Get params
            String jsonAssignment = req.attribute("assignment");

            // Exec update
            final Assignment assignment = JSONParser.jsonToAssignment(jsonAssignment);
            DatabaseHelper.insertAssignment(assignment);

            return "";
        });

        get("/all", (req, res) -> {
            final String userId = req.attribute("userId");
            final List<Course> allCourses = DatabaseHelper.getAllCourses(userId);
            return JSONParser.courseArrayToJSONArray(allCourses);
        });

    }
}
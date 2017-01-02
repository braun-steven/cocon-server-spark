package database;

import objects.Assignment;
import objects.Course;
import objects.DynamicPointsCourse;
import objects.FixedPointsCourse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JSONParser;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static database.DatabaseVocab.*;

/**
 * Created by tak on 1/2/17.
 */
public class DatabaseHelper {

    public static final String DATABASE_NAME = "cocon.db";
    public static final int DB_VERSION = 1;
    static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

    /**
     * Initialize the database
     */
    public static void init() {
        // Check if the database already exists
        if (new File(DATABASE_NAME).exists()) {
            return;
        }

        // Initialize tables and indices
        executeUpdate(CREATE_COURSE_TABLE);
        executeUpdate(CREATE_ASSIGNMENT_TABLE);
        executeUpdate(CREATE_USER_TABLE);
        executeUpdate(CREATE_UNIQUE_INDEX_COURSE);
        executeUpdate(CREATE_UNIQUE_INDEX_ASSIGNMENT);
    }

    /**
     * Executes a sql statement
     *
     * @param query statement
     */
    public static void executeUpdate(String query) {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            logger.info("Query: " + query);
            statement.executeUpdate(query);
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }
    }

    /**
     * Insert/update course
     *
     * @param course course
     * @param userId userId
     */
    public static void insertCourse(Course course, String userId) {
        executeUpdate("REPLACE INTO " + TABLE_COURSES + " " + courseToValues(course));
        executeUpdate("INSERT OR IGNORE INTO " + TABLE_USER + " values('" + userId + "', '" + course.getId() + "')");

        course.getAssignments().forEach(DatabaseHelper::insertAssignment);
    }

    /**
     * Insert/update assignment
     *
     * @param assignment assignment
     */
    public static void insertAssignment(Assignment assignment) {
        executeUpdate("REPLACE INTO " + TABLE_COURSES + " " + assignmentToValues(assignment));
    }

    /**
     * Generates a string of "values(..,..,..)" of a course
     *
     * @param course course
     * @return String
     */
    public static String courseToValues(Course course) {
        StringBuilder sb = new StringBuilder();
        sb.append("values(");
        sb.append("'" + course.getId() + "',");
        sb.append("'" + course.getCourseName() + "',");
        sb.append(course.getNumberOfAssignments() + ",");

        /*
        If course is a "FixedPointsCourse" -> insert course.getMaxPoints
        Else insert "Null-Value"
         */
        Double maxPoints = null;
        if (course.hasFixedPoints()) {
            maxPoints = ((FixedPointsCourse) course).getMaxPoints();
        }
        sb.append(maxPoints + ",");
        sb.append((course.hasFixedPoints() ? 1 : 0) + ",");
        sb.append(course.getNecPercentToPass() + ",");
        sb.append(course.getDate());
        sb.append(")");

        return sb.toString();
    }

    /**
     * Generates a string of "values(..,..,..)" of an assignment
     *
     * @param assignment assignment
     * @return String
     */
    public static String assignmentToValues(Assignment assignment) {
        StringBuilder sb = new StringBuilder();

        sb.append("values(");
        sb.append("'" + assignment.getId() + "',");
        sb.append(assignment.getIndex() + ",");
        sb.append(assignment.getMaxPoints() + ",");
        sb.append(assignment.isExtraAssignment()? 1: 0 + ",");
        sb.append("'" + assignment.getCourse_id() + "',");
        sb.append(assignment.getAchievedPoints() + ",");
        sb.append(assignment.getDate() + ",");
        sb.append(")");
        return sb.toString();
    }

    public static Course getCourse(UUID courseId) {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:cocon.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            final String sql = "SELECT * FROM " + TABLE_COURSES
                    + " WHERE " + KEY_ID + "='" + courseId.toString() + "';";
            logger.info("GetCourse Query: " + sql);
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                // read the result set
                Course course = null;
                final boolean fixedPoints = rs.getBoolean(KEY_HAS_FIXED_POINTS);
                String courseName = rs.getString(KEY_COURSENAME);
                int numberOfAssignments = Integer.parseInt(rs.getString(KEY_NUMBER_OF_ASSIGNMENTS));

                //Get max points
                double maxPoints;
                String maxPointsString = rs.getString(KEY_REACHABLE_POINTS_PER_ASSIGNMENT);
                if (maxPointsString == null) {
                    maxPoints = 0;
                } else {
                    maxPoints = Double.parseDouble(maxPointsString);
                }

                //get necPercentToPass
                double necPercentToPass = Double.parseDouble(rs.getString(KEY_NEC_PERCENT_TO_PASS));

                //get date
                long date = rs.getLong(KEY_DATE);

                //Get has fixed points (1 == true, 0 == false in sqlite)
                boolean hasFixedPoints = rs.getInt(KEY_HAS_FIXED_POINTS) == 1;

                //Create specific course instance depending on "hasFixedPoints"
                if (hasFixedPoints) {
                    course = new FixedPointsCourse(courseName, maxPoints);
                } else {
                    course = new DynamicPointsCourse(courseName);
                }
                course.setId(courseId);
                course.setNumberOfAssignments(numberOfAssignments);
                course.setNecPercentToPass(necPercentToPass);
                course.setDate(date);
                //get assignments
                course.setAssignments(getAssignmentsOfCourse(courseId));
                connection.close();
                return course;
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }
        return null;
    }

    public static ArrayList<Assignment> getAssignmentsOfCourse(UUID courseId) {

        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:cocon.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            ResultSet rs = statement.executeQuery("SELECT * FROM " + TABLE_ASSIGNMENTS
                    + " WHERE " + KEY_COURSE_ID + "='" + courseId.toString() + "'");
            ArrayList<Assignment> assignments = new ArrayList<>();
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString(KEY_ID));
                int index = Integer.parseInt(rs.getString(KEY_ASSIGNMENT_INDEX));
                double maxPoints = Double.parseDouble(rs.getString(KEY_MAX_POINTS));
                boolean isExtraAssignment = rs.getInt(KEY_IS_EXTRA_ASSIGNMENT) > 0;
                double achievedPoints = Double.parseDouble(rs.getString(KEY_ACHIEVED_POINTS));
                long date = rs.getLong(KEY_DATE);


                Assignment assignment = new Assignment(id, index, maxPoints, achievedPoints, courseId);
                assignment.isExtraAssignment(isExtraAssignment);
                assignment.setDate(date);
                assignments.add(assignment);
            }

            connection.close();
            return assignments;
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }
        return null;


    }

    /**
     * Get all courses by a specific user
     *
     * @param userId userId
     * @return list of courses
     */
    public static List<Course> getAllCourses(String userId) {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:cocon.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            List<Course> courses = new ArrayList<>();
            //language=SQLite
            final String query = "SELECT " + KEY_ID + ", " + KEY_COURSE_ID + " FROM " + TABLE_USER
                    + " WHERE " + KEY_ID + " = '" + userId+"';";
            logger.info(query);
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                // read the result set
                final UUID courseUUID = UUID.fromString(rs.getString(KEY_COURSE_ID));
                courses.add(getCourse(courseUUID));
            }
            connection.close();
            return courses;
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }
        return null;
    }
}

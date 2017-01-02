package database;

import objects.Course;
import objects.FixedPointsCourse;

import java.sql.*;

import static database.DatabaseVocab.KEY_REACHABLE_POINTS_PER_ASSIGNMENT;

/**
 * Created by tak on 1/2/17.
 */
public class DatabaseHelper {

    public static final String DATABASE_NAME = "cocon.db";

    public static void insertCourse(Course course){
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("create table person (id integer, name string)");
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
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
    }

    public String courseToValueString(Course course){
        StringBuilder sb = new StringBuilder();
        sb.append("values(");
        sb.append(course.getId() + ",");
        sb.append(course.getCourseName() + ",");
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
        sb.append(course.getIndex() + ",");
        sb.append(course.getNumberOfAssignments() + ",");
        sb.append(course.getNumberOfAssignments() + ",");
        sb.append(course.getNumberOfAssignments() + ",");
        sb.append(")");
    }
}

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Arrays;

import static spark.Spark.*;

public class HelloWorld {
    public static void main(String[] args) {
        get("/hello", (req, res) -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Querymap: \n");
            req.queryMap().toMap().forEach((s1, strings) -> sb.append(s1 + ": " + Arrays.toString(strings) + "\n"));
            return sb.toString();
        });

    }
}
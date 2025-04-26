import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GetProfileImage")
public class ImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username"); // Get username from request

        if (username == null || username.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=#;encrypt=false");
             PreparedStatement ps = con.prepareStatement("SELECT Profile FROM UserValidation WHERE Username = ?")) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                byte[] imageData = rs.getBytes("Profile"); // Get image as bytes

                if (imageData != null) {
                    response.setContentType("image/jpeg"); // Set response as an image
                    OutputStream os = response.getOutputStream();
                    os.write(imageData);
                    os.close();
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND); // No image found
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND); // No user found
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}


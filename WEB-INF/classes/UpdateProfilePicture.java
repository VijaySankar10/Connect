import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;

@MultipartConfig // Enables file upload handling
public class UpdateProfilePicture extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String username = (session != null) ? (String) session.getAttribute("username") : null;

        if (username == null) {
            res.sendRedirect("login.jsp"); // Redirect to login if session is invalid
            return;
        }

        Part filePart = req.getPart("profilePic"); // Retrieves the uploaded file
        InputStream fileContent = filePart.getInputStream(); // Converts file into InputStream

        Connection cn = null;
        PreparedStatement ps = null;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=#;encrypt=true;trustServerCertificate=true";

            cn = DriverManager.getConnection(url);

            String query = "UPDATE UserValidation SET Profile = ? WHERE Username = ?";
            ps = cn.prepareStatement(query);
            ps.setBlob(1, fileContent); // Set the file as a BLOB
            ps.setString(2, username);

            int rowsUpdated = ps.executeUpdate();

            res.setContentType("text/html");
            PrintWriter out = res.getWriter();

            if (rowsUpdated > 0) {
                res.sendRedirect("/Connect/Profile");
            } else {
                out.println("<p>Failed to update profile picture. Please try again.</p>");
            }
        } catch (Exception e) {
            res.setContentType("text/html");
            PrintWriter out = res.getWriter();
            out.println("<h3>Error:</h3>");
            e.printStackTrace(new PrintWriter(out)); // show error in browser
        }
        finally {
            try {
                if (ps != null) ps.close();
                if (cn != null) cn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

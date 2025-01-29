import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.Base64;

public class Profile extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String username = (session != null) ? (String) session.getAttribute("username") : null;

        if (username == null) {
            res.sendRedirect("login.jsp"); // Redirect to login if session is invalid
            return;
        }

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        PrintWriter out = res.getWriter();
        res.setContentType("text/html");

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=vj10;encrypt=false";

            cn = DriverManager.getConnection(url);

            String query = "SELECT Username, Profile, Designation, MailId FROM UserValidation WHERE Username = ?";
            ps = cn.prepareStatement(query);
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (rs.next()) {
                byte[] imgData = rs.getBytes("Profile");
                String mailId = rs.getString("MailId");
                String designation = rs.getString("Designation");

                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>User Profile</title>");
                out.println("<link rel='stylesheet' href='http://localhost:8080/Connect/css/styles1.css'>");
                out.println("</head>");
                out.println("<body>");
                out.println("<header>");
                out.println("<img src='http://localhost:8080/Connect/images/Connect.png' id='Header-Logo'>");
                out.println("</header>");
                out.println("<br><br>");
                out.println("<center>");

                out.println("<div class='container'>");
                out.println("<center>");

                if (imgData != null) {
                    String base64Image = Base64.getEncoder().encodeToString(imgData);
                    out.println("<img src='data:image/jpeg;base64," + base64Image + "' alt='Profile Picture' class='content-image'/>");
                } else {
                    out.println("<p>No profile picture available.</p>");
                }
                out.println("<form action='http://localhost:8080/Connect/UpdateProfilePicture' method='POST' enctype='multipart/form-data'>");
                out.println("<label for='profilePic'>Update Profile Picture:</label>");
                out.println("<input type='file' name='profilePic' accept='image/*' required>");
                out.println("<br><br>");
                out.println("<button type='submit'>Update Picture</button>");
                out.println("</form>");

                out.println("<h1>Username: " + username + "</h1>");
                out.println("<h1>Designation: " + designation + "</h1>");
                out.println("<h1>Mail ID: " + mailId + "</h1>");

                out.println("</center>");
                out.println("</div>");
                out.println("</center>");
                out.println("</body>");
                out.println("</html>");
            } else {
                out.println("<p>User not found.</p>");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            out.println("<p>Error: " + e.getMessage() + "</p>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (cn != null) cn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

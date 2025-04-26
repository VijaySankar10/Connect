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
            res.sendRedirect("/Connect/logIn.html");
            return;
        }

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        PrintWriter out = res.getWriter();
        res.setContentType("text/html");

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=#;encrypt=true;trustServerCertificate=true";
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
                out.println("<link rel='icon' type='image/png' href='/Connect/images/ConnectProfile.png'>");
                out.println("<style>");
                out.println("body { margin: 0; padding: 0; font-family: 'Segoe UI', sans-serif; background-image: url('/Connect/images/background.jpg'); background-size: cover; background-repeat: repeat-x; background-attachment: fixed; color: #333; }");
                out.println("header { height: 100px; background-color: #000000; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }");
                out.println("#Header-Logo { height: 70px; }");
                out.println(".container { width: 600px; background-color: rgba(255, 255, 255, 0.95); border-radius: 20px; padding: 40px; box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); margin: 30px auto; text-align: center; }");
                out.println(".content-image { width: 160px; height: 160px; object-fit: cover; border-radius: 50%; border: 4px solid #eee; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); margin-bottom: 20px; }");
                out.println("h1 { font-size: 22px; margin: 10px 0; color: #444; }");
                out.println("form { margin-top: 20px; }");
                out.println("label { font-size: 16px; font-weight: bold; display: block; margin-bottom: 10px; color: #333; }");
                out.println("input[type='file'] { font-size: 14px; padding: 5px; border-radius: 8px; border: 1px solid #ccc; background-color: #f9f9f9; }");
                out.println("button[type='submit'] { margin-top: 15px; padding: 10px 25px; background-color:rgb(0, 0, 0); border: none; color: white; font-size: 16px; border-radius: 10px; cursor: pointer; transition: 0.3s ease; }");
                out.println("button[type='submit']:hover { background-color:rgb(2, 22, 36); transform: scale(1.05); }");
                out.println("@media screen and (max-width: 650px) { .container { width: 90%; padding: 25px; } .content-image { width: 130px; height: 130px; } h1 { font-size: 18px; } }");
                out.println("</style>");
                out.println("</head>");
                out.println("<body>");
                out.println("<header><img src='/Connect/images/Connect.png' id='Header-Logo'></header>");
                out.println("<div class='container'>");

                if (imgData != null) {
                    String base64Image = Base64.getEncoder().encodeToString(imgData);
                    out.println("<img src='data:image/jpeg;base64," + base64Image + "' alt='Profile Picture' class='content-image'/>");
                } else {
                    out.println("<p>No profile picture available.</p>");
                }

                out.println("<form action='/Connect/UpdateProfilePicture' method='POST' enctype='multipart/form-data'>");
                out.println("<label for='profilePic'>Update Profile Picture:</label>");
                out.println("<input type='file' name='profilePic' accept='image/*' required><br><br>");
                out.println("<button type='submit'>Update Picture</button>");
                out.println("</form>");

                out.println("<h1>Username: " + username + "</h1>");
                out.println("<h1>Designation: " + designation + "</h1>");
                out.println("<h1>Mail ID: " + mailId + "</h1>");

                out.println("</div>");
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

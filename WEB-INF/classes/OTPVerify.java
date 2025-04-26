import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class OTPVerify extends HttpServlet
{
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
    {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String enteredOTP = req.getParameter("otp");
        HttpSession session = req.getSession(false);

        if (session == null) {
            res.sendRedirect(req.getContextPath() + "/VerifyOTP.html?error=sessionExpired");
            return;
        }

        String realOTP = (String) session.getAttribute("otp");

        if (realOTP != null && realOTP.equals(enteredOTP))
        {
            String username = (String) session.getAttribute("username");
            String password = (String) session.getAttribute("password");
            String designation = (String) session.getAttribute("designation");
            String email = (String) session.getAttribute("mail");

            String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=#;encrypt=false";
            Connection cn = null;
            PreparedStatement ps = null;

            try 
            {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                cn = DriverManager.getConnection(url);

                String query1 = "INSERT INTO UserValidation(Username, Password, Designation, MailId) VALUES (?, ?, ?, ?)";
                ps = cn.prepareStatement(query1);
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, designation);
                ps.setString(4, email);
                ps.executeUpdate();
                ps.close(); // Always close previous PreparedStatement before reusing

                String query2 = "INSERT INTO Messages(sender, receiver, message, timestamp) VALUES (?, ?, ?, ?)";
                ps = cn.prepareStatement(query2);
                ps.setString(1, "Admin");
                ps.setString(2, username);
                ps.setString(3, "For Queries, DM to this Profile");
                ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();

                res.sendRedirect(req.getContextPath() + "/index.jsp");
            }
            catch (Exception e)
            {
                out.println("<html><body>");
                out.println("<h2>Error: " + e.getMessage() + "</h2>");
                out.println("</body></html>");
            }
            finally 
            {
                try {
                    if (ps != null) ps.close();
                    if (cn != null) cn.close();
                } catch (SQLException e) {
                    out.println("<h2>Error closing resources: " + e.getMessage() + "</h2>");
                }
            }
        }
        else
        {
            res.sendRedirect(req.getContextPath() + "/VerifyOTP.html?error=invalid");
        }
    }
}

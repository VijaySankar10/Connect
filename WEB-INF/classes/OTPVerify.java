import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class OTPVerify extends HttpServlet
{
    protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException 
    {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String EnteredOTP = req.getParameter("otp");
        HttpSession session=req.getSession(false);
        String RealOTP=(String)session.getAttribute("otp");
        if(RealOTP.equals(EnteredOTP))
        {
            String username = (String)session.getAttribute("username");
            String password = (String)session.getAttribute("password");
            String designation = (String)session.getAttribute("designation");
            String email=(String)session.getAttribute("mail");

            String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=vj10;encrypt=false";
            Connection cn = null;

            try 
            {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                cn = DriverManager.getConnection(url);
                PreparedStatement ps = null;

    
                String query = "INSERT INTO UserValidation(Username,Password,Designation,MailId) VALUES(?,?,?,?)";
                ps = cn.prepareStatement(query);
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, designation);
                ps.setString(4, email);

                ps.executeUpdate();
                String contextPath=req.getContextPath();

                res.sendRedirect(contextPath+"/index.jsp");

            }
            catch(Exception e)
            {
                out.println("<html><body>");
                out.println("<h2>Error closing resources: " + e.getMessage() + "</h2>");
                out.println("</body></html>");
            }
            finally {
                // Close resources
                try {
                    if (cn != null) cn.close();
                } catch (SQLException e) {
                    out.println("<h2>Error closing resources: " + e.getMessage() + "</h2>");
                }
            }
        }
        else{
            String contextPath=req.getContextPath();
            res.sendRedirect(contextPath+"/VerifyOTP?error=invalid");
        }
    }
}
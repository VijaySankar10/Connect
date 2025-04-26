import java.io.*;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;


public class AddAccount extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String username = req.getParameter("UName");
        String password = req.getParameter("PWord");
        String email = req.getParameter("Email");
        String designation = req.getParameter("designation");

        String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=#;encrypt=false";

        boolean success = false;
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            cn = DriverManager.getConnection(url);

            String query = "SELECT Username FROM UserValidation WHERE Username = ?";
            ps = cn.prepareStatement(query);
            ps.setString(1, username);

            rs = ps.executeQuery();

            if (rs.next()) {
                String contextPath=req.getContextPath();
                res.sendRedirect(contextPath+"/SignUp.html?error=invalid");
            } else {
                String otp = generateOtp();
                if (sendEmail(email, otp, username)) {
                    HttpSession session = req.getSession();
                    session.setAttribute("otp", otp);
                    session.setAttribute("username", username);
                    session.setAttribute("password", password);
                    session.setAttribute("designation", designation);
                    session.setAttribute("mail",email);
                    String contextPath=req.getContextPath();
                    res.sendRedirect(contextPath+"/VerifyOTP.html");
                } else {
                    out.println("<html><body>");
                    out.println("<h2>Error sending OTP. Please try again.</h2>");
                    out.println("</body></html>");
                }
            }
        } catch (SQLException e) {
            out.println("<html><body>");
            out.println("<h2>Database connection error: " + e.getMessage() + "</h2>");
            out.println("</body></html>");
            return;
        } catch (ClassNotFoundException e) {
            out.println("<html><body>");
            out.println("<h2>JDBC Driver not found: " + e.getMessage() + "</h2>");
            out.println("</body></html>");
            return;
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (cn != null) cn.close();
            } catch (SQLException e) {
                out.println("<html><body>");
                out.println("<h2>Error closing resources: " + e.getMessage() + "</h2>");
                out.println("</body></html>");
            }
        }
    }

    private String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    private boolean sendEmail(String recipientEmail, String otp, String username) {
        final String senderEmail = "#";
        final String senderPassword = "#";
    
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
    
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    
        try {
            Message message = new MimeMessage(session);
    
            // 👇 Sender name will appear as "Connect"
            message.setFrom(new InternetAddress(senderEmail, "Connect"));
    
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your OTP Code for Connect");
    
            // 📝 Use HTML for formatting
            String htmlContent = "<html>"
                    + "<body style='font-family: Arial, sans-serif;'>"
                    + "<h3>Hello " + username + ",</h3>"
                    + "<p>Your one-time password (OTP) for logging into <b>Connect Messaging App</b> is:</p>"
                    + "<p style='font-size: 18px;'><b>" + otp + "</b></p>"
                    + "<p>This OTP is valid for 10 minutes. Please do not share it with anyone.</p>"
                    + "<br>"
                    + "<p>Thank you,<br><b>Connect Team</b></p>"
                    + "</body></html>";
    
            // ⬇️ Set the message content with MIME type as HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");
    
            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }    
}
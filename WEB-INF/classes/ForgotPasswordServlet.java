import java.io.*;
import java.sql.*;
import java.util.Properties;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class ForgotPasswordServlet extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String email = req.getParameter("email");
        String name = req.getParameter("name");
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=#;encrypt=false";

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String password = null;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            cn = DriverManager.getConnection(url);

            // Check if email exists and fetch password
            String query = "SELECT Password FROM UserValidation WHERE Username = ? AND MailId = ?";
            ps = cn.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, email);
            rs = ps.executeQuery();

            if (rs.next()) {
                password = rs.getString("Password"); // Get password from DB

                if (sendEmail(email, password)) {
                    out.println("<html><body><h2>Password sent to your email.</h2></body></html>");
                } else {
                    out.println("<html><body><h2>Error sending email. Please try again.</h2></body></html>");
                }
            } else {
                res.sendRedirect("/Connect/forgotPassword.html?error=invalid");
            }
        } catch (SQLException | ClassNotFoundException e) {
            out.println("<html><body><h2>Error: " + e.getMessage() + "</h2></body></html>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (cn != null) cn.close();
            } catch (SQLException e) {
                out.println("<html><body><h2>Error closing resources: " + e.getMessage() + "</h2></body></html>");
            }
        }
    }

    private boolean sendEmail(String recipientEmail, String password) {
        final String senderEmail = "#";
        final String senderPassword = "#";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);

            // ✅ Display name as "Connect"
            message.setFrom(new InternetAddress(senderEmail, "Connect"));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Password Recovery - Connect");

            // ✅ HTML content with bold styling
            String htmlContent = "<html>"
                    + "<body style='font-family: Arial, sans-serif;'>"
                    + "<h3>Password Recovery - <b>Connect Messaging App</b></h3>"
                    + "<p>Hello,</p>"
                    + "<p>Your password is: <span style='font-size: 16px;'><b>" + password + "</b></span></p>"
                    + "<p>Please keep it safe and do not share it with anyone.</p>"
                    + "<br><p>Regards,<br><b>Connect Team</b></p>"
                    + "</body></html>";

            // Set HTML content
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

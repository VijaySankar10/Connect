<%@ page import="java.sql.*" %>
<%
    String username = request.getParameter("username");

    if (username != null && !username.isEmpty()) {
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;encrypt=false";
            String dbUser = "Vijay";
            String dbPassword = "";
            con = DriverManager.getConnection(url, dbUser, dbPassword);

            // Delete messages first
            String messageQuery = "DELETE FROM Messages WHERE sender = ? OR receiver = ?";
            pstmt = con.prepareStatement(messageQuery);
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            pstmt.close();

            // Then delete user
            String userQuery = "DELETE FROM UserValidation WHERE Username = ?";
            pstmt = con.prepareStatement(userQuery);
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                out.print("User '" + username + "' and related messages deleted successfully!");
            } else {
                out.print("Error: User not found.");
            }

        } catch (Exception e) {
            out.print("Error: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                out.print("Error closing connection: " + e.getMessage());
            }
        }
    } else {
        out.print("Invalid request.");
    }
%>

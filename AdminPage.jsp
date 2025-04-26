<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connect | Home</title>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="icon" type="image/png" href="/Connect/images/ConnectProfile.png">
    <link rel="stylesheet" href="/Connect/css/styles1.css">
    <script>
        function deleteAccount(username) {
            if (confirm("Are you sure you want to delete this account?")) {
                // AJAX request to deleteUser.jsp
                var xhr = new XMLHttpRequest();
                xhr.open("POST", "deleteUser.jsp", true);
                xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

                xhr.onreadystatechange = function () {
                    if (xhr.readyState == 4 && xhr.status == 200) {
                        alert(xhr.responseText); // Show success or error message
                        location.reload(); // Refresh the page to reflect changes
                    }
                };
                xhr.send("username=" + encodeURIComponent(username));
            }
        }
    </script>
    <style>
        header {
        background-color: black;
        align-items: center;
        justify-content: center;
        color: white;
        text-align: center;
        padding: 20px;
        font-family: 'Arial', sans-serif;
        display: flex;
        flex-direction: row;
    }
    .bn{
        background-color: gray;
        color: black;
        height: 10%;
        width: 10%;
        border-radius: 50px;
        font-size: 15px;
        
        transition: background-color 0.4s ease;
    }

    .bn a{
        color: inherit;
        transition: color 0.4s ease;
    }

    .bn a:hover{
        color: inherit;
    }

    .bn:hover{
        background-color:#181818;
        color:white
    }

    </style>
</head>
<body>
    <%@ page import="java.sql.*" %>
    <header>
        <img src="/Connect/images/Connect.png" id="Header-Logo">
        <nav id="navbar">
            <li class="navli"><a href="/Connect/Home.jsp" style="text-decoration: none; color: aliceblue;">Home</a></li>
            <button class="bn" style="text-align: center; justify-content: center;"><a href="/Connect/Logout" style="text-decoration: none; color: aliceblue;">LogOut</a></button>
        </nav>
    </header>
<br>
<br>
<center>
    <%
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;encrypt=false";
            String dbUser = "Vijay";
            String dbPassword = "";
            con = DriverManager.getConnection(url, dbUser, dbPassword);

            String query = "SELECT Username, Designation, MailId FROM UserValidation";
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
    %>

    <div class="Container">
        <table border="1">
            <tr>
                <th>Username</th>
                <th>Designation</th>
                <th>Email</th>
                <th>Action</th>
            </tr>
            <%
                while (rs.next()) {
                    String user = rs.getString("Username");
                    String desig = rs.getString("Designation");
                    String mailId = rs.getString("MailId");
            %>
            <tr>
                <td><%= user %></td>
                <td><%= desig %></td>
                <td><%= mailId %></td>
                <td><button class="Button" onclick="deleteAccount('<%= user %>')">Delete</button></td>
            </tr>
            <%
                }
            %>
        </table>
    </div>

    <%
        } catch (Exception e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                out.println("<p>Database closing error: " + e.getMessage() + "</p>");
            }
        }
    %>
</center>
</body>
</html>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connect | All Profiles</title>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="icon" type="image/png" href="/Connect/images/ConnectProfile.png">
    <link rel="stylesheet" href="/Connect/css/styles.css">
</head>
<body>
    <%@ page import="java.sql.*" %>
<div>
    <header>
        <div class="logo">
            <img src="/Connect/images/Connect.png" id="Header-Logo">
        </div>
        <nav id="navbar">
            <li class="navli"><a href="/Connect/Profile">My Profile</a></li>
            <li class="navli"><a href="/Connect/Home.jsp">Home</a></li>
            <button class="bn"><a href="/Connect/Logout">Log Out</a></button>
        </nav>
    </header>

    <div class="leftrightContainer">
        <div class="leftPannel">
            <div class="search-bar-container">
                <input type="text" class="search-bar-input" placeholder="Search...">
                <button class="material-icons" style="border-radius: 50%; background-color: inherit; ">search</button> <!-- Search Icon -->
                <button class="material-icons" style="border-radius: 50%; background-color: inherit; ">refresh</button> <!-- Refresh Icon -->
            </div>
            <%
                String username = (String) session.getAttribute("username");
                try {
                    // Load SQL Server JDBC Driver
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

                    // Establish connection to the Connect database
                    String url = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=#;encrypt=false";
                    Connection con = DriverManager.getConnection(url);

                    // SQL Query to fetch usernames from UserValidation table
                    String query = "SELECT Username FROM UserValidation WHERE Username != ?";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

            %>

            <!-- List of users -->
            
            <ul id="userItems" style="width: 100%; margin: 0; padding: 0;">
                <%
                    while (rs.next()) {
                        String user = rs.getString("username");
                %>
                    <li class="user-list" onclick="loadChat('<%= user %>')" style="display: flex; align-items: center; cursor: pointer; padding: 8px;">
                        <img src="/Connect/GetProfileImage?username=<%= user %>" class="user-profile-pic" style="width: 40px; height: 40px; border-radius: 50%; margin-right: 10px;">
                        <span><%= user %></span>
                    </li>
                <%
                    }
                    rs.close();
                    stmt.close();
                    con.close();
                %>
            </ul>
            

            <%
                } catch (Exception e) {
                    e.printStackTrace();
                }
            %>
        </div>

        <div class="rightPannel">
            <div id="msgProfile">
                <img id="chatProfilePic" src="/Connect/images/default-profile.jpg" class="chat-header-pic">
                <label class="Name" id="chatWith">Select a user</label>
            </div>
            
            <div id="chatMessages"></div>
                <div class="chat-input">
                    <input type="text" id="messageInput" placeholder="Type a message..." />
                    <button class="bn" style="width:125px; height: 25px;" onclick="sendMessage()">Send</button>
                </div>
        </div>
    </div>

    <!-- Inject session data -->
    
    <input type="hidden" id="username" value="<%= username != null ? username : "Guest" %>">

    <script>
        const username = document.getElementById("username").value;
        const ws = new WebSocket(
            (window.location.protocol === "https:" ? "wss://" : "ws://") +
            window.location.host +
            "/Connect/chat/" + username
            );
        let currentUser = '';

        // Function to load chat with selected user
        function loadChat(userName) 
        {
            document.getElementById('chatWith').textContent = userName;

            // Update chat header profile pic
            const chatProfilePic = document.getElementById("chatProfilePic");
            chatProfilePic.src = "/Connect/GetProfileImage?username=" + encodeURIComponent(userName);

            document.getElementById('chatMessages').innerHTML = '';
            currentUser = userName;

            ws.send(JSON.stringify({
                type: 'loadChat',
                user: username,
                receiver: userName
            }));
        }

        // Function to display messages properly
        function displayMessage(sender, message, isOwnMessage) {
            const messagesDiv = document.getElementById("chatMessages");
            const messageDiv = document.createElement("div");
            messageDiv.className = isOwnMessage ? "my-message" : "their-message";

            // Profile Picture
            const img = document.createElement("img");
            img.src = "/Connect/GetProfileImage?username=" + encodeURIComponent(sender);
            img.className = "chat-user-pic";

            // Message Text
            const textSpan = document.createElement("span");
            textSpan.textContent = sender + ": " + message;

            if (isOwnMessage) {
                messageDiv.appendChild(textSpan);
                messageDiv.appendChild(img);
            } else {
                messageDiv.appendChild(img);
                messageDiv.appendChild(textSpan);
            }

            messagesDiv.appendChild(messageDiv);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        }

        // Handle incoming WebSocket messages
        ws.onmessage = function(event) {
            const data = JSON.parse(event.data);

            // Handle chat history
            if (Array.isArray(data)) {
                data.forEach(msg => {
                    const isOwnMessage = msg.sender === username;
                    displayMessage(msg.sender, msg.message, isOwnMessage);
                });
            } else {
                // Handle new incoming messages
                if (data.sender === currentUser || data.sender === username) {
                    const isOwnMessage = data.sender === username;
                    displayMessage(data.sender, data.message, isOwnMessage);
                }
            }
        };

        // Send a message to the selected user
        function sendMessage() {
            const input = document.getElementById("messageInput");
            const messageText = input.value.trim();
            if (!messageText || !currentUser) return;

            const jsonMessage = {
                type: "sendMessage",
                sender: username,
                receiver: currentUser,
                message: messageText
            };

            ws.send(JSON.stringify(jsonMessage));
            input.value = ""; // Clear input after sending
        }

        // Handle WebSocket errors
        ws.onerror = function(event) {
            console.error("WebSocket error:", event);
        };

        // Handle WebSocket closure
        ws.onclose = function(event) {
            console.log("WebSocket connection closed:", event);
        };
    </script>

    <style>
        /* Simple styles for sent and received messages */
        #chatMessages {
            max-height: 400px;
            overflow-y: auto;
            padding: 10px;
            background-color: #f1f1f1;
        }

        .my-message {
            text-align: right;
            background-color: #d1ffd1;
            padding: 8px;
            margin: 5px;
            border-radius: 10px;
        }

        .their-message {
            text-align: left;
            background-color: #ffffff;
            padding: 8px;
            margin: 5px;
            border-radius: 10px;
        }
    </style>
</body>
</html>
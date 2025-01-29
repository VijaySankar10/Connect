<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connect | Home</title>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="http://localhost:8080/Connect/css/styles.css">
</head>
<body>
<div>
    <header>
        <div class="logo">
            <img src="http://localhost:8080/Connect/images/Connect.png" id="Header-Logo">
        </div>
        <nav id="navbar">
            <li class="navli"><a href="http://localhost:8080/Connect/Profile">My Profile</a></li>
            <li class="navli"><a href="http://localhost:8080/Connect/AllProfiles.jsp">All Profiles</a></li>
            <button class="bn"><a href="#">Log Out</a></button>
        </nav>
    </header>

    <div class="leftrightContainer">
        <div class="leftrightContainer">
            <div class="leftPannel">
                <div class="search-bar-container">
                    <input type="text" class="search-bar-input" placeholder="Search...">
                    <button class="material-icons" style="border-radius: 50%; background-color: inherit; ">search</button> <!-- Search Icon -->
                    <button class="material-icons" style="border-radius: 50%; background-color: inherit; ">refresh</button> <!-- Refresh Icon -->
                </div>

                <div class="leftka">
                <ul id="userItems" style="height: fit-content;">
                    <!-- User list will be dynamically populated here -->
                    
                </ul>
                </div>
            </div>

        <div class="rightPannel">
            <div id="msgProfile">
                <Label class="Name" id="chatWith">Select a user</Label>
            </div>
            <div id="chatMessages"></div>
            <div class="chat-input">
                <input type="text" id="messageInput" placeholder="Type a message..." />
                <button class="bn" style="width:125px; height: 25px;" onclick="sendMessage()">Send</button>
            </div>
        </div>
    </div>

    <%
        String username = (String) session.getAttribute("username");
    %>
    <input type="hidden" id="username" value="<%= username != null ? username : "Guest" %>">

    <script>
        const username = document.getElementById("username").value;
        const ws = new WebSocket("ws://" + window.location.host + "/Connect/chat/" + username);
        let currentUser = '';

        ws.onopen = function () {
            refreshUserList();
        };

        function refreshUserList() {
            ws.send(JSON.stringify({
                type: 'loadUserList',
                user: username
            }));
        }

        function loadChat(userName) {
            document.getElementById('chatWith').textContent = userName;
            document.getElementById('chatMessages').innerHTML = '';
            currentUser = userName;
            ws.send(JSON.stringify({
                type: 'loadChat',
                user: username,
                receiver: userName
            }));
        }

        function displayMessage(sender, message, isOwnMessage) {
            const messagesDiv = document.getElementById("chatMessages");
            const messageDiv = document.createElement("div");
            messageDiv.className = isOwnMessage ? "my-message" : "their-message";
            messageDiv.textContent = sender + ": " + message;
            messagesDiv.appendChild(messageDiv);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        }

        ws.onmessage = function (event) {
            const data = JSON.parse(event.data);

            if (data.type === 'userListUpdate' && Array.isArray(data.users)) {
                const userItems = document.getElementById("userItems");
                userItems.innerHTML = "";

                data.users.forEach(user => {
                    const li = document.createElement("li");
                    li.className = "user-list";
                    li.textContent = user;
                    li.onclick = function () { loadChat(user); };
                    userItems.appendChild(li);
                });
            } else if (Array.isArray(data)) {
                data.forEach(msg => {
                    const isOwnMessage = msg.sender === username;
                    displayMessage(msg.sender, msg.message, isOwnMessage);
                });
            } else {
                if (data.sender === currentUser || data.sender === username) {
                    const isOwnMessage = data.sender === username;
                    displayMessage(data.sender, data.message, isOwnMessage);
                }
            }
        };

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
            //displayMessage(username, messageText, true);
            input.value = "";
        }

        ws.onerror = function (event) {
            console.error("WebSocket error:", event);
        };

        ws.onclose = function (event) {
            console.log("WebSocket connection closed:", event);
        };
    </script>

    <style>
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
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connect | Home</title>
    <link rel="icon" type="image/png" href="/Connect/images/ConnectProfile.png">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="/Connect/css/styles.css">
</head>
<body>
<div>
    <header>
        <div class="logo">
            <img src="/Connect/images/Connect.png" id="Header-Logo">
        </div>
        <nav id="navbar">
            <li class="navli"><a href="/Connect/Profile">My Profile</a></li>
            <li class="navli"><a href="/Connect/AllProfiles.jsp">All Profiles</a></li>
            <button class="bn"><a href="/Connect/Logout">Log Out</a></button>
        </nav>
    </header>

        <div class="leftrightContainer">
            <div class="leftPannel">
                <div class="search-bar-container">
                    <input type="text" class="search-bar-input" placeholder="Search">
                    <button class="material-icons" style="border-radius: 50%; background-color: inherit; ">search</button> <!-- Search Icon -->
                </div>

                
                <ul id="userItems" style="width: 100%; margin: 0; padding: 0;">
                    <!-- User list will be dynamically populated here -->
                    
                </ul>
                
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

    <%
        String username = (String) session.getAttribute("username");
    %>
    <input type="hidden" id="username" value="<%= username != null ? username : "Guest" %>">

    <script>
        const username = document.getElementById("username").value;
        const ws = new WebSocket(
            (window.location.protocol === "https:" ? "wss://" : "ws://") +
            window.location.host +
            "/Connect/chat/" + username
            );
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


        ws.onmessage = function (event) {
    const data = JSON.parse(event.data);

    // 1️⃣ Handling the User List Update (With Profile Pics)
    if (data.type === 'userListUpdate' && Array.isArray(data.users)) {
        const userItems = document.getElementById("userItems");
        userItems.innerHTML = "";

        data.users.forEach(user => {
            const li = document.createElement("li");
            li.className = "user-list";

            // Create profile picture element
            const img = document.createElement("img");
            img.src = "/Connect/GetProfileImage?username=" + encodeURIComponent(user);
            img.className = "user-profile-pic";

            // Create name text
            const span = document.createElement("span");
            span.textContent = user;

            // Append profile pic and name
            li.appendChild(img);
            li.appendChild(span);

            li.onclick = function () { loadChat(user); };
            userItems.appendChild(li);
        });
    } 
    // 2️⃣ Handling the Messages (Multiple Messages at Once)
    else if (Array.isArray(data)) {
        data.forEach(msg => {
            const isOwnMessage = msg.sender === username;
            displayMessage(msg.sender, msg.message, isOwnMessage);
        });
    } 
    // 3️⃣ Handling a Single Message
    else {
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
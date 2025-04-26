import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.*;

@ServerEndpoint("/chat/{username}")  // WebSocket endpoint URI with username parameter
public class ChatWebSocket {

    // Database connection settings
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Connect;user=Vijay;password=#;encrypt=false";

    // Map to store active user sessions
    private static Map<String, Session> userSessions = new ConcurrentHashMap<>();

    // Handle new WebSocket connection
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        userSessions.put(username, session);
        System.out.println("User connected: " + username);
        sendUserListUpdate(username);  // Pass username to send only to the requesting user
    }

    // Handle incoming messages from the client
    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String type = jsonMessage.getString("type");

            if ("sendMessage".equals(type)) {
                handleSendMessage(jsonMessage, session);
                String username = jsonMessage.getString("sender");
                sendUserListUpdateToAll();
            } else if ("loadChat".equals(type)) {
                handleLoadChat(jsonMessage, session);
            } else if ("loadUserList".equals(type)) {
                String username = jsonMessage.getString("user");  // Get the username who requested the list
                sendUserListUpdate(username);  // Pass username to send only to the requesting user
            }
        } catch (JSONException e) {
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("error", "Invalid message format.");
            session.getBasicRemote().sendText(errorMessage.toString());
        }
    }


    // Send updated user list to all active users
private void sendUserListUpdateToAll() {
    // Iterate through all active user sessions
    for (String username : userSessions.keySet()) {
        JSONArray updatedList = getUpdatedUserListFor(username);

        // Send the updated list to the user
        Session userSession = userSessions.get(username);
        if (userSession != null && userSession.isOpen()) {
            try {
                JSONObject userListMessage = new JSONObject();
                userListMessage.put("type", "userListUpdate");
                userListMessage.put("users", updatedList);
                userSession.getBasicRemote().sendText(userListMessage.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

// Helper method to get the updated user list for a specific user
private JSONArray getUpdatedUserListFor(String username) {
    JSONArray userList = new JSONArray();
    try (Connection connection = DriverManager.getConnection(DB_URL)) {
        String query = "SELECT users, MAX(timestamp) AS latest_timestamp " +
                       "FROM ( " +
                       "   SELECT CASE " +
                       "       WHEN sender = ? THEN receiver " +
                       "       WHEN receiver = ? THEN sender " +
                       "   END AS users, " +
                       "   timestamp " +
                       "   FROM Messages " +
                       "   WHERE sender = ? OR receiver = ? " +
                       ") AS user_interactions " +
                       "GROUP BY users " +
                       "ORDER BY latest_timestamp DESC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            ps.setString(4, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userList.put(rs.getString("users"));
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return userList;
}


    // Handle sending messages
    private void handleSendMessage(JSONObject jsonMessage, Session session) throws IOException {
        String sender = jsonMessage.getString("sender");
        String receiver = jsonMessage.getString("receiver");
        String message = jsonMessage.getString("message");

        // Save message to the database
        try (Connection cn = DriverManager.getConnection(DB_URL)) {
            String query = "INSERT INTO Messages (sender, receiver, message, timestamp) VALUES (?, ?, ?, GETDATE())";
            try (PreparedStatement ps = cn.prepareStatement(query)) {
                ps.setString(1, sender);
                ps.setString(2, receiver);
                ps.setString(3, message);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Send message to receiver if online
        Session receiverSession = userSessions.get(receiver);
        if (receiverSession != null && receiverSession.isOpen()) {
            JSONObject forwardMessage = new JSONObject();
            forwardMessage.put("sender", sender);
            forwardMessage.put("message", message);
            receiverSession.getBasicRemote().sendText(forwardMessage.toString());
        }

        // Send message to sender to update their own chat window
        JSONObject senderMessage = new JSONObject();
        senderMessage.put("sender", sender);
        senderMessage.put("message", message);
        session.getBasicRemote().sendText(senderMessage.toString());
    }

    // Handle loading chat history between two users
    private void handleLoadChat(JSONObject jsonMessage, Session session) throws IOException {
        String currentUser = jsonMessage.getString("user");
        String receiver = jsonMessage.getString("receiver");

        JSONArray chatHistory = new JSONArray();
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT sender, message, timestamp FROM Messages WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY timestamp";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, currentUser);
                ps.setString(2, receiver);
                ps.setString(3, receiver);
                ps.setString(4, currentUser);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        JSONObject message = new JSONObject();
                        message.put("sender", rs.getString("sender"));
                        message.put("message", rs.getString("message"));
                        message.put("timestamp", rs.getString("timestamp"));
                        chatHistory.put(message);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        session.getBasicRemote().sendText(chatHistory.toString());
    }
// Send updated user list to the requesting user
private void sendUserListUpdate(String username) 
{
    JSONArray userList = new JSONArray();
    try (Connection connection = DriverManager.getConnection(DB_URL)) {
        // Query to retrieve users who have interacted with the requesting user
        String query = "SELECT users, MAX(timestamp) AS latest_timestamp " +
                       "FROM ( " +
                       "   SELECT CASE " +
                       "       WHEN sender = ? THEN receiver " +
                       "       WHEN receiver = ? THEN sender " +
                       "   END AS users, " +
                       "   timestamp " +
                       "   FROM Messages " +
                       "   WHERE sender = ? OR receiver = ? " +
                       ") AS user_interactions " +
                       "GROUP BY users " +
                       "ORDER BY latest_timestamp DESC";  // Sorting by the latest interaction
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);  // username who is requesting the user list
            ps.setString(2, username);
            ps.setString(3, username);
            ps.setString(4, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userList.put(rs.getString("users"));
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    // Send the updated user list to the requesting user only
    JSONObject userListMessage = new JSONObject();
    userListMessage.put("type", "userListUpdate");
    userListMessage.put("users", userList);

    // Send the user list to the specific user who requested it
    Session requestingUserSession = userSessions.get(username);
    if (requestingUserSession != null && requestingUserSession.isOpen()) {
        try {
            requestingUserSession.getBasicRemote().sendText(userListMessage.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        userSessions.remove(username);
        System.out.println("User disconnected: " + username);
        sendUserListUpdate(username);  // Pass username to send only to the requesting user
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}

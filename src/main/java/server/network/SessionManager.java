package server.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import server.entity.EntityManager;
import server.entity.Player;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EntityManager entityManager;

    public SessionManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Broadcasts the game state to all connected players.
     */
    public void broadcastGameState() {
        try {
            Map<String, Object> entities = Map.of(
            "players", entityManager.getPlayers(),
            "projectiles", entityManager.getProjectiles()
            //"items", entityManager.getItems(),
            //"walls", entityManager.getWalls()
        );

        Map<String, Object> gameState = Map.of("entities", entities);
        String gameStateJson = objectMapper.writeValueAsString(gameState);

            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    synchronized (session) { // Prevent concurrent send issues
                        session.sendMessage(new TextMessage(gameStateJson));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new player session and assigns an ID.
     */
    public void addSession(WebSocketSession session) {
        int team = (int) (Math.random() * 2) + 1;
        Player newPlayer = new Player(team);

        sessions.put(newPlayer.getId(), session);  // Store session by player ID
        entityManager.addPlayer(newPlayer);        // Add player to EntityManager

        // Send Player ID to newly connected player
        try {
            System.out.println("Player added: " + newPlayer.getId());
            Map<String, Object> response = Map.of(
                "type", "assign_id",
                "playerId", newPlayer.getId()
            );
            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates player state based on incoming WebSocket messages.
     */
    public void updatePlayerState(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> incomingMessage = objectMapper.readValue(payload, Map.class);

        Player player = entityManager.getPlayers().stream()
            .filter(p -> sessions.get(p.getId()) == session)
            .findFirst()
            .orElse(null);

        if (player != null) {
            entityManager.updatePlayerState(player, incomingMessage);
        }
    }

    // 
    // Remove Session
    //
    public void removeSession(WebSocketSession session) {
        // Find player associated with this session
        String playerIdToRemove = null;
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                playerIdToRemove = entry.getKey();
                break;
            }
        }
    
        if (playerIdToRemove != null) {
            System.out.println("Removing player: " + playerIdToRemove);
            
            // Remove player from session and entity manager
            sessions.remove(playerIdToRemove);
            entityManager.removePlayer(playerIdToRemove);
    
            // Attempt to close WebSocket session safely
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    

    //
    // Returns all active WebSocket sessions.
    //
    public Collection<WebSocketSession> getSessions() {
        return sessions.values();
    }
}

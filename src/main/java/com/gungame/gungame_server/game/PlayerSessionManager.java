package com.gungame.gungame_server.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gungame.gungame_server.entity.Player;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerSessionManager {

    private final Map<String, WebSocketSession> players = new ConcurrentHashMap<>();
    private final Map<String, Player> playerStates = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addPlayer(WebSocketSession session) {
        int team = (int) (Math.random() * 2) + 1;
        Player newPlayer = new Player(team);

        players.put(session.getId(), session);
        playerStates.put(session.getId(), newPlayer);

        try {
            Map<String, Object> response = Map.of(
                    "type", "assign_id",  // 메시지 타입 추가
                    "playerId", newPlayer.getId()
            );
            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json)); // 새로운 플레이어에게 ID 전송
            System.out.println("Player added: " + newPlayer.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void updatePlayerState(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> incomingMessage = objectMapper.readValue(payload, Map.class);

        Player player = playerStates.get(session.getId());
        if (player != null) {
            player.updateState(incomingMessage);
        } else {
            System.out.println("Player not found for session: " + session.getId());
        }
    }



    public void removePlayer(WebSocketSession session) {
        players.remove(session.getId());
        playerStates.remove(session.getId());
        System.out.println("player removed");
    }

    public Map<String, Player> getPlayerStates() {
        return playerStates;
    }

    public Collection<WebSocketSession> getSessions() {
        return players.values();
    }
}

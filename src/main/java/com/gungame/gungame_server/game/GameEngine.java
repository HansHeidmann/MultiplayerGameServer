package com.gungame.gungame_server.game;/*
 * created by seokhyun on 2025-02-13.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gungame.gungame_server.entity.Player;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class GameEngine {

    private final PlayerSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameEngine(PlayerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        startGameLoop();
    }

    private void startGameLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30);
                    for (Player player : sessionManager.getPlayerStates().values()) {
                        updatePlayerPosition(player);
                    }
                    broadcastGameState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 게임 상태 브로드캐스트
    public void broadcastGameState() {
        try {
            Map<String, Object> gameState = new ConcurrentHashMap<>();
            gameState.put("type", "update"); // 메시지 타입 지정
            gameState.put("players", sessionManager.getPlayerStates());

            String gameStateJson = objectMapper.writeValueAsString(gameState);

            for (WebSocketSession session : sessionManager.getSessions()) {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(gameStateJson));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 새 플레이어 추가 후 상태 브로드캐스트
    public void addPlayer(WebSocketSession session) {
        sessionManager.addPlayer(session); // PlayerSessionManager를 통해 플레이어 추가

        // assign_id 메시지를 추가된 플레이어에게 전송
        Player newPlayer = sessionManager.getPlayerStates().get(session.getId());
        if (newPlayer != null) {
            try {
                Map<String, Object> response = Map.of(
                        "type", "assign_id",
                        "playerId", newPlayer.getId()
                );
                String json = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(json)); // ID 전송
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        broadcastGameState(); // 전체 상태 브로드캐스트
    }

    // 플레이어 제거 후 상태 브로드캐스트
    public void removePlayer(WebSocketSession session) {
        sessionManager.removePlayer(session); // PlayerSessionManager를 통해 플레이어 제거
        broadcastGameState(); // 전체 상태 브로드캐스트
    }

    private void updatePlayerPosition(Player player) {
        player.updatePosition();
    }

}

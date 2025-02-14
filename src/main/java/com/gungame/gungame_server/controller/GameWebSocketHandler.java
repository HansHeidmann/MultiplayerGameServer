package com.gungame.gungame_server.controller;

import com.gungame.gungame_server.game.GameEngine;
import com.gungame.gungame_server.game.PlayerSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final PlayerSessionManager sessionManager;
    private final GameEngine gameEngine;

    public GameWebSocketHandler(PlayerSessionManager sessionManager, GameEngine gameEngine) {
        this.sessionManager = sessionManager;
        this.gameEngine = gameEngine;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("WebSocket connection established: " + session.getId());
        gameEngine.addPlayer(session); // GameEngine에 플레이어 추가
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("WebSocket connection closed: " + session.getId());
        gameEngine.removePlayer(session); // GameEngine에서 플레이어 제거
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // Update player state based on the received message
            sessionManager.updatePlayerState(session, message);
        } catch (Exception e) {
            // Log any exceptions to ensure the WebSocket session remains stable
            System.err.println("Error processing message from session: " + session.getId());
            e.printStackTrace();
        }
    }
}

package com.gungame.gungame_server.entity;

import java.util.Map;
import java.util.UUID;

public class Player {
    private final String id; // UUID-based Player ID
    private int x, y;
    private double angle;
    private final int team;
    private Map<String, Boolean> keys;

    public Player(int team) {
        this.id = UUID.randomUUID().toString();
        this.team = team;
        this.x = 0;
        this.y = 0;
        this.angle = 0;
        this.keys = Map.of("w", false, "a", false, "s", false, "d", false);
    }

    public void updateState(Map<String, Object> state) {
        if (state.containsKey("x")) {
            this.x = ((Number) state.get("x")).intValue();
        }
        if (state.containsKey("y")) {
            this.y = ((Number) state.get("y")).intValue();
        }
        if (state.containsKey("angle")) {
            this.angle = ((Number) state.get("angle")).doubleValue();
        }
        if (state.containsKey("keys")) {
            this.keys = (Map<String, Boolean>) state.get("keys");
        }
    }

    // 위치 업데이트 로직
    public void updatePosition() {
        int speed = 5; // 이동 속도
        if (keys != null) { // keys가 null이 아닌지 확인
            if (keys.getOrDefault("w", false)) this.y -= speed;
            if (keys.getOrDefault("s", false)) this.y += speed;
            if (keys.getOrDefault("a", false)) this.x -= speed;
            if (keys.getOrDefault("d", false)) this.x += speed;
        } else {
            System.out.println("Keys are null for player: " + id);
        }
    }


    // Getters
    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getAngle() { return angle; }
    public int getTeam() { return team; }
    public Map<String, Boolean> getKeys() { return keys; }

    // 기타 유틸리티 메서드 추가 가능
}

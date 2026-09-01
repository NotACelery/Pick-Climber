package dev.maicra.pickclimber.climb;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class ClimbStateStore {
    private static final Map<UUID, ServerClimbState> SERVER_STATES = new HashMap<>();
    private static final Map<UUID, ClientClimbState> CLIENT_STATES = new HashMap<>();
    private static final Map<UUID, RemoteAnchorPoseState> REMOTE_ANCHOR_POSES = new HashMap<>();

    private ClimbStateStore() {
    }

    static boolean hasServer(UUID playerId) {
        return SERVER_STATES.containsKey(playerId);
    }

    static ServerClimbState server(UUID playerId) {
        return SERVER_STATES.get(playerId);
    }

    static void putServer(UUID playerId, ServerClimbState state) {
        SERVER_STATES.put(playerId, state);
    }

    static ServerClimbState removeServer(UUID playerId) {
        return SERVER_STATES.remove(playerId);
    }

    static boolean hasClient(UUID playerId) {
        return CLIENT_STATES.containsKey(playerId);
    }

    static ClientClimbState client(UUID playerId) {
        return CLIENT_STATES.get(playerId);
    }

    static void putClient(UUID playerId, ClientClimbState state) {
        CLIENT_STATES.put(playerId, state);
    }

    static ClientClimbState removeClient(UUID playerId) {
        return CLIENT_STATES.remove(playerId);
    }

    static void clearClients() {
        CLIENT_STATES.clear();
    }

    static RemoteAnchorPoseState remotePose(UUID playerId) {
        return REMOTE_ANCHOR_POSES.get(playerId);
    }

    static void putRemotePose(UUID playerId, RemoteAnchorPoseState state) {
        REMOTE_ANCHOR_POSES.put(playerId, state);
    }

    static void removeRemotePose(UUID playerId) {
        REMOTE_ANCHOR_POSES.remove(playerId);
    }

    static void clearRemotePoses() {
        REMOTE_ANCHOR_POSES.clear();
    }
}

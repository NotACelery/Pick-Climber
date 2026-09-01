package dev.maicra.pickclimber.climb;

final class ClimbTuning {
    static final int DURABILITY_COST = 15;
    static final int CEILING_DURABILITY_COST = 20;
    static final int CEILING_DURABILITY_INTERVAL_TICKS = 20;
    static final double CEILING_SWING_RADIUS = 0.665D;
    static final double CEILING_SWING_ACCELERATION = 0.025D;
    static final double CEILING_SWING_RETURN = 0.012D;
    static final double CEILING_SWING_DAMPING = 0.96D;
    static final double CEILING_SWING_MAX_SPEED = 0.18D;
    static final double WALL_JUMP_HORIZONTAL_IMPULSE = 0.65D;
    static final double CEILING_RELEASE_MOMENTUM_MAX_SPEED = 0.38D;
    static final double CEILING_RELEASE_MAX_SPEED = WALL_JUMP_HORIZONTAL_IMPULSE
            + CEILING_RELEASE_MOMENTUM_MAX_SPEED;
    static final int BRAKING_DURABILITY_PER_BLOCK = 10;
    static final int CRACK_STAGE = 5;
    static final int ANCHOR_COOLDOWN_TICKS = 20;
    static final int UNSTABLE_ANCHOR_COOLDOWN_TICKS = 40;
    static final double RISING_VELOCITY_THRESHOLD = 0.08D;
    static final int JUMP_BOOST_WINDOW_TICKS = 8;
    static final double BASE_BOOST_HEIGHT = 1.0D;
    static final double BOOST_HEIGHT_PER_ENCHANTMENT_LEVEL = 0.5D;
    static final double WALL_JUMP_HEIGHT_PER_ENCHANTMENT_LEVEL = 0.5D;
    static final float PINNED_SWING_PROGRESS = 0.5F;
    static final float PINNED_POSE_RAMP_TICKS = 4.0F;
    static final double MAX_HIT_DISTANCE_SQR = 25.0D;
    static final double MAX_INDICATOR_DISTANCE_SQR = 9.0D;
    static final double MAX_ANCHOR_MOVE_SQR = 2.25D;
    static final double MAX_DRIFT_DISTANCE_SQR = 16.0D;
    static final double ATTACHMENT_COHERENCE_DISTANCE_SQR = 1.0D;
    static final int WALL_TARGET_COLLISION_STEPS = 18;
    static final int CRACK_REFRESH_INTERVAL = 10;
    static final int SERVER_SYNC_INTERVAL = 5;
    static final int CLIENT_SYNC_TIMEOUT_TICKS = 40;
    static final int REMOTE_POSE_REFRESH_INTERVAL_TICKS = 20;
    static final int FAILED_ATTACH_GRACE_TICKS = 5;
    static final double BRAKING_START_SPEED = -0.40D;
    static final float BRAKING_MIN_FALL_DISTANCE = 5.0F;
    static final double BRAKING_STOP_SPEED = -0.08D;
    static final double BRAKING_DRAG = 0.75D;
    static final double BRAKING_RECOVERY = 0.035D;
    static final double UNSTABLE_SLIDE_SPEED = -0.136D;
    static final double MAX_BRAKING_MOVE_PER_TICK = 0.60D;
    static final double CONTACT_BLOCK_EPSILON = 1.0E-3D;

    static final double ANCHOR_WALL_CLEARANCE = 0.08D;
    static final double ANCHOR_FACE_EDGE_INSET = 0.04D;
    static final double ANCHOR_FACE_EDGE_MAX = 1.0D - ANCHOR_FACE_EDGE_INSET;
    static final double WALL_TARGET_HEIGHT_FACTOR = 0.62D;
    static final double MAX_VERTICAL_TARGET_CORRECTION_FACTOR = 0.5D;
    static final double WALL_HOLD_CORRECTION_DISTANCE_SQR = 2.5E-3D;
    static final double CEILING_HOLD_CORRECTION_DISTANCE_SQR = 1.0E-8D;
    static final double DIRECTION_EPSILON_SQR = 1.0E-5D;
    static final double BRAKING_LATERAL_SPEED_FACTOR = 0.5D;
    static final int BRAKING_SUPPORT_STEPS = 2;

    static final double WALL_JUMP_BASE_VERTICAL = 0.28D;
    static final double WALL_JUMP_LOOK_VERTICAL_FACTOR = 0.32D;
    static final double WALL_JUMP_MIN_VERTICAL = 0.12D;
    static final double WALL_JUMP_MAX_VERTICAL = 0.52D;
    static final double RISE_SEARCH_INITIAL_MARGIN = 0.5D;
    static final double RISE_SEARCH_MAX_VELOCITY = 4.0D;
    static final double RISE_SEARCH_GROWTH = 1.35D;
    static final int RISE_SEARCH_ITERATIONS = 24;
    static final int VERTICAL_RISE_PREDICTION_TICKS = 80;
    static final double VANILLA_GRAVITY_PER_TICK = 0.08D;
    static final double VANILLA_VERTICAL_DRAG = 0.98D;

    static final double ANCHOR_SOUND_VOLUME_OFFSET = 1.0D;
    static final double ANCHOR_SOUND_VOLUME_SCALE = 0.22D;
    static final float ANCHOR_SOUND_MIN_VOLUME = 0.25F;
    static final float ANCHOR_SOUND_MAX_VOLUME = 0.65F;
    static final float ANCHOR_SOUND_PITCH_BASE = 0.88F;
    static final float ANCHOR_SOUND_PITCH_VARIATION = 0.08F;

    private ClimbTuning() {
    }
}

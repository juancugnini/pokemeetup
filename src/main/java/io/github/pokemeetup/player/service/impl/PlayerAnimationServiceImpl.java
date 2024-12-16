package io.github.pokemeetup.player.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import io.github.pokemeetup.player.model.PlayerDirection;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of PlayerAnimationService to handle player animations.
 */
@Service
public class PlayerAnimationServiceImpl implements PlayerAnimationService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerAnimationServiceImpl.class);

    // Frame durations aligned with movement durations
    private static final float WALK_FRAME_DURATION = 0.06f; // 5 frames per 0.3s walk step
    private static final float RUN_FRAME_DURATION = 0.03f;  // 5 frames per 0.15s run step

    private TextureRegion standingUp, standingDown, standingLeft, standingRight;
    private Animation<TextureRegion> walkUp, walkDown, walkLeft, walkRight;
    private Animation<TextureRegion> runUp, runDown, runLeft, runRight;

    /**
     * Constructor initializes animations.
     */
    public PlayerAnimationServiceImpl() {
        try {
            logger.info("Initializing PlayerAnimationServiceImpl...");
            loadAnimations();
            logger.info("PlayerAnimationServiceImpl initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize PlayerAnimationServiceImpl: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves the current animation frame based on player state.
     *
     * @param direction  Current direction of the player.
     * @param moving     Whether the player is moving.
     * @param running    Whether the player is running.
     * @param stateTime  Time elapsed in the current animation state.
     * @return Current TextureRegion to render.
     */
    @Override
    public TextureRegion getCurrentFrame(PlayerDirection direction, boolean moving, boolean running, float stateTime) {
        if (!moving) {
            return getStandingFrame(direction);
        }

        Animation<TextureRegion> anim = getMovementAnimation(direction, running);
        return anim.getKeyFrame(stateTime, true); // Looping animation
    }

    /**
     * Retrieves the standing frame based on direction.
     *
     * @param direction Current direction of the player.
     * @return TextureRegion representing the standing frame.
     */
    @Override
    public TextureRegion getStandingFrame(PlayerDirection direction) {
        return switch (direction) {
            case UP -> standingUp;
            case DOWN -> standingDown;
            case LEFT -> standingLeft;
            case RIGHT -> standingRight;
            default -> standingDown;
        };
    }

    /**
     * Retrieves the appropriate movement animation based on direction and running state.
     *
     * @param direction Current direction of the player.
     * @param running   Whether the player is running.
     * @return Corresponding Animation<TextureRegion>.
     */
    private Animation<TextureRegion> getMovementAnimation(PlayerDirection direction, boolean running) {
        return switch (direction) {
            case UP -> running ? runUp : walkUp;
            case DOWN -> running ? runDown : walkDown;
            case LEFT -> running ? runLeft : walkLeft;
            case RIGHT -> running ? runRight : walkRight;
            default -> walkDown;
        };
    }

    /**
     * Loads all necessary animations from the TextureAtlas.
     */
    private void loadAnimations() {
        String atlasPath = "assets/atlas/boy-gfx-atlas";
        logger.info("Loading TextureAtlas from path: {}", atlasPath);
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(atlasPath));

        logger.info("Available regions in the atlas:");
        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
            logger.info("- {} (index: {})", region.name, region.index);
        }

        walkUp = createLoopAnimation(atlas, "boy_walk_up", WALK_FRAME_DURATION);
        walkDown = createLoopAnimation(atlas, "boy_walk_down", WALK_FRAME_DURATION);
        walkLeft = createLoopAnimation(atlas, "boy_walk_left", WALK_FRAME_DURATION);
        walkRight = createLoopAnimation(atlas, "boy_walk_right", WALK_FRAME_DURATION);

        // Set standing frames to the first frame of walking animations
        standingUp = walkUp.getKeyFrames()[0];
        standingDown = walkDown.getKeyFrames()[0];
        standingLeft = walkLeft.getKeyFrames()[0];
        standingRight = walkRight.getKeyFrames()[0];

        runUp = createLoopAnimation(atlas, "boy_run_up", RUN_FRAME_DURATION);
        runDown = createLoopAnimation(atlas, "boy_run_down", RUN_FRAME_DURATION);
        runLeft = createLoopAnimation(atlas, "boy_run_left", RUN_FRAME_DURATION);
        runRight = createLoopAnimation(atlas, "boy_run_right", RUN_FRAME_DURATION);
    }

    /**
     * Creates a looping animation for a given base name.
     *
     * @param atlas    TextureAtlas containing the animation frames.
     * @param baseName Base name of the animation frames.
     * @param duration Duration per frame.
     * @return Looping Animation<TextureRegion>.
     */
    private Animation<TextureRegion> createLoopAnimation(TextureAtlas atlas, String baseName, float duration) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(baseName);
        if (regions.size == 0) {
            logger.error("No regions found for animation: {}", baseName);
            throw new RuntimeException("No regions found for animation: " + baseName);
        }

        regions.sort((a, b) -> Integer.compare(a.index, b.index));

        // Log number of frames and duration
        logger.info("Creating animation '{}', frames: {}, frameDuration: {}",
                baseName, regions.size, duration);

        Animation<TextureRegion> anim = new Animation<>(duration, regions, Animation.PlayMode.LOOP);
        logger.debug("Created loop animation: {}", baseName);
        return anim;
    }

    /**
     * Disposes of animation resources if necessary.
     */
    public void dispose() {
        // Dispose of resources if necessary
    }
}

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


@Service
public class PlayerAnimationServiceImpl implements PlayerAnimationService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerAnimationServiceImpl.class);


    private static final float WALK_FRAME_DURATION = 0.06f;
    private static final float RUN_FRAME_DURATION = 0.03f;

    private TextureRegion standingUp, standingDown, standingLeft, standingRight;
    private Animation<TextureRegion> walkUp, walkDown, walkLeft, walkRight;
    private Animation<TextureRegion> runUp, runDown, runLeft, runRight;
    private boolean initialized = false;

    
    public PlayerAnimationServiceImpl() {

    }

    
    @Override
    public TextureRegion getCurrentFrame(PlayerDirection direction, boolean moving, boolean running, float stateTime) {
        if (!moving) {
            return getStandingFrame(direction);
        }

        Animation<TextureRegion> anim = getMovementAnimation(direction, running);
        return anim.getKeyFrame(stateTime, true);
    }

    
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

    
    private Animation<TextureRegion> getMovementAnimation(PlayerDirection direction, boolean running) {
        return switch (direction) {
            case UP -> running ? runUp : walkUp;
            case DOWN -> running ? runDown : walkDown;
            case LEFT -> running ? runLeft : walkLeft;
            case RIGHT -> running ? runRight : walkRight;
            default -> walkDown;
        };
    }

    
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


        standingUp = walkUp.getKeyFrames()[0];
        standingDown = walkDown.getKeyFrames()[0];
        standingLeft = walkLeft.getKeyFrames()[0];
        standingRight = walkRight.getKeyFrames()[0];

        runUp = createLoopAnimation(atlas, "boy_run_up", RUN_FRAME_DURATION);
        runDown = createLoopAnimation(atlas, "boy_run_down", RUN_FRAME_DURATION);
        runLeft = createLoopAnimation(atlas, "boy_run_left", RUN_FRAME_DURATION);
        runRight = createLoopAnimation(atlas, "boy_run_right", RUN_FRAME_DURATION);
    }

    public void initAnimationsIfNeeded() {
        if (!initialized) {
            loadAnimations();
            initialized = true;
        }
    }

    
    private Animation<TextureRegion> createLoopAnimation(TextureAtlas atlas, String baseName, float duration) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(baseName);
        if (regions.size == 0) {
            logger.error("No regions found for animation: {}", baseName);
            throw new RuntimeException("No regions found for animation: " + baseName);
        }

        regions.sort((a, b) -> Integer.compare(a.index, b.index));


        logger.info("Creating animation '{}', frames: {}, frameDuration: {}",
                baseName, regions.size, duration);

        Animation<TextureRegion> anim = new Animation<>(duration, regions, Animation.PlayMode.LOOP);
        logger.debug("Created loop animation: {}", baseName);
        return anim;
    }

    
    public void dispose() {

    }
}

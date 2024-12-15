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

    private TextureRegion standingUp, standingDown, standingLeft, standingRight;

    private Animation<TextureRegion> walkUp, walkDown, walkLeft, walkRight;
    private Animation<TextureRegion> runUp, runDown, runLeft, runRight;

    
    private static final float WALK_FRAME_DURATION = 0.1125f; 
    private static final float RUN_FRAME_DURATION = 0.075f;   

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

        
        walkUp = createAnimation(atlas, "boy_walk_up", WALK_FRAME_DURATION);
        walkDown = createAnimation(atlas, "boy_walk_down", WALK_FRAME_DURATION);
        walkLeft = createAnimation(atlas, "boy_walk_left", WALK_FRAME_DURATION);
        walkRight = createAnimation(atlas, "boy_walk_right", WALK_FRAME_DURATION);

        
        standingUp = walkUp.getKeyFrame(0);
        standingDown = walkDown.getKeyFrame(0);
        standingLeft = walkLeft.getKeyFrame(0);
        standingRight = walkRight.getKeyFrame(0);

        
        runUp = createAnimation(atlas, "boy_run_up", RUN_FRAME_DURATION);
        runDown = createAnimation(atlas, "boy_run_down", RUN_FRAME_DURATION);
        runLeft = createAnimation(atlas, "boy_run_left", RUN_FRAME_DURATION);
        runRight = createAnimation(atlas, "boy_run_right", RUN_FRAME_DURATION);
    }

    private Animation<TextureRegion> createAnimation(TextureAtlas atlas, String baseName, float duration) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(baseName);
        if (regions.size == 0) {
            logger.error("No regions found for animation: {}", baseName);
            throw new RuntimeException("No regions found for animation: " + baseName);
        }

        
        regions.sort((a, b) -> Integer.compare(a.index, b.index));

        TextureRegion[] frames = new TextureRegion[regions.size];
        for (int i = 0; i < regions.size; i++) {
            frames[i] = regions.get(i);
            logger.debug("Loaded frame for {}: index={}, name={}", baseName, regions.get(i).index, regions.get(i).name);
        }

        Animation<TextureRegion> anim = new Animation<>(duration, frames);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        logger.debug("Created animation: {}", baseName);
        return anim;
    }

    
    public void dispose() {
        
    }
}

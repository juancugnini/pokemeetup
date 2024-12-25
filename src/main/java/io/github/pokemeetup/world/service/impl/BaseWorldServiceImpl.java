
package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.graphics.OrthographicCamera;
import io.github.pokemeetup.world.service.WorldService;

public abstract class BaseWorldServiceImpl implements WorldService {
    protected OrthographicCamera camera;

    @Override
    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }
}
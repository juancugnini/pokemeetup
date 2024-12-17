package io.github.pokemeetup;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class PokemeetupApplication {

	private static ApplicationContext springContext;

	public static void main(String[] args) {
		springContext = SpringApplication.run(PokemeetupApplication.class, args);

		Game game = springContext.getBean(Game.class);

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(game, config);
	}

	public static ApplicationContext getSpringContext() {
		return springContext;
	}
}

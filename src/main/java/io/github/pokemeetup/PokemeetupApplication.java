package io.github.pokemeetup;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class PokemeetupApplication {

	@Getter
	private static ApplicationContext springContext;

	public static void main(String[] args) {
		springContext = SpringApplication.run(PokemeetupApplication.class, args);

		Game game = springContext.getBean(Game.class);

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(game, config);
	}

}

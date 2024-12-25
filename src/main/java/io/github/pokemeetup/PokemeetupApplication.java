package io.github.pokemeetup;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.awt.*;

@SpringBootApplication
@ComponentScan("io.github.pokemeetup")
public class PokemeetupApplication {

	@Getter
	private static ApplicationContext springContext;

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		springContext = SpringApplication.run(PokemeetupApplication.class, args);

		DisplayMode monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().
					getDefaultScreenDevice().getDisplayMode();

		Game game = springContext.getBean(Game.class);

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("PokeMeetup");
		config.setWindowedMode(monitor.getWidth(), monitor.getHeight());
		//gives borderless window effect
		config.setDecorated(false);

		config.setForegroundFPS(monitor.getRefreshRate());
		config.useVsync(true);

		new Lwjgl3Application(game, config);
	}
}
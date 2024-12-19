	package io.github.pokemeetup;

	import com.badlogic.gdx.Game;
	import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
	import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
	import lombok.Getter;
	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;
	import org.springframework.context.ApplicationContext;
	import org.springframework.context.annotation.ComponentScan;

	@SpringBootApplication
	@ComponentScan("io.github.pokemeetup")
	public class PokemeetupApplication {

		@Getter
		private static ApplicationContext springContext;

		public static void main(String[] args) {
			springContext = SpringApplication.run(PokemeetupApplication.class, args);

			Game game = springContext.getBean(Game.class);

			Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
			config.setTitle("PokeMeetup");
			config.setWindowedMode(1280, 720);
			config.setForegroundFPS(60);
			config.useVsync(true);

			new Lwjgl3Application(game, config);
		}
	}
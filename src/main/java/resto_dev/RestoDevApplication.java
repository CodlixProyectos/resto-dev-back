package resto_dev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class RestoDevApplication {

	public static void main(String[] args) {
		String dotenvDir = "./";
		if (new java.io.File("./resto-dev/resto-dev/.env").exists()) {
			dotenvDir = "./resto-dev/resto-dev";
		}

		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
				.directory(dotenvDir)
				.ignoreIfMissing()
				.load();
		
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(RestoDevApplication.class, args);
	}

}

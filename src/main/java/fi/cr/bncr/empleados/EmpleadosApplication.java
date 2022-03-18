package fi.cr.bncr.empleados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class EmpleadosApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmpleadosApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void loadingBrowser() {
		/*String cmd = "open http://localhost:8080";
		Runtime run = Runtime.getRuntime();
		try {
			Process pr = run.exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}

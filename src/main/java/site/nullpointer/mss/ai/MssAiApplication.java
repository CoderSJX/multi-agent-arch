package site.nullpointer.mss.ai;

import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;

@SpringBootApplication(exclude = { WebMvcAutoConfiguration.class, OpenAiAutoConfiguration.class })
public class MssAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MssAiApplication.class, args);
    }

}

package org.bamc.security;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class SecurityChecker extends ListenerAdapter implements CommandLineRunner {
    
    @Value("${security.projectName}")
    private String projectName;

    private final RestTemplate restTemplate;

    private String fileUrlToCheck = "https://raw.githubusercontent.com/Bamc-dev/securityChecker/master/src/main/resources/verif.txt";

    public SecurityChecker(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        String waitingMessage =
                " __          __          _   _     _                                                                               _   \n" +
                        " \\ \\        / /         (_) | |   (_)                                                                             | |  \n" +
                        "  \\ \\  /\\  / /    __ _   _  | |_   _   _ __     __ _     _ __     __ _   _   _    ___   _ __ ___     ___   _ __   | |_ \n" +
                        "   \\ \\/  \\/ /    / _` | | | | __| | | | '_ \\   / _` |   | '_ \\   / _` | | | | |  / _ \\ | '_ ` _ \\   / _ \\ | '_ \\  | __|\n" +
                        "    \\  /\\  /    | (_| | | | | |_  | | | | | | | (_| |   | |_) | | (_| | | |_| | |  __/ | | | | | | |  __/ | | | | | |_ \n" +
                        "     \\/  \\/      \\__,_| |_|  \\__| |_| |_| |_|  \\__, |   | .__/   \\__,_|  \\__, |  \\___| |_| |_| |_|  \\___| |_| |_|  \\__|\n" +
                        "                                                __/ |   | |               __/ |                                        \n" +
                        "                                               |___/    |_|              |___/                                         \n";

        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("The required property 'projectName' is not set.");
        }
        Map<String, String> projectStatusMap = new HashMap<>();
        String fileContent = restTemplate.getForObject(fileUrlToCheck, String.class);
        if (fileContent != null) {
            String[] lines = fileContent.split("\\r?\\n");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    projectStatusMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        String projectStatus = projectStatusMap.get(projectName);
        JDABuilder builder = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN"));
        builder.build().awaitReady();
        TextChannel channel = builder.build().getTextChannelById(System.getenv("DISCORD_CHANNEL_ID"));
        switch (projectStatus)
        {
            case "incorrect":
                channel.sendMessage("Tried to start "+projectName+", but isn't correct");
                System.out.println(waitingMessage);
                System.exit(0);
                break;
            case "ko":
                channel.sendMessage("Tried to start "+projectName+", but isn't correct WARNING NEED TO KO");
                System.out.println(waitingMessage);
                System.exit(0);
                break;
            default:
                System.out.println("All correct");

        }
    }
}
package org.bamc.discord;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;

@Component
public class DiscordBot extends ListenerAdapter implements ApplicationListener<ApplicationStartingEvent> {

    @Value("${discord.bot.token}")
    private String token;

    @Value("${discord.bot.channelId}")
    private String channelId;

    @Value("${discord.bot.projectName}")
    private String projectName;

    @Value("${discord.bot.projectVersion}")
    private String projectVersion;

    private final String checkEmoji = "✅";
    private boolean isValidated = false;
    private Message projectMessage;

    @PostConstruct
    public void init() throws LoginException {
        JDABuilder.createDefault(token)
                .addEventListeners(this)
                .build();
    }

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        sendMessageIfNotExists();

        String waitingMessage =
                " __          __          _   _     _                                                                               _   \n" +
                        " \\ \\        / /         (_) | |   (_)                                                                             | |  \n" +
                        "  \\ \\  /\\  / /    __ _   _  | |_   _   _ __     __ _     _ __     __ _   _   _    ___   _ __ ___     ___   _ __   | |_ \n" +
                        "   \\ \\/  \\/ /    / _` | | | | __| | | | '_ \\   / _` |   | '_ \\   / _` | | | | |  / _ \\ | '_ ` _ \\   / _ \\ | '_ \\  | __|\n" +
                        "    \\  /\\  /    | (_| | | | | |_  | | | | | | | (_| |   | |_) | | (_| | | |_| | |  __/ | | | | | | |  __/ | | | | | |_ \n" +
                        "     \\/  \\/      \\__,_| |_|  \\__| |_| |_| |_|  \\__, |   | .__/   \\__,_|  \\__, |  \\___| |_| |_| |_|  \\___| |_| |_|  \\__|\n" +
                        "                                                __/ |   | |               __/ |                                        \n" +
                        "                                               |___/    |_|              |___/                                         \n";

        while (!isValidated) {
            try {
                System.out.println(waitingMessage);
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Validation received, continuing startup...");
    }

    private void sendMessageIfNotExists() {
        TextChannel channel = JDABuilder.createDefault(token).build().getTextChannelById(channelId);
        if (channel != null) {
            channel.getHistory().retrievePast(100).queue(messages -> {
                boolean messageExists = messages.stream()
                        .anyMatch(message -> message.getContentDisplay().contains(projectName + " " + projectVersion));
                if (!messageExists) {
                    channel.sendMessage("Project: " + projectName + " Version: " + projectVersion).queue(message -> {
                        projectMessage = message;
                    });
                } else {
                    projectMessage = messages.stream()
                            .filter(message -> message.getContentDisplay().contains(projectName + " " + projectVersion))
                            .findFirst()
                            .orElse(null);
                }
            });
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (projectMessage != null && event.getMessageId().equals(projectMessage.getId()) &&
                event.getReaction().getEmoji().equals(checkEmoji)) {
            isValidated = true;
        }
    }
}
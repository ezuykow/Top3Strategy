package ru.ezuykow.top3strategy.messages;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.ezuykow.top3strategy.services.StatisticsService;

/**
 * @author ezuykow
 */
@Component
@RequiredArgsConstructor
public class MessageSender {

    @Value("${telegram.chat.id}")
    private long chatId;

    private final TelegramBot bot;
    private final StatisticsService statisticsService;

    //-----------------API START-----------------

    public void send(String msg) {
        bot.execute(new SendMessage(chatId, msg));
    }

    public void sendStats() {
        bot.execute(new SendMessage(chatId, statisticsService.createStatsMsg()));
    }

    public void delete(int msgId) {
        bot.execute(new DeleteMessage(chatId, msgId));
    }

    //-----------------API END-----------------

}

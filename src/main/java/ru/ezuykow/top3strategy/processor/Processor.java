package ru.ezuykow.top3strategy.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ezuykow.top3strategy.messages.MessageSender;
import ru.ezuykow.top3strategy.services.StatisticsService;

/**
 * @author ezuykow
 */
@Component
@RequiredArgsConstructor
public class Processor {

    private final MessageSender msgSender;
    private final StatisticsService statisticsService;

    private boolean isStarted = false;
    private ParserThread parserThread;

    //-----------------API START-----------------

    public void startProcessor() {
        if (!isStarted) {
            parserThread = new ParserThread(msgSender, statisticsService);
            parserThread.start();
            isStarted = true;
        }
    }

    public void stopProcessor() {
        if (isStarted) {
            parserThread.interrupt();
        }
    }

    //-----------------API END-----------------

}

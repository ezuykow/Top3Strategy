package ru.ezuykow.top3strategy.processor;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import ru.ezuykow.top3strategy.entities.Statistics;
import ru.ezuykow.top3strategy.messages.MessageSender;
import ru.ezuykow.top3strategy.services.StatisticsService;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ezuykow
 */
@Log
public class ParserThread extends Thread{

    private static final String ARCHIVE_URL = "https://www.stoloto.ru/top3/archive";
    private static final String FIRST_ELEM_XPATH = "/html/body/div[1]/div[1]/div[6]/div[2]/div[2]/div[2]/div";
    private static final String SECOND_ELEM_XPATH = "/html/body/div[1]/div[1]/div[6]/div[2]/div[2]/div[3]/div";
    private static final String THIRD_ELEM_XPATH = "/html/body/div[1]/div[1]/div[6]/div[2]/div[2]/div[4]/div";
    private static final String GAME_TIME_XPATH_POSTFIX = "/div[1]";
    private static final String GAME_NUMBER_XPATH_POSTFIX = "/div[2]/a";
    private static final String NUMBERS_SPAN_XPATH_POSTFIX = "/div[3]/div[1]/div[1]/span";
    private static final int MAX_GAMES_IN_MEMORY = 5;
    private static final int BANK_DIFFER = 331;
    private static final int WAITING_STEP_MINUTES = 18;

    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class Game {

        @EqualsAndHashCode.Include
        int gameNumber;
        int[] numbers;
        LocalDateTime dateTime;
    }

    @AllArgsConstructor
    private static class Bet {

        int targetGameNumber;
        int targetNum;
    }

    private final MessageSender msgSender;
    private final StatisticsService statisticsService;

    private HtmlPage page;
    private final List<Game> games = new LinkedList<>();
    private final List<Bet> bets = new LinkedList<>();
    private boolean newGameAdded;
    private boolean isStartUp = true;

    public ParserThread(MessageSender msgSender, StatisticsService statisticsService) {
        this.msgSender = msgSender;
        this.statisticsService = statisticsService;
    }

    //-----------------API START-----------------

    public void run() {
        log.info("Parser started!");

        WebClient webClient = createWebClient();

        while (true) {
            try {
                page = webClient.getPage(ARCHIVE_URL);
                fillGamesIfStartUp();
                parseElem(FIRST_ELEM_XPATH);
                checkNewGameAdding();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                break;
            }
        }

        webClient.close();
        log.info("Parser stopped!");
    }

    //-----------------API END-----------------

    private WebClient createWebClient() {
        WebClient client = new WebClient(BrowserVersion.FIREFOX);
        client.setCssErrorHandler(new SilentCssErrorHandler());
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setDownloadImages(false);
        return client;
    }

    private void fillGamesIfStartUp() throws InterruptedException {
        if (isStartUp) {
            parseElem(THIRD_ELEM_XPATH);
            parseElem(SECOND_ELEM_XPATH);
            isStartUp = false;
        }
    }

    private void parseElem(String elemXPath) throws InterruptedException {
        HtmlDivision elemDiv = page.getFirstByXPath(elemXPath);

        int gameNumber = parseGameNumber(elemXPath, elemDiv);

        if (games.isEmpty() || games.get(games.size() - 1).gameNumber != gameNumber) {
            Game newGame = new Game(
                    gameNumber,
                    parseNumbers(elemXPath, elemDiv),
                    parseDateTimeText(elemXPath, elemDiv)
            );
            games.add(newGame);
            newGameAdded = true;
            log.info("New game parsed: " + newGame);
        } else {
            log.info("Repeated game " + gameNumber);
            newGameAdded = false;
        }
    }

    private int parseGameNumber(String elemXPath, HtmlDivision elemDiv) {
        HtmlAnchor anchor = elemDiv.getFirstByXPath(elemXPath + GAME_NUMBER_XPATH_POSTFIX);
        return Integer.parseInt(anchor.getVisibleText());
    }

    private int[] parseNumbers(String elemXPath, HtmlDivision elemDiv) throws InterruptedException {
        boolean dataUploading = true;
        int[] numbers = new int[3];

        while (dataUploading) {
            HtmlSpan span = elemDiv.getFirstByXPath(elemXPath + NUMBERS_SPAN_XPATH_POSTFIX);
            if (span == null) {
                sleep(60_000);
            } else {
                dataUploading = false;
                String text = span.asNormalizedText();
                numbers[0] = Integer.parseInt(text.substring(0, 1));
                numbers[1] = Integer.parseInt(text.substring(2, 3));
                numbers[2] = Integer.parseInt(text.substring(4, 5));
            }
        }
        return numbers;
    }

    private LocalDateTime parseDateTimeText(String elemXPath, HtmlDivision elemDiv) {
        HtmlDivision div = elemDiv.getFirstByXPath(elemXPath + GAME_TIME_XPATH_POSTFIX);
        return LocalDateTime.parse(div.getVisibleText(), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    private void trimList() {
        if (games.size() > MAX_GAMES_IN_MEMORY) {
            games.remove(0);
            log.info("Removed 0 element from games");
        }
    }

    private void checkGames() {
        if (games.size() > 3) {
            Game lastGame = games.get(games.size() - 1);
            int[] lastGameNums = lastGame.numbers;
            int[] secondGameNums = games.get(games.size() - 2).numbers;
            int[] thirdGameNums = games.get(games.size() - 3).numbers;

            for (int lastGameNum : lastGameNums) {
                boolean repeat2 = false;
                for (int secondGameNum : secondGameNums) {
                    if (secondGameNum == lastGameNum) {
                        repeat2 = true;
                        break;
                    }
                }
                boolean repeat3 = false;
                for (int thirdGameNum : thirdGameNums) {
                    if (thirdGameNum == lastGameNum) {
                        repeat3 = true;
                        break;
                    }
                }
                if (repeat2 && repeat3) {
                    bets.add(new Bet(lastGame.gameNumber + 1, lastGameNum));
                    log.info("New bet added");
                }
            }
        }
    }

    private void checkBets(StatisticsService statisticsService) {
        Game newGame = games.get(games.size() - 1);

        for (Bet bet : bets) {
            boolean loose = false;
            for (int number : newGame.numbers) {
                if (number == bet.targetNum) {
                    loose = true;
                    break;
                }
            }

            Statistics stats = statisticsService.getStatistics();
            stats.setBetsCount(stats.getBetsCount() + 1);
            if (loose) {
                stats.setLooses(stats.getLooses() + 1);
                stats.setBankStatus(stats.getBankStatus() - 1000);
            } else {
                stats.setBankStatus(stats.getBankStatus() + BANK_DIFFER);
            }
            statisticsService.save(stats);
            log.info("Statistics refreshed");
        }

        if (!bets.isEmpty()) {
            msgSender.sendStats();
            bets.clear();
        }
    }

    private void sendNewBets() {
        for (Bet bet : bets) {
            msgSender.send("В тираже " + bet.targetGameNumber + " не выпадет цифра " + bet.targetNum +
                    ", кф 1." + BANK_DIFFER);
            log.info("New bet sent");
        }
    }

    private long calcWaitingTime() {
        LocalDateTime lastGameTime = games.get(games.size() - 1).dateTime;
        LocalDateTime targetDateTime = lastGameTime.plusMinutes(WAITING_STEP_MINUTES);
        Duration dur = Duration.between(LocalDateTime.now(), targetDateTime);
        return Math.abs(dur.toMillis());
    }

    private void checkNewGameAdding() throws InterruptedException {
        if (newGameAdded) {
            performNewGameAddedActions();
        } else {
            performNewGameNotAddedActions();
        }
    }

    private void performNewGameAddedActions() throws InterruptedException {
        trimList();
        checkBets(statisticsService);
        checkGames();
        sendNewBets();
        long waitingTime = calcWaitingTime();
        log.info("Sleep " + waitingTime/60_000 + " mins");
        sleep(waitingTime);
    }

    private void performNewGameNotAddedActions() throws InterruptedException {
        log.info("Sleep 5 mins");
        sleep(5 * 60_000);
    }
}

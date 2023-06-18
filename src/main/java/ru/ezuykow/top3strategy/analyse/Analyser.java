package ru.ezuykow.top3strategy.analyse;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.*;

/**
 * @author ezuykow
 */
@Log
public class Analyser {

    public static final String ARCHIVE_URL = "file:///C:/Users/zuy19/OneDrive/%D0%A0%D0%B0%D0%B1%D0%BE%D1%87%D0%B8%D0%B9%20%D1%81%D1%82%D0%BE%D0%BB/archive.html";
    public static final String ARCHIVE_TABLE_XPATH = "/html/body/div[1]/div/div[1]/main/table";

    private String analyseResult;
    private final Map<Integer, Integer> digitsInARowCount = new HashMap<>();

    private Analyser() {
        makeAnalyseResult();
    }

    //-----------------API START-----------------

    public static String performAnalyse() {
        return new Analyser().analyseResult;
    }

    //-----------------API END-----------------

    private void makeAnalyseResult() {
        try (WebClient client = new WebClient(BrowserVersion.FIREFOX)) {
            client.setCssErrorHandler(new SilentCssErrorHandler());
            client.getOptions().setJavaScriptEnabled(false);
            client.getOptions().setDownloadImages(false);
            HtmlPage page = client.getPage(ARCHIVE_URL);
            HtmlTable archiveTable = page.getFirstByXPath(ARCHIVE_TABLE_XPATH);
            List<HtmlTableRow> rows = archiveTable.getRows();
            analyseResult = "Target games count: " + rows.size() + "\n" +
            "from " + rows.get(0).getCells().get(0).asNormalizedText() +
            " to " + rows.get(rows.size() - 1).getCells().get(0).asNormalizedText() + "\n";
            analyse(rows);
        } catch (IOException e) {
            analyseResult = "Error";
        }
    }

    private void analyse(List<HtmlTableRow> rows) {
        Map<Integer, Integer> digitsStorage = new HashMap<>();
        for (HtmlTableRow row : rows) {
            List<HtmlTableCell> cells = row.getCells();
            Integer[] digits = new Integer[3];
            digits[0] = Integer.parseInt(cells.get(1).asNormalizedText());
            digits[1] = Integer.parseInt(cells.get(2).asNormalizedText());
            digits[2] = Integer.parseInt(cells.get(3).asNormalizedText());

            checkDigitsInRow(digitsStorage, new HashSet<>(List.of(digits)));
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : digitsInARowCount.entrySet()) {
            if (e.getKey() > 2) {
                sb.append("Digit ").append(e.getKey()).append(" times in a row: ").append(e.getValue()).append("\n");
            }
        }
        analyseResult += sb.toString();
    }

    private void checkDigitsInRow(Map<Integer, Integer> digitsStorage, Set<Integer> gameDigits) {
        List<Integer> digitsToRemove = new ArrayList<>();
        for (Integer digit : digitsStorage.keySet()) {
            if (!gameDigits.contains(digit)) {
                Integer count = digitsStorage.get(digit);
                if (digitsInARowCount.containsKey(count)) {
                    digitsInARowCount.put(count, digitsInARowCount.get(count) + 1);
                } else {
                    digitsInARowCount.put(count, 1);
                }
                digitsToRemove.add(digit);
            }
        }
        for (Integer i : digitsToRemove) {
            digitsStorage.remove(i);
        }

        for (Integer gameDigit : gameDigits) {
            if (digitsStorage.containsKey(gameDigit)) {
                digitsStorage.put(gameDigit, digitsStorage.get(gameDigit) + 1);
            } else {
                digitsStorage.put(gameDigit, 1);
            }
        }
    }
}

package knu.invigoworksknu.service;

import knu.invigoworksknu.common.exception.InvigoWorksException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static knu.invigoworksknu.common.exception.errors.CrawlingError.CRAWLING_IO_ERROR;
import static knu.invigoworksknu.common.exception.errors.CrawlingError.INVALID_DATETIME_FORMAT_ERROR;
import static knu.invigoworksknu.util.DateTimeFormatterPatterns.COIN_READERS_DASH_PATTERN;
import static knu.invigoworksknu.util.DateTimeFormatterPatterns.COIN_READERS_DOT_PATTERN;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingService {

    public static final int TARGET_HOURS = 1;

    public List<String> getCoinReadersBreakingNews(LocalDateTime now) {
        // 타겟 시간
        LocalDateTime endLocalDateTime = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime startLocalDateTime = endLocalDateTime.minusHours(TARGET_HOURS);

        // 크롤링
        List<String> ids = new ArrayList<>();

        String baseUrl = "https://coinreaders.com/sub.html?section=sc16";

        String dateTimeSeparator = "-";
        int pageNum = 1;
        try {
            while (true) {
                StringBuilder url = new StringBuilder(baseUrl);
                url.append("&page=")
                        .append(pageNum);

                Document doc = Jsoup.connect(url.toString()).get();
                Elements elements = doc.select(".sub_read_list_box");

                List<String> parsedIds = elements.stream()
                        .filter(element -> {
                            String localDateTimeString = element.selectFirst(".etc").text();
                            LocalDateTime localDateTime = parseLocalDateTime(localDateTimeString, dateTimeSeparator);
                            return isBetweenStartAndEndLocalDateTime(localDateTime, startLocalDateTime, endLocalDateTime);
                        })
                        .map(element -> {
                            Element idElement = element.selectFirst("dl dt a[href]");
                            String id = idElement.attr("href");
                            return id;
                        })
                        .toList();
                ids.addAll(parsedIds);

                String lastElementLocalDateTimeString = elements.get(elements.size() - 1).selectFirst(".etc").text();

                LocalDateTime targetLocalDateTime = parseLocalDateTime(lastElementLocalDateTimeString, dateTimeSeparator);
                boolean betweenStartAndEndLocalDateTime = isBetweenStartAndEndLocalDateTime(targetLocalDateTime, startLocalDateTime, endLocalDateTime);
                if (!betweenStartAndEndLocalDateTime) {
                    break;
                }
                pageNum += 1;
            }
        } catch (IOException e) {
            log.error("CoinReadersBreakingNewsError", e);
            throw new InvigoWorksException(CRAWLING_IO_ERROR);
        }

        return ids;
    }

    public List<String> getCoinReadersEthereumNews(LocalDateTime now) {
        // 타겟 시간
        LocalDateTime endLocalDateTime = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime startLocalDateTime = endLocalDateTime.minusHours(TARGET_HOURS);

        String targetDate = startLocalDateTime.toString().split("T")[0].replace("-", "");

        // 크롤링
        List<String> ids = new ArrayList<>();

        String baseUrl = "https://coinreaders.com/search.html?submit=submit&search=%EC%9D%B4%EB%8D%94%EB%A6%AC%EC%9B%80&search_exec=all&news_order=1&search_section=all&search_and=1";

        String dateTimeSeparator = ".";
        int pageNum = 1;
        try {
            while (true) {
                StringBuilder url = new StringBuilder(baseUrl);
                url.append("&search_start_day=")
                        .append(targetDate)
                        .append("&search_end_day=")
                        .append(targetDate)
                        .append("&page=")
                        .append(pageNum);

                Document doc = Jsoup.connect(url.toString()).get();
                Elements elements = doc.select(".search_result_list_box");

                List<String> parsedIds = elements.stream()
                        .filter(element -> {
                            String localDateTimeString = element.selectFirst(".etc").text();
                            String[] split = localDateTimeString.split(" \\| ")[1].split(" ");
                            localDateTimeString = split[0] + " " + split[1];

                            LocalDateTime localDateTime = parseLocalDateTime(localDateTimeString, dateTimeSeparator);
                            return isBetweenStartAndEndLocalDateTime(localDateTime, startLocalDateTime, endLocalDateTime);
                        })
                        .map(element -> {
                            Element idElement = element.selectFirst("dl dt a[href]");
                            String id = idElement.attr("href");
                            return id;
                        })
                        .toList();
                ids.addAll(parsedIds);

                String lastElementLocalDateTimeString = elements.get(elements.size() - 1).selectFirst(".etc").text();
                String[] split = lastElementLocalDateTimeString.split(" \\| ")[1].split(" ");
                lastElementLocalDateTimeString = split[0] + " " + split[1];

                LocalDateTime targetLocalDateTime = parseLocalDateTime(lastElementLocalDateTimeString, dateTimeSeparator);
                boolean betweenStartAndEndLocalDateTime = isBetweenStartAndEndLocalDateTime(targetLocalDateTime, startLocalDateTime, endLocalDateTime);
                if (!betweenStartAndEndLocalDateTime) {
                    break;
                }
                pageNum += 1;
            }
        } catch (IOException e) {
            log.error("CoinReadersEthereumNewsError", e);
            throw new InvigoWorksException(CRAWLING_IO_ERROR);
        }

        return ids;
    }

    public String getCoinReadersBreakingNewsById(String id) {
        try {
            String baseUrl = "https://coinreaders.com";

            StringBuilder url = new StringBuilder();
            url.append(baseUrl)
                    .append(id);

            Document doc = Jsoup.connect(url.toString()).get();
            Element element = doc.selectFirst("#textinput");
            return element.text();
        } catch (IOException e) {
            log.error("getCoinReadersBreakingNewsByIdError", e);
            throw new InvigoWorksException(CRAWLING_IO_ERROR);
        }
    }

    public String getCoinReadersEthereumNewsById(String id) {
        try {
            String baseUrl = "https://coinreaders.com";

            StringBuilder url = new StringBuilder();
            url.append(baseUrl)
                    .append(id);

            Document doc = Jsoup.connect(url.toString()).get();
            Elements elements = doc.select("#textinput p");
            String collect = elements.stream()
                    .filter(element -> !element.toString().equals("<p>&nbsp;</p>"))
                    .map(Element::text)
                    .collect(Collectors.joining());
            return collect;
        } catch (IOException e) {
            log.error("getCoinReadersEthereumNewsByIdError", e);
            throw new InvigoWorksException(CRAWLING_IO_ERROR);
        }
    }

    private LocalDateTime parseLocalDateTime(String datetime, String separator) {
        String pattern = getPattern(separator);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime targetLocalDateTime = LocalDateTime.parse(datetime, formatter);
        return targetLocalDateTime;
    }

    private String getPattern(String separator) {
        switch (separator) {
            case ".":
                return COIN_READERS_DOT_PATTERN;
            case "-":
                return COIN_READERS_DASH_PATTERN;
            default:
                throw new InvigoWorksException(INVALID_DATETIME_FORMAT_ERROR);
        }
    }

    private boolean isBetweenStartAndEndLocalDateTime(LocalDateTime targetLocalDateTime, LocalDateTime startLocalDateTime, LocalDateTime endLocalDateTime) {
        return (targetLocalDateTime.isAfter(startLocalDateTime) || targetLocalDateTime.isEqual(startLocalDateTime)) && targetLocalDateTime.isBefore(endLocalDateTime);
    }
}

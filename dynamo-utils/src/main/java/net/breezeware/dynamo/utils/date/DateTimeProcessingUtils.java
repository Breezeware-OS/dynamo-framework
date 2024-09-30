package net.breezeware.dynamo.utils.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility for Date and time processing.
 */
@Slf4j
public class DateTimeProcessingUtils {

    /**
     * Parses {@link LocalDateTime} from String based on the formatter.
     * @param  data                     date string to be parsed as
     *                                  {@link LocalDateTime}.
     * @param  dateTimeFormatterPattern formatter for parsing the date and time
     *                                  string.
     * @return                          {@link LocalDateTime}
     */
    public static LocalDateTime parseLocalDateTime(String data, String dateTimeFormatterPattern) {
        log.debug("Entering parseLocalDateTime(), data = {}, dateTimeFormatterPattern = {}", data,
                dateTimeFormatterPattern);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormatterPattern);
        LocalDateTime localDateTime = LocalDateTime.parse(data, formatter);

        log.debug("Leaving parseLocalDateTime(), parsed localDateTime = {}", localDateTime);
        return localDateTime;
    }

    /**
     * Parses {@link LocalDate} from String based on the formatter.
     * @param  data                 date string to be parsed as {@link LocalDate}.
     * @param  dateFormatterPattern formatter for parsing the date string.
     * @return                      {@link LocalDate}
     */
    public static LocalDate parseLocalDate(String data, String dateFormatterPattern) {
        log.debug("Entering parseLocalDate(), data = {}, dateFormatterPattern = {}", data, dateFormatterPattern);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatterPattern);
        LocalDate localDate = LocalDate.parse(data, formatter);

        log.debug("Leaving parseLocalDate(), parsed localDate = {}", localDate);
        return localDate;
    }
}

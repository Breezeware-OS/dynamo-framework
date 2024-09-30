package net.breezeware.dynamo.utils.string;

import java.util.Objects;

import org.apache.commons.text.WordUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility for String processing.
 */
@Slf4j
public class StringProcessingUtils {

    /**
     * Cleans and Capitalizes each word.
     * @param  data data to be cleaned and capitalized
     * @return      Data space stripped and filly capitalized String.
     */
    public static String cleanAndCapitalizeEachWord(String data) {
        log.debug("Entering cleanAndCapitalizeEachWord(), data = {}", data);

        if (Objects.nonNull(data)) {
            log.debug("Data is not null. Cleaning and capitalizing data");
            // removes leading and trailing spaces
            data = data.strip();
            // capitalizes all the whitespace separated words in the 'data' into capitalized
            // words
            String cleanedAndCapitalizedString = WordUtils.capitalizeFully(data);
            log.debug("Leaving cleanAndCapitalizeEachWord(), cleanedAndCapitalizedString = {}",
                    cleanedAndCapitalizedString);
            return cleanedAndCapitalizedString;
        } else {
            log.error("Null data found. Throwing IllegalArgumentException");
            log.debug("Leaving cleanAndCapitalizeEachWord()");
            throw new IllegalArgumentException("Cannot clean and capitalize 'null' data");
        }

    }
}

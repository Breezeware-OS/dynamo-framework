package net.breezeware.dynamo.utils.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility for Regex processing.
 */
@Slf4j
public class RegexUtils {

    /**
     * Matches data exactly with the pattern. <b>NOTE:</b> Pattern is
     * case-insensitive by default.
     * @param  pattern Pattern to which the data will be matched against.
     * @param  data    Data to be matched.
     * @return         <code>true</code> if data matches exactly with the pattern,
     *                 else <code>false</code>.
     */
    public static boolean matchesExact(String pattern, String data) {
        log.debug("Entering matchesExact(), pattern = {}, data = {}", pattern, data);

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(data);
        boolean matches = matcher.matches();
        log.debug("Matched = {}", matches);

        log.debug("Leaving matchesExact()");
        return matches;
    }

    /**
     * Matches data partially with the pattern.<br>
     * <b>NOTE:</b> Pattern is case-insensitive by default.
     * @param  pattern Pattern to which the data will be matched against.
     * @param  data    Data to be matched.
     * @return         <code>true</code> if data matches partially with the pattern,
     *                 else <code>false</code>.
     */
    public static boolean matchesPartial(String pattern, String data) {
        log.debug("Entering matchesPartial(), pattern = {}, data = {}", pattern, data);

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(data);
        boolean matches = matcher.find();
        log.debug("Matched = {}", matches);

        log.debug("Leaving matchesPartial()");
        return matches;
    }
}

package net.breezeware.dynamo.csv.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * CSV file configuration.<br>
 * Includes fields to customize CSV file properties like columns, delimiter,
 * escape character, line end, etc.
 */
@Builder
@Data
public class CsvFileConfig {

    /**
     * Default delimiter or separator constant with value ','.
     */
    private static final Character DEFAULT_DELIMITER_OR_SEPARATOR = ',';
    /**
     * Default escape character constant with value ','.
     */
    private static final Character DEFAULT_ESCAPE_CHARACTER = '"';
    /**
     * Default quote character constant with value '"'.
     */
    private static final Character DEFAULT_QUOTE_CHARACTER = '"';
    /**
     * Default suffix or line end constant with value "\n".
     */
    private static final String DEFAULT_SUFFIX_OR_LINE_END = "\n";

    /**
     * Is record values delimited. Default value is {@link Boolean#TRUE}.
     */
    @Default
    private boolean delimited = Boolean.TRUE;

    /**
     * Delimiter or separator between the record values in the CSV file. Default
     * value is {@link CsvFileConfig#DEFAULT_DELIMITER_OR_SEPARATOR}.
     */
    @Default
    private char delimiterOrSeparator = DEFAULT_DELIMITER_OR_SEPARATOR;

    /**
     * CSV file path.
     */
    private String filePath;

    /**
     * Is CSV file contains header. Default value is {@link Boolean#TRUE}.
     */
    @Default
    private boolean containsColumnsOrHeader = Boolean.TRUE;

    /**
     * CSV file Columns or header.
     */
    private String[] columnsOrHeader;

    /**
     * Suffix or Line end for the records in the CSV. Default value is
     * {@link CsvFileConfig#DEFAULT_SUFFIX_OR_LINE_END}.
     */
    @Default
    private String suffixOrLineEnd = DEFAULT_SUFFIX_OR_LINE_END;

    /**
     * Escape character for records in the CSV. Default value is
     * {@link CsvFileConfig#DEFAULT_ESCAPE_CHARACTER}.
     */
    @Default
    private char escapeCharacter = DEFAULT_ESCAPE_CHARACTER;

    /**
     * Quote character for records in the CSV. Default value is
     * {@link CsvFileConfig#DEFAULT_QUOTE_CHARACTER}.
     */
    @Default
    private char quoteCharacter = DEFAULT_QUOTE_CHARACTER;

    /**
     * Getter method for {@link CsvFileConfig#containsColumnsOrHeader} field.
     * @return <code>true</code> if the CSV file contains <b>columns or header</b>,
     *         else <code>false</code>
     */
    public boolean containsColumnsOrHeader() {
        return this.containsColumnsOrHeader;
    }
}

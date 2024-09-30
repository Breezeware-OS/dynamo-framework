package net.breezeware.dynamo.csv.service.api;

import java.nio.file.Path;
import java.util.List;

import com.opencsv.bean.StatefulBeanToCsv;

import net.breezeware.dynamo.csv.config.CsvFileConfig;
import net.breezeware.dynamo.csv.exception.CsvReadException;
import net.breezeware.dynamo.csv.exception.CsvWriteException;

/**
 * Service for CSV file operations using OpenCSV.
 */
public interface CsvService {

    /**
     * Writes a single object to CSV file using {@link StatefulBeanToCsv} with
     * customized configuration from {@link CsvFileConfig}.
     * @param  beanClass         {@link Class} of the object.
     * @param  csvFileConfig     Configuration class for customizing
     *                           {@link StatefulBeanToCsv} parameters like columns,
     *                           delimiter, suffix or line end, escape character,
     *                           quote character, etc.
     * @param  obj               object to be written as CSV.
     * @param  <T>               Type of object that is to be converted to CSV.
     * @throws CsvWriteException in case of error while writing object to CSV.
     */
    <T> void writeToFile(Class<T> beanClass, CsvFileConfig csvFileConfig, T obj) throws CsvWriteException;

    /**
     * Writes a collection of object to CSV file using {@link StatefulBeanToCsv}
     * with customized configuration from {@link CsvFileConfig}.
     * @param  beanClass         {@link Class} of the object.
     * @param  csvFileConfig     Configuration class for customizing
     *                           {@link StatefulBeanToCsv} parameters like columns,
     *                           delimiter, suffix or line end, escape character,
     *                           quote character, etc.
     * @param  objs              Collection of object to be written to CSV.
     * @param  <T>               Type of object that is to be converted to CSV.
     * @throws CsvWriteException in case of error while writing object to CSV.
     */
    <T> void writeToFile(Class<T> beanClass, CsvFileConfig csvFileConfig, List<T> objs) throws CsvWriteException;

    /**
     * Writes single line to the CSV file.
     * @param  line              Line to write.
     * @param  filePath          CSV file path.
     * @throws CsvWriteException in case of error while writing the line to CSV.
     */
    void writeToFile(String[] line, Path filePath) throws CsvWriteException;

    /**
     * Writes multiple lines to the CSV file.
     * @param  lines             Lines to write.
     * @param  filePath          CSV file path.
     * @throws CsvWriteException in case of error while writing the lines to CSV.
     */
    void writeToFile(List<String[]> lines, Path filePath) throws CsvWriteException;

    <T> List<T> read(CsvFileConfig csvFileConfig, byte[] bytesArray, Class<T> type) throws CsvReadException;
}
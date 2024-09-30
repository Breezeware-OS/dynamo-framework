package net.breezeware.dynamo.csv.service.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import net.breezeware.dynamo.csv.config.CsvFileConfig;
import net.breezeware.dynamo.csv.config.CustomMappingStrategy;
import net.breezeware.dynamo.csv.exception.CsvReadException;
import net.breezeware.dynamo.csv.exception.CsvWriteException;
import net.breezeware.dynamo.csv.exception.DynamoCsvException;
import net.breezeware.dynamo.csv.service.api.CsvService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link CsvService}.
 */
@Slf4j
@Service
public class CsvServiceImpl implements CsvService {

    @Override
    public <T> void writeToFile(Class<T> beanClass, CsvFileConfig csvFileConfig, T obj) throws CsvWriteException {
        log.debug("Entering writeToFile(), beanClass = {}, csvFileConfig = {}, obj = {}", beanClass, csvFileConfig,
                obj);
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(csvFileConfig.getFilePath()))) {
            StatefulBeanToCsv<T> statefulBeanToCsv = buildStatefulBeanToCsv(csvFileConfig, beanClass, writer);
            statefulBeanToCsv.write(obj);
            log.debug("Written object = {} to CSV file = {}", obj.toString(), csvFileConfig.getFilePath());
            log.debug("Leaving writeToFile()");
        } catch (CsvRequiredFieldEmptyException e) {
            log.error("Required field empty, while creating StatefulBeanToCsv, error = {}", e.getMessage());
            throw new CsvWriteException(e.getMessage());
        } catch (CsvDataTypeMismatchException e) {
            log.error("Data type mismatched while creating StatefulBeanToCsv, error = {}", e.getMessage());
            throw new CsvWriteException(e.getMessage());
        } catch (IOException e) {
            log.error("Error while creating StatefulBeanToCsv, error = {}", e.getMessage());
            throw new CsvWriteException(e.getMessage());
        }

    }

    /**
     * Builds {@link StatefulBeanToCsv}.<br>
     * Configured to use {@link CustomMappingStrategy} as mapping strategy if
     * {@link CsvFileConfig#containsColumnsOrHeader} is
     * <code>{@link Boolean#TRUE}</code> and {@link CsvFileConfig#columnsOrHeader}
     * is not empty else uses {@link ColumnPositionMappingStrategy} as default
     * mapping strategy.
     * @param  csvFileConfig CSV file configuration.
     * @param  type          target type of the {@link StatefulBeanToCsv}.
     * @param  writer        {@link Writer} to store as a buffer.
     * @param  <T>           target type parameter of the {@link StatefulBeanToCsv}.
     * @return               {@link StatefulBeanToCsv}.
     */
    private <T> StatefulBeanToCsv<T> buildStatefulBeanToCsv(CsvFileConfig csvFileConfig, Class<T> type, Writer writer) {
        log.debug("Entering buildStatefulBeanToCsv(), csvFileConfig = {}, type = {}, writer = {}", csvFileConfig, type,
                writer);
        StatefulBeanToCsvBuilder<T> statefulBeanToCsvBuilder = new StatefulBeanToCsvBuilder<>(writer);

        // record delimiter configuration
        if (csvFileConfig.isDelimited()) {
            log.debug("Records are delimited");
            char delimiter = csvFileConfig.getDelimiterOrSeparator();
            statefulBeanToCsvBuilder.withSeparator(delimiter);
            log.debug("Configured statefulBeanToCsvBuilder's separator with = {}", delimiter);
        }

        // record suffix configuration
        String suffixOrLineEnd = csvFileConfig.getSuffixOrLineEnd();
        if (Objects.nonNull(suffixOrLineEnd) && !suffixOrLineEnd.isBlank()) {
            log.debug("Record suffix present, suffixOrLineEnd = {}", suffixOrLineEnd);
            statefulBeanToCsvBuilder.withLineEnd(suffixOrLineEnd);
            log.debug("Configured StatefulBeanToCsvBuilder with line end = {}", suffixOrLineEnd);
        }

        // record escape character configuration
        char escapeCharacter = csvFileConfig.getEscapeCharacter();
        statefulBeanToCsvBuilder.withEscapechar(escapeCharacter);
        if (csvFileConfig.containsColumnsOrHeader() && !Arrays.asList(csvFileConfig.getColumnsOrHeader()).isEmpty()) {
            log.debug("Contains column header. Configuring custom mapping strategy");
            CustomMappingStrategy<T> customMappingStrategy = new CustomMappingStrategy<>();
            customMappingStrategy.setType(type);
            customMappingStrategy.setColumnMapping(csvFileConfig.getColumnsOrHeader());
            statefulBeanToCsvBuilder.withMappingStrategy(customMappingStrategy);
            log.debug("Configured StatefulBeanToCsvBuilder with custom mapping strategy = {}", customMappingStrategy);
        }

        StatefulBeanToCsv<T> statefulBeanToCsv = statefulBeanToCsvBuilder.build();
        log.debug("Leaving buildStatefulBeanToCsv(), buildStatefulBeanToCsv = {}", statefulBeanToCsv);
        return statefulBeanToCsv;
    }

    @Override
    public <T> void writeToFile(Class<T> beanClass, CsvFileConfig csvFileConfig, List<T> objects)
            throws CsvWriteException {
        log.debug("Entering writeToFile(), beanClass = {}, csvFileConfig = {}, objects = {}", beanClass, csvFileConfig,
                objects);
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(csvFileConfig.getFilePath()))) {
            StatefulBeanToCsv<T> statefulBeanToCsv = buildStatefulBeanToCsv(csvFileConfig, beanClass, writer);
            statefulBeanToCsv.write(objects);
            log.debug("Written # of objects = {} to CSV file = {}", objects.size(), csvFileConfig.getFilePath());
            log.debug("Leaving writeToFile()");
        } catch (CsvRequiredFieldEmptyException e) {
            log.error("Required field empty, while creating StatefulBeanToCsv, error = {}", e.getMessage());
            throw new CsvWriteException(e.getMessage());
        } catch (CsvDataTypeMismatchException e) {
            log.error("Data type mismatched while creating StatefulBeanToCsv, error = {}", e.getMessage());
            throw new CsvWriteException(e.getMessage());
        } catch (IOException e) {
            log.error("Error while creating StatefulBeanToCsv, error = {}", e.getMessage());
            throw new CsvWriteException(e.getMessage());
        }

    }

    @Override
    public void writeToFile(String[] line, Path filePath) throws CsvWriteException {
        log.debug("Entering writeToFile(), line = {}, filePath = {}", line, filePath);
        try (CSVWriter csvWriter = buildDefaultOpenCsvCsvWriter(filePath.toString())) {
            csvWriter.writeNext(line, Boolean.TRUE);
            log.debug("Written line = {} to file = {}", line, filePath);
            log.debug("Leaving writeToFile()");
        } catch (IOException e) {
            log.error("IO exception occurred, error = {}", e.getMessage());
            throw new CsvWriteException(e.getMessage());
        } catch (DynamoCsvException e) {
            log.error("Error while writing line = {} to file path = {}, error = {}", line, filePath, e.getMessage());
            throw new CsvWriteException(e.getMessage());
        }

    }

    /**
     * Builds a <b>default</b> OpenCSV {@link CSVWriter}.
     * @param  filePath           file path for which the writer to be created.
     * @return                    {@link CSVWriter} with default configuration.
     * @throws DynamoCsvException in case of error while creating default
     *                            {@link CSVWriter}.
     */
    private CSVWriter buildDefaultOpenCsvCsvWriter(String filePath) throws DynamoCsvException {
        log.debug("Entering buildDefaultOpenCsvCsvWriter(), filePath = {}", filePath);
        try (FileWriter writer = new FileWriter(filePath)) {
            CSVWriter csvWriter = new CSVWriter(writer);
            log.debug("Leaving buildDefaultOpenCsvCsvWriter(), csvWriter = {}", csvWriter);
            return csvWriter;
        } catch (IOException e) {
            log.error("Error while creating default CSVWriter using filePath = {}, error = {}", filePath,
                    e.getMessage());
            throw new DynamoCsvException(e.getMessage());
        }

    }

    @Override
    public void writeToFile(List<String[]> lines, Path filePath) throws CsvWriteException {
        log.debug("Entering writeToFile(), line = {}, filePath = {}", lines, filePath);
        try (FileWriter writer = new FileWriter(filePath.toString())) {
            CSVWriter csvWriter = new CSVWriter(writer);
            csvWriter.writeAll(lines, Boolean.TRUE);
            log.debug("Written # of lines = {} to file = {}", lines.size(), filePath);
            log.debug("Leaving writeToFile()");
        } catch (IOException e) {
            log.error("Error while writing # of lines = {} to file path = {}, error = {}", lines.size(), filePath,
                    e.getMessage());
            throw new CsvWriteException(e.getMessage());
        }

    }

    public <T> List<T> read(CsvFileConfig csvFileConfig, byte[] bytes, Class<T> type) throws CsvReadException {
        log.debug("Entering read()");

        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes))) {

            CSVReader csvReader = new CSVReader(reader);
            CustomMappingStrategy<T> customMappingStrategy = new CustomMappingStrategy<>();
            customMappingStrategy.setType(type);
            customMappingStrategy.setColumnMapping(csvFileConfig.getColumnsOrHeader());
            CsvToBean<T> csvToBean =
                    new CsvToBeanBuilder<T>(csvReader).withMappingStrategy(customMappingStrategy).build();
            List<T> parsedData = csvToBean.parse();
            log.info("# of data parsed from the csv: {}", parsedData.size());

            csvReader.close();

            return parsedData;

        } catch (IOException e) {
            throw new CsvReadException(e.getMessage());
        }

    }

    /**
     * Builds an OpenCSV {@link CSVWriter} with customizations using
     * {@link CsvFileConfig}.
     * @param  csvFileConfig      Customization configuration for {@link CSVWriter}.
     * @return                    {@link CSVWriter} based on configuration from the
     *                            {@link CsvFileConfig}.
     * @throws DynamoCsvException in case of error while creating {@link CSVWriter}.
     */
    private CSVWriter buildOpenCsvCsvWriter(CsvFileConfig csvFileConfig) throws DynamoCsvException {
        log.debug("Entering buildOpenCsvCsvWriter(), csvFileConfig = {}", csvFileConfig);
        try (FileWriter writer = new FileWriter(csvFileConfig.getFilePath())) {
            CSVWriter csvWriter =
                    new CSVWriter(writer,
                            csvFileConfig.isDelimited() ? csvFileConfig.getDelimiterOrSeparator()
                                    : CSVWriter.DEFAULT_SEPARATOR,
                            csvFileConfig.getQuoteCharacter(), csvFileConfig.getEscapeCharacter(),
                            csvFileConfig.getSuffixOrLineEnd());
            log.debug("Leaving buildOpenCsvCsvWriter(), csvWriter = {}", csvWriter);
            return csvWriter;
        } catch (IOException e) {
            log.error("Error while creating CSVWriter using csvFileConfig = {}, error = {}", csvFileConfig,
                    e.getMessage());
            throw new DynamoCsvException(e.getMessage());
        }

    }
}

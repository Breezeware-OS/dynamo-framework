package net.breezeware.dynamo.batch.service;

import java.util.Objects;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.SuffixRecordSeparatorPolicy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.csv.config.CsvFileConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Batch job step's {@link org.springframework.batch.item.ItemReader} builder
 * service.<br>
 * Configured to build {@link FlatFileItemReader}.
 */
@Slf4j
@Service
public class ItemReaderBuilder {

    /**
     * Builds a {@link FlatFileItemReader} to read the values from the CSV file.
     * @param  targetClass   Target class type for CSV to object conversion.
     * @param  csvFileConfig CSV file configuration.
     * @param  <T>           Type parameter of the {@link FlatFileItemReader}.
     * @return               {@link FlatFileItemReader}.
     */
    public <T> FlatFileItemReader<T> build(Class<T> targetClass, CsvFileConfig csvFileConfig) {
        log.debug("Entering build(), targetClass = {}, csvFileConfig = {}", targetClass, csvFileConfig);

        FlatFileItemReaderBuilder<T> flatFileItemReaderBuilder = new FlatFileItemReaderBuilder<>();

        flatFileItemReaderBuilder.name("flatFileItemReader");
        flatFileItemReaderBuilder.resource(new FileSystemResource(csvFileConfig.getFilePath()));
        flatFileItemReaderBuilder.targetType(targetClass);

        // Configures FlatFileItemReaderBuilder to skip line #1 in the CSV
        // if CsvFileConfig's containsColumnsOrHeader is 'true'.
        int linesToSkip = csvFileConfig.containsColumnsOrHeader() ? 1 : 0;
        flatFileItemReaderBuilder.linesToSkip(linesToSkip);
        log.debug("CSV contains column or header, Configuring linesToSkip = {}", linesToSkip);

        // reader suffix configuration
        String suffixOrLineEnd = csvFileConfig.getSuffixOrLineEnd();
        if (Objects.nonNull(suffixOrLineEnd) && !suffixOrLineEnd.isBlank()) {
            log.debug("Record suffixOrLineEnd present, suffixOrLineEnd = {}", suffixOrLineEnd);

            SuffixRecordSeparatorPolicy suffixRecordSeparatorPolicy = new SuffixRecordSeparatorPolicy();
            suffixRecordSeparatorPolicy.setSuffix(suffixOrLineEnd);
            flatFileItemReaderBuilder.recordSeparatorPolicy(suffixRecordSeparatorPolicy);

            log.debug("Configured FlatFileItemReader with SuffixRecordSeparatorPolicy = {}",
                    suffixRecordSeparatorPolicy);
        }

        // reader delimiter configuration
        if (csvFileConfig.isDelimited()) {
            log.debug("Records are delimited");

            FlatFileItemReaderBuilder.DelimitedBuilder<T> delimitedBuilder = flatFileItemReaderBuilder.delimited();
            // delimited record's column configuration
            delimitedBuilder.names(csvFileConfig.getColumnsOrHeader());
            log.debug("Configured FlatFileItemReaderBuilder's DelimitedBuilder with record's columns = {}",
                    delimitedBuilder);
            // record delimiter configuration
            char delimiter = csvFileConfig.getDelimiterOrSeparator();
            delimitedBuilder.delimiter(Character.toString(delimiter));
            log.debug("Configured FlatFileItemReaderBuilder's DelimitedBuilder with record delimiter = {}",
                    delimitedBuilder);

            log.debug("Configured FlatFileItemReader with DelimitedBuilder = {}", delimitedBuilder);
        }

        FlatFileItemReader<T> flatFileItemReader = flatFileItemReaderBuilder.build();
        log.debug("Leaving build(), flatFileItemReader = {}", flatFileItemReader);
        return flatFileItemReader;
    }
}

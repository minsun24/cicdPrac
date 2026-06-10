package com.lgcns.pipeline.memo;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CvsBatchConfig {

    private final MemoRepository memoRepository;


    @Bean
    public Job csvJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("csvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager, MemoRepository memoRepository) {
        return new StepBuilder("csvStep", jobRepository)
                .<Memo, Memo>chunk(5)
                .reader(memoReader()) // StepScope로 CSV 경로 받음
                .writer(memoWriter(memoRepository))
                .build();
    }

    @Bean
    @StepScope
    // memoReader(@Value("#{jobParameters['filePath']}") String filePath) {
    protected FlatFileItemReader<Memo> memoReader() {
        return new FlatFileItemReaderBuilder<Memo>()
                .name("memoReader")
                .resource(new ClassPathResource("memo.csv"))   // filePath
                .linesToSkip(1)
                .delimited()
                .names("memoText", "state")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(Memo.class);
                }}).build();
    }

    @Bean
    public RepositoryItemWriter<Memo> memoWriter(CrudRepository<Memo, Integer> repository) {
        return new RepositoryItemWriterBuilder<Memo>()
                .repository(repository)
                .methodName("save")
                .build();
    }

}

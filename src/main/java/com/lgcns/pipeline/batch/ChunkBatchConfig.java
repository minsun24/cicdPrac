package com.lgcns.pipeline.batch;

import com.lgcns.pipeline.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
public class ChunkBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;


    // JOB
    @Bean
    public Job chunkJob() {
        return new JobBuilder("chunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStep())
                .build();
    }


    // STEP
    @Bean
    public Step chunkStep() {
        return new StepBuilder("chunkStep", jobRepository)
                .<String, String>chunk(2)   // String 을 받아서 String 을 반환하겠다.
//                .reader(reader())
                .reader(reader(userRepository))
                .processor(processor())
                .writer(writer())
                .build();
    }

    // ITEM-READER
//    @Bean
//    public ItemReader<String> reader() {
//        return new ListItemReader<>(List.of("hong", "kim", "jeong", "lee"));
//    }

    /**
     * reader 로 List 를 읽어서
     * chunk() 사이즈로 설정한 크기만큼 String 값이 전달됨.
     */

    @Bean
    public RepositoryItemReader<String> reader(UserRepository userRepository) {
        return new RepositoryItemReaderBuilder<String>()
                .name("reader")
                .repository(userRepository)
                .methodName("findNames")
                .pageSize(2)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }


    // ITEM PROCESSOR
    @Bean
    public ItemProcessor<String, String> processor() {
        return StringUtils::capitalize; // 대문자로 만들기
    }

    // ITEM WRITER
    @Bean
    public ItemWriter<String> writer() {
        return items -> Stream.of(items).forEach(System.out::println);
    }

}

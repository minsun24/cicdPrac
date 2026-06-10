package com.lgcns.pipeline.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ProdJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private int sumCnt = 5;

    @Bean
    public Job prodJob() {
        return new JobBuilder("prodJob", jobRepository)
//                .incrementer(new RunIdIncrementer())  // 반복 실행 가능하도록 jobParameter 부여!
                .start(startProduction())
                .next(summary())
                .next(endProduction())
                .build();
    }

    /**
     * chunkContext : 필요한 Job에 대한 정보
     */
    @Bean
    public Step startProduction() {
        return new StepBuilder("startProduction", jobRepository)
                // contribution: 스텝의 진행정도, chunkContext: 작업 정보
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("제품 생산 시작!");
                    return RepeatStatus.FINISHED; // 반복 없음!
                }, transactionManager).build();
        // transactionManager 대신 new ResourcelessTransactionManager() 하면 Tx 없이 수행!!
    }

    @Bean
    public Step summary() {
        return new StepBuilder("summary", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    sumCnt++;
                    System.out.println("summary:" + sumCnt);
                    if (sumCnt >= 5) {
                        sumCnt = 0;
                        return RepeatStatus.FINISHED;
                    }
                    return RepeatStatus.CONTINUABLE;
                }, transactionManager).build();
    }


    @Bean
    public Step endProduction() {
        return new StepBuilder("startProduction", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("제품 생산 종료!");
                    return RepeatStatus.FINISHED; // 반복 없음!
                }, transactionManager).build();
    }


}

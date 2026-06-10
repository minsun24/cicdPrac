package com.lgcns.pipeline.userBatch;

import com.lgcns.pipeline.user.User;
import com.lgcns.pipeline.user.UserDTO;
import com.lgcns.pipeline.user.UserMapper;
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

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class getUserRoleJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Bean
    public Job getUserRoleJob() {
        return new JobBuilder("getUserRoleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(userConvertStep())
                .build();
    }

    @Bean
    public Step userConvertStep() {
        return new StepBuilder("userConvertStep", jobRepository)
                // <Input 타입: User 엔티티, Output 타입: UserDto>
                .<User, UserDTO>chunk(10)
                .reader(userReader(userRepository))
                .processor(userProcessor())
                .writer(userWriter())
                .transactionManager(transactionManager)
                .build();
    }


    // 1. Reader: User 엔티티 전체(이름과 Roles 포함)를 페이징하여 읽어옴
    @Bean
    public RepositoryItemReader<User> userReader(UserRepository userRepository) {
        return new RepositoryItemReaderBuilder<User>()
                .name("userReader")
                .repository(userRepository)
                .methodName("findAllWithRoles")
                .pageSize(10)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }


    // 2. Processor: User > UserDTO 변환
    @Bean
    public ItemProcessor<User, UserDTO> userProcessor() {
        return user -> {
            UserDTO dto = userMapper.toDTO(user);

            // 이름을 대문자로 바꾸는 기존 로직을 유지하면서 DTO 생성
            dto.setName(user.getName().toUpperCase());
            return dto;
        };
    }

    // 3. Writer: 변환된 UserDto 묶음을 처리
    @Bean
    public ItemWriter<UserDTO> userWriter() {
        return chunk -> {
            System.out.println("=== Writing Chunk ===");
            for (UserDTO dto : chunk) {
                System.out.printf("%s: %s%n", dto.getName(), dto.getRoleNames());
            }
        };
    }


}

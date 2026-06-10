package com.lgcns.pipeline.memo;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoService {
    private final JobOperator jobOperator;
    private final Job csvJob;
    private final MemoRepository repository;
    private final MemoMapper mapper;

    // public BatchStatus runBatch(String filePath) throws Exception {
    public BatchStatus runBatch() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                // .addString("filePath", filePath)
                .toJobParameters();   // 동일한 파라미터면 skip 되므로 매 번 다른 값을 전달함!

        return jobOperator.start(csvJob, jobParameters).getStatus();
    }

    @Scheduled(cron = "*/10 * * * * *")  // 매 0초, 30초  (*/10 * * … 이면 매 10초 마다!, 0 * * …는 매 분!)
    public void updateStateBatch() {
        MemoState state = MemoState.PAYED;
        System.out.println("bbb - MemoService.updateStateBatch");
        while (state != MemoState.DELIVERED) {
            LocalDateTime now = LocalDateTime.now();
            repository.updateStateBatch(state, state.getNextState(), now.minusSeconds(state.stateInterval()));

            state = state.getNextState();
        }
    }


    public List<MemoDTO> getAllMemo() {
        return mapper.toDTOList(repository.findAll());
    }
}

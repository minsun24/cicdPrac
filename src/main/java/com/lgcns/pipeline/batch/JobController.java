package com.lgcns.pipeline.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch/run")
@RequiredArgsConstructor
public class JobController {
    private final JobOperator jobOperator;
    private final ApplicationContext context;

    @PostMapping("{jobName}")
    public ResponseEntity<String> run(@PathVariable String jobName) {
        try {
            Job job = (Job) context.getBean(jobName);

            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobOperator.start(job, params);       // .run() 대신 .start()
            return ResponseEntity.ok(jobName + " started");
        } catch (NoSuchBeanDefinitionException e) {
            return ResponseEntity.badRequest().body("Job not found: " + jobName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

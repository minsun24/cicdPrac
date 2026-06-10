package com.lgcns.pipeline.memo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/memos")
public class MemoController {

    private final MemoService service;

    @PostMapping("/batch")
    public ResponseEntity<?> runMemoBatch() throws Exception {
        return ResponseEntity.ok("BatchStatus: " + service.runBatch());
    }

    @GetMapping()
    public ResponseEntity<?> getAll() throws Exception {
        return ResponseEntity.ok(service.getAllMemo());
    }

}

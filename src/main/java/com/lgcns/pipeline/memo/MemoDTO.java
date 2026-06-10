package com.lgcns.pipeline.memo;

import java.time.LocalDateTime;

public record MemoDTO(Integer mno, String memoText, MemoState state, LocalDateTime statedAt) {
}

package com.lgcns.pipeline.memo;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@DynamicInsert
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mno;

    @Column(nullable = false, length = 200)
    private String memoText;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PAYED'")
    private MemoState state;

    @Column(columnDefinition
            = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime statedAt;
}

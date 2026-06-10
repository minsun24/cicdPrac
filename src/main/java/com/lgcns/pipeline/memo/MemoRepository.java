package com.lgcns.pipeline.memo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface MemoRepository extends JpaRepository<Memo, Integer> {

    /**
     * @Modifying - JPA에서 이 쿼리가 데이터를 단순히 '조회(Select)'하는 게 아니라,
     * '수정(Update, Delete, Insert)'하는 작업임을 스프링 데이터 JPA에게 알려주는 어노테이션입니다.
     * - 이게 없으면 UPDATE 쿼리를 실행할 때 예외(InvalidDataAccessApiUsageException)가 발생합니다.
     */
    /**
     * @Transactional - 이 메서드가 실행되는 동안 하나의 '트랜잭션(작업 단위)'으로 묶이도록 보장합니다.
     * - 데이터 수정(Update) 작업은 도중에 에러가 나면 롤백(Rollback)되어야 하고,
     * 성공하면 데이터베이스에 커밋(Commit)되어야 하므로 반드시 필요합니다.
     * (보단 서비스 계층에서 붙이는 게 권장되지만, 레포지토리 단독 테스트나 배치용으로 여기에 붙여 쓰기도 합니다.)
     */
    /**
     * @Query - 기본적으로 제공되는 메서드(save, findById 등) 외에, 개발자가 직접 원하는 쿼리를 커스텀해서 쓰고 싶을 때 사용합니다.
     * - 여기서는 데이터베이스의 진짜 테이블이 아닌, JPA 엔티티 객체 객체 구조를 기반으로 하는 'JPQL' 문법을 사용했습니다.
     * - :nextState, :state, :timeToUp 부분은 아래 파라미터들이 매핑되는 변수(바인딩 파라미터)입니다.
     */
    @Modifying
    @Transactional
    @Query("update Memo m set m.state = :nextState, m.statedAt = now() where m.state = :state and m.statedAt <= :timeToUp")
    int updateStateBatch(@Param("state") MemoState state,
                         @Param("nextState") MemoState nextState,
                         @Param("timeToUp") LocalDateTime timeToUp);

}

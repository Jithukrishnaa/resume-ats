package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.ATSResult;

import java.util.List;

@Repository
public interface ATSResultRepository
        extends JpaRepository<ATSResult, Long> {

    List<ATSResult> findAllByOrderByRankPositionAsc();

}
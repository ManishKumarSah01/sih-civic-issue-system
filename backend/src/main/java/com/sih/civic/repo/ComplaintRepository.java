package com.sih.civic.repo;
import com.sih.civic.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ComplaintRepository extends JpaRepository<Complaint,Long> {
    List<Complaint> findAllByOrderByCreatedAtDesc();
    List<Complaint> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
    long countByStatus(String status);
}

package spring.tripmate.dao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import spring.tripmate.domain.Consumer;

public interface ConsumerDAO extends JpaRepository<Consumer, Long>{
	
    Consumer findByEmail(String email);
    
    Page<Consumer> findAll(PageRequest pageRequest);
}

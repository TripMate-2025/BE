package spring.tripmate.dao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import spring.tripmate.domain.Consumer;

public interface ConsumerDAO {
	
    Consumer findByEmail(String email);
    
    Page<Consumer> findAll(PageRequest pageRequest);
    
    void delete(Consumer consumer);
    
    void deleteById(Long consumerId);
}

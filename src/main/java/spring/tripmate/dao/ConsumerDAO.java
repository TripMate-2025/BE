package spring.tripmate.dao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import spring.tripmate.domain.Consumer;

public interface ConsumerDAO extends JpaRepository<Consumer, Long>{
	
    Consumer findByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    
    // JpaRepository가 제공해서 지워도됨
    //Page<Consumer> findAll(PageRequest pageRequest);
}

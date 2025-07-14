package radu.domain.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import radu.engine.Order;

@Repository
public interface SellOrderRepo  extends JpaRepository<Order, Long>{
}

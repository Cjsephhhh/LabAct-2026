package edu.cit.alvarado.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface TianggeOrderLinkRepository extends JpaRepository<TianggeOrderLink, Long> {
    Optional<TianggeOrderLink> findByTianggeOrderId(String tianggeOrderId);
    List<TianggeOrderLink> findByDecision(String decision);
}

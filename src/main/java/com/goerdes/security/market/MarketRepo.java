package com.goerdes.security.market;

import com.goerdes.security.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepo extends JpaRepository<MarketEntity, Integer> {
}

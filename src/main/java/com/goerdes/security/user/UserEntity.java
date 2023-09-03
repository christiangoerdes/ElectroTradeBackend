package com.goerdes.security.user;

import com.goerdes.security.market.MarketEntity;
import com.goerdes.security.market.TransactionEntity;
import com.goerdes.security.token.Token;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class UserEntity implements UserDetails {

  @Id
  @GeneratedValue
  private Integer id;
  private String name;
  private String email;
  private String password;
  private Double balance;

  @Enumerated(EnumType.STRING)
  private Role role;

  @OneToMany(mappedBy = "userEntity")
  private List<Token> tokens;

  @ElementCollection
  @CollectionTable(name = "user_market_mapping", joinColumns = @JoinColumn(name = "user_id"))
  @MapKeyJoinColumn(name = "market_id")
  @Column(name = "quantity")
  private Map<MarketEntity, Integer> stockQuantityMap = new HashMap<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<TransactionEntity> transactions = new ArrayList<>();

  public void addTransaction(TransactionEntity transaction) {
    transactions.add(transaction);
  }

  public void buyStock(MarketEntity market, int quantity) {
    stockQuantityMap.put(market, quantity);
  }

  public void sellMarket(MarketEntity stock, int quantity) {
    if (stockQuantityMap.containsKey(stock) && stockQuantityMap.get(stock) >= quantity) {
      double stockPrice = stock.getPriceHistory().get(stock.getPriceHistory().size() - 1);
      double saleValue = quantity * stockPrice;

      stockQuantityMap.put(stock, stockQuantityMap.get(stock) - quantity);

      TransactionEntity saleTransaction = TransactionEntity.builder()
              .user(this)
              .market(stock)
              .price(stockPrice)
              .quantity(quantity * -1)
              .timestamp(LocalDateTime.now())
              .build();
      transactions.add(saleTransaction);
      setBalance(getBalance() + saleValue);
      System.out.println("asdasdadsad");
    } else {
      throw new IllegalArgumentException("Not enough quantity to sell");
    }
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return role.getAuthorities();
  }

  @Override
  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

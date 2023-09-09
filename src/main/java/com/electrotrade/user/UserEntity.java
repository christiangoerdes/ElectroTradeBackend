package com.electrotrade.user;

import com.electrotrade.market.MarketEntity;
import com.electrotrade.market.TransactionEntity;
import com.electrotrade.token.Token;
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

  public void buyStock(MarketEntity stock, int quantity) {
    double stockPrice = stock.getPriceHistory().get(stock.getPriceHistory().size() - 1);
    if(quantity >= 1 && balance >= stockPrice*quantity) {
      transactions.add(
              TransactionEntity.builder()
                      .user(this)
                      .market(stock)
                      .price(stockPrice)
                      .quantity(quantity)
                      .timestamp(LocalDateTime.now())
                      .build()
      );
      if(stockQuantityMap.containsKey(stock)) {
        stockQuantityMap.put(stock, stockQuantityMap.get(stock) + quantity);
      } else {
        stockQuantityMap.put(stock, quantity);
      }
      balance -= stockPrice*quantity;
      balance = Math.round(balance * 100.0) / 100.0;
    } else {
    throw new IllegalArgumentException("Not enough quantity to sell");
    }
  }

  public void sellStock(MarketEntity stock, int quantity) {
    if (stockQuantityMap.containsKey(stock) && stockQuantityMap.get(stock) >= quantity) {
      double stockPrice = stock.getPriceHistory().get(stock.getPriceHistory().size() - 1);
      transactions.add(
              TransactionEntity.builder()
                      .user(this)
                      .market(stock)
                      .price(stockPrice)
                      .quantity(quantity * -1)
                      .timestamp(LocalDateTime.now())
                      .build()
      );
      stockQuantityMap.put(stock, stockQuantityMap.get(stock) - quantity);
      if(stockQuantityMap.get(stock) == 0) {
        stockQuantityMap.remove(stock);
      }
      balance += quantity * stockPrice;
      balance = Math.round(balance * 100.0) / 100.0;
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

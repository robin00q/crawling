package me.sjlee.crawling;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
public class RedisTests {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    AccountRepository accountRepository;

    @Test
    void redisTestByRedisTemplate() {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set("sjlee", "123");
        String sjlee = values.get("sjlee");
        System.out.println(sjlee);
    }

    @Test
    void redisTestByRepository() throws InterruptedException {
        Account account = new Account();
        account.setEmail("robin00q@naver.com");
        account.setUsername("sjlee");

        accountRepository.save(account);
        Optional<Account> byId = accountRepository.findById(account.getId());
        System.out.println(byId.get().getId());
        System.out.println(byId.get().getUsername());
        System.out.println(byId.get().getEmail());
    }
}
@RedisHash("accounts")
@Setter @Getter
class Account {
    @Id
    private String id;

    private String username;

    private String email;
}

interface AccountRepository extends CrudRepository<Account, String> {

}
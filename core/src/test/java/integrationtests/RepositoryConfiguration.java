package integrationtests;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by Krzysztof Wasiak on 28/01/2016.
 */

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.tjazi.infra.messagesrouterupdater.core.dao.model"})
@EnableJpaRepositories(basePackages = {"com.tjazi.infra.messagesrouterupdater.core.dao"})
@EnableTransactionManagement
public class RepositoryConfiguration {
}

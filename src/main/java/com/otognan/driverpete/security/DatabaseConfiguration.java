package com.otognan.driverpete.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/* Connection pooling to prevent:
 * "You should consider either expiring and/or testing connection validity
 * before use in your application, increasing the server configured values for client timeouts,
 * or using the Connector/J connection property 'autoReconnect=true' to avoid this problem."
 * Based on http://stackoverflow.com/questions/2077081/connection-with-mysql-is-being-aborted-automaticly-how-to-configure-connector-j
 * and https://gist.github.com/krams915/4186488
*/
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
public class DatabaseConfiguration {

    @Autowired
    private Environment env;
    
    @Bean
    public DataSource dataSource() {
        try {               
            ComboPooledDataSource ds = new ComboPooledDataSource();
            ds.setDriverClass(env.getRequiredProperty("spring.datasource.driverClassName"));
            ds.setJdbcUrl(env.getRequiredProperty("spring.datasource.url"));
            ds.setUser(env.getRequiredProperty("spring.datasource.username"));
            ds.setPassword(env.getRequiredProperty("spring.datasource.password"));
            ds.setAcquireIncrement(5);
            ds.setIdleConnectionTestPeriod(60);
            ds.setMaxPoolSize(100);
            ds.setMaxStatements(50);
            ds.setMinPoolSize(10);
            return ds;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

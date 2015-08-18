package com.otognan.driverpete;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
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
    
//    @Bean
//    public PlatformTransactionManager transactionManager() {
//        return new JpaTransactionManager();
//    }
//    
    
//    @Bean 
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
//        //em.setPersistenceXmlLocation("classpath*:META-INF/persistence.xml");
//        //em.setPersistenceUnitName("hibernatePersistenceUnit");
//        em.setDataSource(dataSource());
//        
//        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
//        vendor.setShowSql(false);
//        em.setJpaVendorAdapter(vendor);
//        
//        return em;
//    }
    
    // Declare a transaction manager
//    @Bean 
//    public JpaTransactionManager transactionManager(DataSource ds) {
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setDataSource(ds);
//        //transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
//        return transactionManager;
//    }
}

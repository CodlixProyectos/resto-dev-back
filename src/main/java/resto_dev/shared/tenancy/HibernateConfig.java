package resto_dev.shared.tenancy;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configure Hibernate Multi-Tenancy safely by overriding the
 * EntityManagerFactory.
 * This guarantees the DataSource and Providers are fully initialized by Spring
 * before Hibernate boots up.
 */
@Configuration
@RequiredArgsConstructor
public class HibernateConfig {

    private final JpaProperties jpaProperties;

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource,
            MultiTenantConnectionProvider<String> multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver) {

        Map<String, Object> hibernateProps = new HashMap<>(jpaProperties.getProperties());
        hibernateProps.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        hibernateProps.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);

        return builder
                .dataSource(dataSource)
                .packages("resto_dev") // Scan all entities
                .properties(hibernateProps)
                .build();
    }
}

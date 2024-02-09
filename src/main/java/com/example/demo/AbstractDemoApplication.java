package com.example.demo;

import com.example.demo.controller.HomePageController;
import com.example.demo.controller.rest.UserRestController;
import com.example.demo.entity.User;
import com.example.demo.entity.repository.UserRepository;
import com.example.demo.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.jdbc.datasource.AbstractDataSource;

/** @author Pankaj */
public abstract class AbstractDemoApplication {

  @Value("${spring.jpa.database-platform}")
  private String databasePlatform;

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Value("${spring.datasource.username}")
  private String dataSourceUserName;

  @Value("${spring.datasource.password}")
  private String dataSourcePassword;

  private EntityManager entityManager;

  private final Class[] entityClasses = new Class[] {User.class};

  public EntityManager entityManager() {
    if (entityManager == null) {
      entityManager = entityManagerFactory().createEntityManager();
    }
    return entityManager;
  }

  public EntityManagerFactory entityManagerFactory() {
    return new EntityManagerFactoryBuilderImpl(
            new PersistenceUnitInfoDescriptor(
                new PersistenceUnitInfo() {

                  private final List<String> managedClassNames =
                      Arrays.asList(entityClasses).stream()
                          .map(Class::getName)
                          .collect(Collectors.toList());

                  private PersistenceUnitTransactionType transactionType =
                      PersistenceUnitTransactionType.RESOURCE_LOCAL;

                  private DataSource jtaDataSource;

                  private DataSource nonJtaDataSource;

                  @Override
                  public String getPersistenceUnitName() {
                    return "DynamicJpaPersistenceUnit";
                  }

                  @Override
                  public String getPersistenceProviderClassName() {
                    return HibernatePersistenceProvider.class.getName();
                  }

                  @Override
                  public PersistenceUnitTransactionType getTransactionType() {
                    return transactionType;
                  }

                  @Override
                  public DataSource getJtaDataSource() {
                    return jtaDataSource;
                  }

                  public PersistenceUnitInfo setJtaDataSource(DataSource jtaDataSource) {
                    this.jtaDataSource = jtaDataSource;
                    this.nonJtaDataSource = null;
                    transactionType = PersistenceUnitTransactionType.JTA;
                    return this;
                  }

                  @Override
                  public DataSource getNonJtaDataSource() {
                    return nonJtaDataSource;
                  }

                  public PersistenceUnitInfo setNonJtaDataSource(DataSource nonJtaDataSource) {
                    this.nonJtaDataSource = nonJtaDataSource;
                    this.jtaDataSource = null;
                    transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
                    return this;
                  }

                  @Override
                  public List<String> getMappingFileNames() {
                    return new ArrayList<>();
                  }

                  @Override
                  public List<URL> getJarFileUrls() {
                    return Collections.emptyList();
                  }

                  @Override
                  public URL getPersistenceUnitRootUrl() {
                    return null;
                  }

                  @Override
                  public List<String> getManagedClassNames() {
                    return managedClassNames;
                  }

                  @Override
                  public boolean excludeUnlistedClasses() {
                    return false;
                  }

                  @Override
                  public SharedCacheMode getSharedCacheMode() {
                    return SharedCacheMode.UNSPECIFIED;
                  }

                  @Override
                  public ValidationMode getValidationMode() {
                    return ValidationMode.AUTO;
                  }

                  @Override
                  public Properties getProperties() {
                    Properties properties = new Properties();

                    properties.put("hibernate.dialect", databasePlatform);
                    properties.put(
                        "hibernate.connection.datasource",
                        new AbstractDataSource() {

                          @Override
                          public Connection getConnection() throws SQLException {
                            return getConnection(dataSourceUserName, dataSourcePassword);
                          }

                          @Override
                          public Connection getConnection(String username, String password)
                              throws SQLException {
                            return DriverManager.getConnection(dataSourceUrl, username, password);
                          }
                        });

                    return properties;
                  }

                  @Override
                  public String getPersistenceXMLSchemaVersion() {
                    return "2.1";
                  }

                  @Override
                  public ClassLoader getClassLoader() {
                    return Thread.currentThread().getContextClassLoader();
                  }

                  @Override
                  public ClassLoader getNewTempClassLoader() {
                    return null;
                  }

                  @Override
                  public void addTransformer(ClassTransformer ct) {}
                }),
            new HashMap<>())
        .build();
  }

  @Bean
  public HomePageController homePageController() {
    return new HomePageController();
  }

  @Bean
  public UserRestController userRestController() {
    return new UserRestController();
  }

  @Bean
  public UserService userService() {
    return new UserService();
  }

  @Bean
  public UserRepository userRepository() {
    return new UserRepositoryImpl(entityManager());
  }
}

class UserRepositoryImpl extends AbstractSimpleJpaRepository<User, Long> implements UserRepository {

  public UserRepositoryImpl(EntityManager entityManager) {
    super(User.class, entityManager);
  }

  @Override
  public List<User> findAllByUserName(String userName) {
    return getEntityManager()
        .createQuery("SELECT u FROM User u WHERE u.userName = :userName")
        .setParameter("userName", userName)
        .getResultList();
  }
}

abstract class AbstractSimpleJpaRepository<T, ID> extends SimpleJpaRepository<T, ID>
    implements JpaRepository<T, ID> {

  private final EntityManager entityManager;

  AbstractSimpleJpaRepository(Class<T> domainClass, EntityManager entityManager) {
    super(domainClass, entityManager);
    this.entityManager = entityManager;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  @Override
  public <S extends T> S save(S entity) {
    try {
      entityManager.getTransaction().begin();
      entityManager.persist(entity);
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      entityManager.getTransaction().rollback();
    }
    return entity;
  }
}

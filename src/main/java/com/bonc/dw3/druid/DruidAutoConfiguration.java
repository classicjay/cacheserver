package com.bonc.dw3.druid;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages="com.bonc.dw3.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
//@EnableAutoConfiguration
//@ConditionalOnClass(ComboPooledDataSource.class)
//@EnableConfigurationProperties(C3p0Properties.class)
public class DruidAutoConfiguration implements EnvironmentAware{
	private final static Logger log = LoggerFactory.getLogger(DruidAutoConfiguration.class);
	
    @Autowired
    private Environment env;
    
    @Bean
    public DataSource dataSource() {
    	try {
    		log.debug("Configruing Druid DataSource");
    		DruidDataSource datasource = new DruidDataSource();
    		datasource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
    		datasource.setUrl(env.getProperty("spring.datasource.url"));
    		datasource.setUsername(env.getProperty("spring.datasource.username"));
    		datasource.setPassword(env.getProperty("spring.datasource.password"));
    		
    		datasource.setInitialSize(Integer.parseInt(env.getProperty("spring.datasource.initialSize")));
    		datasource.setMinIdle(Integer.parseInt(env.getProperty("spring.datasource.minIdle")));
    		datasource.setMaxActive(Integer.parseInt(env.getProperty("spring.datasource.maxActive")));
    		datasource.setMaxWait(Integer.parseInt(env.getProperty("spring.datasource.maxWait")));
    		datasource.setTimeBetweenEvictionRunsMillis(Integer.parseInt(env.getProperty("spring.datasource.timeBetweenEvictionRunsMillis")));
    		datasource.setMinEvictableIdleTimeMillis(Integer.parseInt(env.getProperty("spring.datasource.minEvictableIdleTimeMillis")));
    		datasource.setValidationQuery(env.getProperty("spring.datasource.validationQuery"));
    		datasource.setTestWhileIdle(Boolean.valueOf(env.getProperty("spring.datasource.testWhileIdle")));
    		datasource.setTestOnBorrow(Boolean.valueOf(env.getProperty("spring.datasource.testOnBorrow")));
    		datasource.setTestOnReturn(Boolean.valueOf(env.getProperty("spring.datasource.testOnReturn")));
    		datasource.setPoolPreparedStatements(Boolean.valueOf(env.getProperty("spring.datasource.poolPreparedStatements")));
    		datasource.setMaxPoolPreparedStatementPerConnectionSize(Integer.parseInt(env.getProperty("spring.datasource.maxPoolPreparedStatementPerConnectionSize")));
    		datasource.setFilters(env.getProperty("spring.datasource.filters"));
    		datasource.setConnectionProperties(env.getProperty("spring.datasource.connectionProperties"));
	        return datasource;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}
    	return null;
    }
    
    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        /*final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliasesPackage("xxx.mybatis");
        return sessionFactory.getObject();*/
    	SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
  
        /*sqlSessionFactoryBean.setTypeAliasesPackage(env  
                .getProperty("mybatis.typeAliasesPackage"));  
        sqlSessionFactoryBean  
                .setMapperLocations(new PathMatchingResourcePatternResolver()  
                        .getResources(env  
                                .getProperty("mybatis.mapperLocations"))); */ 
        sqlSessionFactoryBean  
                .setConfigLocation(new DefaultResourceLoader()  
                        .getResource(env.getProperty("mybatis.config")));
        
        return sqlSessionFactoryBean.getObject();
    }

	@Override
	public void setEnvironment(Environment environment) {
		this.env = environment;
	}
	
	@Bean
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory(dataSource()));
    }

}
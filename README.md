<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:aop="http://www.springframework.org/schema/aop" 
	xmlns:tx="http://www.springframework.org/schema/tx"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd
        http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.0.xsd
        http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd">
    <tx:annotation-driven transaction-manager="transactionManager" order="400"></tx:annotation-driven>
    
    
    <bean id="db_url" class="com.huawei.support.cbb.util.configuration.KVConfiguration" factory-method="getValue" depends-on="KVConfiguration">
		<constructor-arg value="support/projectservice/db.url" />
	</bean>

	<bean id="db_username" class="com.huawei.support.cbb.util.configuration.KVConfiguration" factory-method="getValue" depends-on="KVConfiguration">
		<constructor-arg value="support/projectservice/db.username" />
	</bean>

	<bean id="db_pwd" class="com.huawei.support.cbb.util.crypt.HwEncryptUtil" factory-method="decryptAESWithHWSF" depends-on="KVConfiguration">
		<constructor-arg>
			<bean class="com.huawei.support.cbb.util.configuration.KVConfiguration" factory-method="getValue">
				<constructor-arg value="support/projectservice/db.password" />
			</bean>
		</constructor-arg>
	</bean>

	<bean id="dataSource" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
		<property name="driverClassName" value="com.mysql.jdbc.Driver" />
		<property name="url" value="#{db_url}" />
		<property name="username" value="#{db_username}" />
		<property name="password" value="#{db_pwd}" />
		<!-- 默认为true，需要设置为false -->
		<property name="testOnBorrow" value="${dbcp2.testOnBorrow}" />
		<property name="testOnReturn" value="${dbcp2.testOnReturn}" />
		<property name="testWhileIdle" value="${dbcp2.testWhileIdle}" />
		<!-- 连接池启动时的初始值 -->
		<property name="initialSize" value="${dbcp2.initialSize}" />
		<!-- 连接池的最大值 -->
		<property name="maxTotal" value="${dbcp2.maxTotal}" />
		<!-- 最大空闲值.当经过一个高峰时间后，连接池可以慢慢将已经用不到的连接慢慢释放一部分，一直减少到maxIdle为止 -->
		<property name="maxIdle" value="${dbcp2.maxIdle}" />
		<!-- 最小空闲值.当空闲的连接数少于阀值时，连接池就会预申请去一些连接，以免洪峰来时来不及申请 -->
		<property name="minIdle" value="${dbcp2.minIdle}" />
		<!-- #在取出连接时进行有效验证 -->
		<property name="removeAbandonedTimeout" value="${dbcp2.removeAbandonedTimeout}" />
		<property name="removeAbandonedOnMaintenance" value="${dbcp2.removeAbandonedOnMaintenance}" />
		<property name="removeAbandonedOnBorrow" value="${dbcp2.removeAbandonedOnBorrow}" />
		<!-- #运行判断连接超时任务的时间间隔，单位为毫秒，默认为-1，即不执行任务。 -->
		<property name="timeBetweenEvictionRunsMillis" value="${dbcp2.timeBetweenEvictionRunsMillis}" />
		<!-- #连接的超时时间，默认为半小时。 -->
		<property name="minEvictableIdleTimeMillis" value="${dbcp2.minEvictableIdleTimeMillis}" />
	</bean>
	
	
	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<property name="dataSource" ref="dataSource"></property>
		<!-- mybatis配置文件自动扫描路径 -->
		<property name="mapperLocations" value="classpath:mybatis/mysql/*.xml"></property>
		<property name="configLocation" value="classpath:mybatis/mybatis-config.xml"></property>
	</bean>
	
	<bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
		<constructor-arg index="0" ref="sqlSessionFactory" />
	</bean>

	<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
		<property name="dataSource" ref="dataSource" />
	</bean>

	<bean id="flywaySqlMigrationRunner" class="com.huawei.support.sql.migrate.SqlMigrationRunner">
		<property name="dataSource" ref="dataSource" />
	</bean>
	
	<!-- *******************以下部分为动态变化部分************************************* -->
	<bean id="projectMemberMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.ProjectMemberMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
	
	<bean id="projectGroupMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.ProjectGroupMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
	
	<bean id="projectMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.ProjectMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
	
	<bean id="projectTaskMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.ProjectTaskMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
	<bean id="taskMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.TaskMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
	<bean id="projectUser2GroupMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.ProjectUser2GroupMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
	<bean id="directoryMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.DirectoryMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
	<bean id="directoryPropertyMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.DirectoryPropertyMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
	<bean id="deliverableMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
		<property name="mapperInterface" value="com.huawei.dfx.projectservice.dao.mapper.DeliverableMapper" />
		<property name="sqlSessionFactory" ref="sqlSessionFactory" />
	</bean>
</beans>
------------------------------
service_dao.xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration   
    PUBLIC "-//mybatis.org//DTD Config 3.0//EN"  
    "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <settings>
        <!-- 打印查询语句 -->
        <setting name="logImpl" value="STDOUT_LOGGING" />
    </settings>
    
    <!-- mapper已经在spring-mybatis.xml中的sqlSessionFactory配置，这里不再需要配置 -->
<!--     <mappers> -->
<!--         <mapper resource="com/a/b/c/dao/BusinessInfoDaoMapper.xml" /> -->
<!--     </mappers> -->
</configuration>

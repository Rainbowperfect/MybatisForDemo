package org.person;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages="org.org.person.mapper")
@SpringBootApplication
public class ApplicationRun
{
    public static void main( String[] args )
    {
        SpringApplication.run(ApplicationRun.class,args);
    }
}

package org.person.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.person.pojo.Employee;

@Mapper
public interface EmployeeMapper {

     Employee getEmployee(Integer id);

     void insertEmp(EmployeeMapper employee);
}

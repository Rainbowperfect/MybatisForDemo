package org.person.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.person.pojo.Department;

import java.util.List;

//@Mapper
public interface DepartmentMapper {

    @Select("select * from department")
    List<Department> getAll();

    @Select("select * from department where id=#{id}")
    Department getById(Integer id);

    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("insert into department(departmentName) values(#{departmentName})")
    int insertDept(Department department);
}
package org.person.controller;

import org.person.mapper.DepartmentMapper;
import org.person.mapper.EmployeeMapper;
import org.person.pojo.Department;
import org.person.pojo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DepartmentController {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @RequestMapping("/dept/{id}")
    public Department getById(@PathVariable("id") Integer id){

       return departmentMapper.getById(id);
    }

    @RequestMapping("/dept/add")
    public Integer add(Department department){
        return departmentMapper.insertDept(department);
    }

    @RequestMapping("/emp/{id}")
    public Employee getEmployee(@PathVariable ("id") Integer id){
        Employee employee = employeeMapper.getEmployee(id);
        return  employee;
    }
}

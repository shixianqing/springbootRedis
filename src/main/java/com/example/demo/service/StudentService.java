package com.example.demo.service;

import java.util.List;
import java.util.Map;

import com.example.demo.service.base.BaseService;
import com.example.demo.table.Student;

public interface StudentService extends BaseService<Student>{

	List<Student> pageQuery(Map<String, Object> params);
	
	void delStuById(Student student);
	
	Student findOne(int id);
}

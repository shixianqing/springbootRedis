package com.example.demo.service.impl;

import java.util.List;
import java.util.Map;

import com.example.demo.common.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dao.repository.StudentRepository;
import com.example.demo.ns.Consts;
import com.example.demo.service.StudentService;
import com.example.demo.table.Student;

@Service
public class StudentServiceImpl implements StudentService {
	
	@Autowired
	private StudentRepository repository;
	
	@Transactional
	@Override
	public void insert(Student entity) {
		repository.save(entity);
	}


	@Override
	public void delStuById(Student student) {
		repository.deleteById(student.getStuId());
		
	}


	@Override
	public List<Student> pageQuery(Map<String, Object> params) {
		int pageNo = StringUtil.getInt(params, Consts.PAGE_NO, 1);
		int pageSize = StringUtil.getInt(params, Consts.PAGE_SIZE, 5);
		
		//设置查询起始页
		pageNo = (pageNo-1);
		Page<Student> page = repository.findAll(new PageRequest(pageNo, pageSize));
		//获取总页数
		int totalPages = page.getTotalPages();
		boolean isNextPage = false;
		//判断是否有下一页
		if(totalPages/2>0){
			isNextPage = true;
		}else{
			isNextPage = false;
		}
		params.put("totalPages", totalPages);
		params.put("isNextPage", isNextPage);
		return page.getContent();
	}


	@Override
	public Student findOne(int id) {
		Student stu = repository.findStuByStuId(id);
		return stu;
	}
	
	

}

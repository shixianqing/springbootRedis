package com.example.demo;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.demo.dao.repository.StudentRepository;
import com.example.demo.service.StudentService;
import com.example.demo.table.Student;
import com.study.util.LoggerUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootJpaApplicationTests {

	@Autowired
	private StudentRepository repository;
	
	@Autowired
	private StudentService service;
	
	private LoggerUtil logger = LoggerUtil.getInstance(getClass());
	
	@Test
	public void contextLoads() {
	}
	
	@Test
	public void insertStu(){
		Student entity = new Student("李打野", "2012080244");
		repository.save(entity);
	} 
	
	@Test
	public void qryStuById(){
		Student student = repository.findStuByStuId(1);
		logger.debug("id为1的学生新信息为{0}",student.toString());
	}
	
	@Test
	public void pageQry(){
		@SuppressWarnings("deprecation")
		Page<Student> page = repository.findAll(new PageRequest(0, 10,new Sort(Direction.DESC, "stuNo")));
		
		logger.debug("分页查询结果===={0}",page.getContent());
	}
	
	@Test
	public void findStuByStuNameLike(){
		List<Student> list = repository.findStuByStuNameLike("李%");
		
		logger.debug("根据姓名模糊查询的结果为{0}",list);
	}
	@Test
	public void findStuByStuIdBetween(){
		List<Student> list = repository.findStuByStuIdBetween(1,1);
		
		logger.debug("根据id范围查询的结果为{0}",list);
	}
	@Test
	public void findStuByStuNo(){
		Student student = repository.findStuByStuNo("2012080244");
		
		logger.debug("根据id范围查询的结果为{0}",student);
	}
	
	@Test
	public void insert(){
		Student entity = new Student("王二麻子", "2012080249");
		service.insert(entity);
	}

}

package com.example.demo;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.demo.service.SeqGenerator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpringbootJpaApplicationTests {

	@Autowired
	private StudentRepository repository;
	
	@Autowired
	private StudentService service;

	@Autowired
    private SeqGenerator seqGenerator;
	
	
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
		log.debug("id为1的学生新信息为{0}",student.toString());
	}
	
	@Test
	public void pageQry(){
		@SuppressWarnings("deprecation")
		Page<Student> page = repository.findAll(new PageRequest(0, 10,new Sort(Direction.DESC, "stuNo")));
		
		log.debug("分页查询结果===={0}",page.getContent());
	}
	
	@Test
	public void findStuByStuNameLike(){
		List<Student> list = repository.findStuByStuNameLike("李%");
		
		log.debug("根据姓名模糊查询的结果为{0}",list);
	}
	@Test
	public void findStuByStuIdBetween(){
		List<Student> list = repository.findStuByStuIdBetween(1,1);
		
		log.debug("根据id范围查询的结果为{0}",list);
	}
	@Test
	public void findStuByStuNo(){
		Student student = repository.findStuByStuNo("2012080244");
		
		log.debug("根据id范围查询的结果为{0}",student);
	}
	
	@Test
	public void insert(){
		Student entity = new Student("王二麻子", "2012080249");
		service.insert(entity);
	}

	@Test
    public void geneSeq(){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 10, 60000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024));
        for (int i=0; i<50; i++){
            poolExecutor.execute(() ->{
                String seq = seqGenerator.genSeqCode("test");
                log.info("{}",seq);
            });
        }

        poolExecutor.shutdown();
    }

}

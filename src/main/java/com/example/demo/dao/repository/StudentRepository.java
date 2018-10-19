package com.example.demo.dao.repository;

import java.util.List;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.example.demo.table.Student;


public interface StudentRepository extends PagingAndSortingRepository<Student, Integer> {

	Student findStuByStuId(int id);
	List<Student> findStuByStuNameLike(String name);
	List<Student> findStuByStuIdBetween(int beginId,int endId);
	
	/**
	 * 表名为@Entity(name="student")的name属性值
	 * 必须将所有字段全部查回来，否则无法转换成实体类
	 * 注解优先级高于方法命名规则
	 *	@param stuNo 实体类的属性
	 *	@return
	 *	Student
	 */
	@Query(value="select s from student s where s.stuNo=?1")
	Student findStuByStuNo(String stuNo);
	
	@Query("select count(s.stuId) from student s")
	long findTotalNum();

}

package com.example.demo.table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name="student")
public class Student {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int stuId;
	
	@Column(length=5,nullable=true)
	private String stuName;
	
	@Column(length=20)
	private String stuNo;

	public int getStuId() {
		return stuId;
	}

	public void setStuId(int stuId) {
		this.stuId = stuId;
	}

	public String getStuName() {
		return stuName;
	}

	public void setStuName(String stuName) {
		this.stuName = stuName;
	}

	public String getStuNo() {
		return stuNo;
	}

	public void setStuNo(String stuNo) {
		this.stuNo = stuNo;
	}

	public Student(String stuName, String stuNo) {
		super();
		this.stuName = stuName;
		this.stuNo = stuNo;
	}

	public Student() {
		super();
	}

	@Override
	public String toString() {
		return "Student [stuId=" + stuId + ", stuName=" + stuName + ", stuNo="
				+ stuNo + "]";
	}
	
	
}

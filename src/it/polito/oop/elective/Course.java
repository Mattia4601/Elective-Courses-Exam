package it.polito.oop.elective;

import java.util.LinkedList;
import java.util.List;

public class Course {
	private String courseName;
	private int nStudents;
	private List<Student> enrolled = new LinkedList<>();
	
	//constructor
	public Course(String courseName, int availablePositions) {
		super();
		this.courseName = courseName;
		this.nStudents = availablePositions;
	}
	

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	// returns true if the course has more available positions than students enrolled
	public boolean hasRoom() {
		return enrolled.size()<nStudents;
	}
	
	public void enroll(Student s) {
		enrolled.add(s);
	}
	
	public List<Student> getEnrolled(){
		return enrolled;
	}

}

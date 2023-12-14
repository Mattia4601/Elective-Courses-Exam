package it.polito.oop.elective;

import java.util.Collection;
import java.util.List;

public class Student {
	private String studentID;
	private double average;
	private List<Course> requests;
	private Course enrolledIn;
	
	// restituisce il corrispondente numero della preferenza legata al corso dato per argomento
	public int choiceNo(String course) {
		for (int i=0; i<requests.size(); i++) {
			if(requests.get(i).getCourseName().equals(course))
				return i+1;
		}
		return -1;
	}
	
	public Course getEnrolledIn() {
		return enrolledIn;
	}

	public void setEnrolledIn(Course enrolledIn) {
		this.enrolledIn = enrolledIn;
	}

	Collection<Course> getPreferences(){
		return requests;
	}
	
	public void setRequests(List<Course> requests) {
		this.requests=requests;
	}
	
	public String getStudentID() {
		return studentID;
	}

	public void setStudentID(String studentID) {
		this.studentID = studentID;
	}
	public double getAverage() {
		return average;
	}
	public void setAverage(double average) {
		this.average = average;
	}
	
	public Student(String studentID, double average) {
		super();
		this.studentID = studentID;
		this.average = average;
	}
	// check if a student is enrolled to any course 
	public boolean isEnrolled() {
		if (this.enrolledIn!=null)
			return true;
		return false;
	}
	
	public boolean isEnrolled(int choice) {
	
		return isEnrolled() && choice <= requests.size() && enrolledIn == requests.get(choice-1);
	}
	
}

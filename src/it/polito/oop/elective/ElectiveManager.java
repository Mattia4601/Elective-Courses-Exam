package it.polito.oop.elective;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.*;
import static java.util.Comparator.*;
import java.util.TreeMap;
import java.util.ArrayList;

/**
 * Manages elective courses enrollment.
 * 
 *
 */
public class ElectiveManager {
	
	private Map<String,Course> courses = new HashMap();
	private Map<String,Student> students = new HashMap();
	private List<Notifier> listeners = new LinkedList<>();
    /**
     * Define a new course offer.
     * A course is characterized by a name and a number of available positions.
     * 
     * @param name : the label for the request type
     * @param availablePositions : the number of available positions
     */
    public void addCourse(String name, int availablePositions) {
        Course c = new Course(name,availablePositions);
        courses.put(name, c); //add the new course to the map courses
        
    }
    
    /**
     * Returns a list of all defined courses
     * @return
     */
    public SortedSet<String> getCourses(){
        return courses.keySet().stream() //stream with all keys set
        		.sorted() //sort the stream
        		.collect(TreeSet::new,Set::add,Set::addAll); //create a new TreeSet and add all the courses to the stream
        //Notice that the last argument is only for parallel processing streams, so it's added for completeness 
    }
    
    /**
     * Adds a new student info.
     * 
     * @param id : the id of the student
     * @param gradeAverage : the grade average
     */
    public void loadStudent(String id, 
                                  double gradeAverage){
    	Student s = new Student(id,gradeAverage);
        students.put(id, s); //adding the new student to the map students
        
    }

    /**
     * Lists all the students.
     * 
     * @return : list of students ids.
     */
    public Collection<String> getStudents(){
        return students.keySet().stream() //get the stream of studentsIDs
        		.collect(Collectors.toList());
    }
    
    /**
     * Lists all the students with grade average in the interval.
     * 
     * @param inf : lower bound of the interval (inclusive)
     * @param sup : upper bound of the interval (inclusive)
     * @return : list of students ids.
     */
    public Collection<String> getStudents(double inf, double sup){
        return students.values().stream()  //getting the stream of the map values
        		.filter(s -> s.getAverage() >= inf && s.getAverage() <= sup)
        		.map(Student::getStudentID)
        		.collect(Collectors.toList());
        		
    }


    /**
     * Adds a new enrollment request of a student for a set of courses.
     * <p>
     * The request accepts a list of course names listed in order of priority.
     * The first in the list is the preferred one, i.e. the student's first choice.
     * 
     * @param id : the id of the student
     * @param selectedCourses : a list of of requested courses, in order of decreasing priority
     * 
     * @return : number of courses the user expressed a preference for
     * 
     * @throws ElectiveException : if the number of selected course is not in [1,3] or the id has not been defined.
     */
    public int requestEnroll(String id, List<String> courses)  throws ElectiveException {
        
    	if (courses.size() < 1 || courses.size() > 3)
    		throw new ElectiveException();
    	
    	if (! this.students.containsKey(id))
    		throw new ElectiveException();
    	
    	for (String course : courses) {
    		
    		if (!this.courses.containsKey(course))
    			throw new ElectiveException();
    	}
    	
    	// after the checks
    	Student s = students.get(id);
    	//we add to the student object the requests he asked
    	s.setRequests(courses.stream().map(courseName->this.courses.get(courseName)).collect(Collectors.toList()));
    	listeners.forEach(l->l.requestReceived(id));
    	
    	return courses.size();
    }
    
    /**
     * Returns the number of students that selected each course.
     * <p>
     * Since each course can be selected as 1st, 2nd, or 3rd choice,
     * the method reports three numbers corresponding to the
     * number of students that selected the course as i-th choice. 
     * <p>
     * In case of a course with no requests at all
     * the method reports three zeros.
     * <p>
     * 
     * @return the map of list of number of requests per course
     */
    public Map<String,List<Long>> numberRequests(){
        	
    	return courses.keySet().stream()
        		.collect(
        				Collectors.toMap(  //make a new map
        						c -> (String) c,   // key = course name
        						c -> (List<Long>) students.values().stream() //getting a stream of student items
        						.map(s->s.choiceNo(c)) // map the student elements to the choice number related to the course c
        						.collect(collectingAndThen(
        								groupingBy(n->n, counting()),
        								m -> { //creo il vettore che contiene il numero di studenti che hanno scelto il corso in esame per ogni tipo di preferenza (1,2 o 3 che sia)
        									ArrayList<Long> res = new ArrayList<>();
        									for (int i=1; i<=3; i++) {
        										res.add(m.getOrDefault(i,0L));
        									}
        									
        									return res;
        								}
        								)
        						)
        					)
        				);
    }
    
    
    /**
     * Make the definitive class assignments based on the grade averages and preferences.
     * <p>
     * Student with higher grade averages are assigned to first option courses while they fit
     * otherwise they are assigned to second and then third option courses.
     * <p>
     *  
     * @return the number of students that could not be assigned to one of the selected courses.
     */
    public long makeClasses() {
        
    	List<Student> studentsPerAvg = students.values().stream().sorted(comparing(Student::getAverage).reversed()).toList();
    	
    	return studentsPerAvg.stream()
    			.mapToInt(s->{
    				if(s.getPreferences()!=null) { //if the student has expressed some references
    					s.getPreferences().stream() //get the stream of preferences List of courses
    					.filter(Course::hasRoom) //take only courses with still some available positions
    					.findFirst().ifPresent(c->{  //take the first course of the stream
    						c.enroll(s); // enroll the student
    						s.setEnrolledIn(c);
    						listeners.forEach(l->l.assignedToCourse(s.getStudentID(), c.getCourseName()));
    					});
    				}
    				if (s.isEnrolled()) //if the student is enrolled we don't sum it to the number of students not enrolled to any course
    					return 0;
    				else //otherwise we sum it 
    					return 1;
    			}).sum(); //at the end we sum all the student not enrolled
    }
    
    
    /**
     * Returns the students assigned to each course.
     * 
     * @return the map course name vs. student id list.
     */
    public Map<String,List<String>> getAssignments(){
        return this.courses.values().stream()
        		.collect(Collectors.toMap( Course::getCourseName,
        				c->c.getEnrolled().stream()
        				.sorted(comparing(Student::getAverage).reversed())
        				.map(Student::getStudentID).toList()));
    }
    
    
    /**
     * Adds a new notification listener for the announcements
     * issues by this course manager.
     * 
     * @param listener : the new notification listener
     */
    public void addNotifier(Notifier listener) {
        listeners.add(listener);
    }

    
    /**
     * Computes the success rate w.r.t. to first 
     * (second, third) choice.
     * 
     * @param choice : the number of choice to consider.
     * @return the success rate (number between 0.0 and 1.0)
     */
    public double successRate(int choice){
        double numStudentsEnrolled = students.values().stream()
        		.filter(s->s.isEnrolled(choice))
        		.count();
        double totalStudents = students.size();
    	return numStudentsEnrolled / totalStudents;
    }

    
    /**
     * Returns the students not assigned to any course.
     * 
     * @return the student id list.
     */
    public List<String> getNotAssigned(){
        return students.values().stream()
        		.filter(s->s.isEnrolled()==false)
        		.map(Student :: getStudentID)
        		.toList();
    }
    
    
}

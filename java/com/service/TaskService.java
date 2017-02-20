package com.service;

import static com.app.RedisKeys.tk;

import java.util.Map;

import com.common.DBService;
import com.domain.StatusMessage;
import com.modal.Task;
import com.modal.TaskList;

import wjw.shiro.redis.RedisPoolManager;

public class TaskService {
	
	public enum TaskType {
		TR("Training");

		private String value;

        private TaskType(String value) {
        	this.value = value;
        }
	};

	public TaskList getTasks(String companyid, String status) {
		
		TaskList tasklist = new TaskList(); 
		
		String tasklistkey = companyid+tk+"tasks";

		Map<String, String> taskmap = RedisPoolManager.hgetAll(tasklistkey);
		
		taskmap.forEach( (k, v) -> {
			Task task = new Task(v.toString());
			if(status.equals(task.getString("status"))){ //open or close
					task.put("taskid", k); //need taskid for table selection
					//task.put("task", v);
				tasklist.put(task);	
			}
		 });
		
		return tasklist;
	}
	
	public Task getTask(String companyid, String taskid){
		String tasklistkey = companyid+tk+"tasks";
		Map<String, String> taskmap = RedisPoolManager.hgetAll(tasklistkey);
		Task task = new Task(taskmap.get(taskid));
		return task;
	}
	
	//add Task from job
	public void addTask(String companyid, String taskid, Task task) {		
		String tasklistkey = companyid+tk+"tasks";
		DBService.save(tasklistkey, taskid, task);
	}
	
	//update Task from GUI
	public StatusMessage updateTask(String companyid, String taskid, String status) {		
		
		StatusMessage msg = new StatusMessage(taskid, "Task updated successfully!");
				
		String tasklistkey = companyid+tk+"tasks";
		
		Task task = getTask(companyid, taskid);
			task.put("status", status);
		
		boolean ok = DBService.save(tasklistkey, taskid, task);
		if(!ok){
			msg = new StatusMessage(taskid, "ERROR:Error while updating Task!");
		}
		return msg;	
	}
}

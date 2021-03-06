package kb50.appointment;

import java.sql.Date;
import java.util.List;

public class Appointment {

	private int id;
	private String name;
	private String description;
	private String date;
	private int priority;
	private List<User> users;
	private String location;
	private int owner;
	private boolean received;

	public boolean getReceived() {

		return received;

	}

	public void setReceived(int r) {

		if (r == 1) {

			received = true;
		} else {

			received = false;
		}

	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}

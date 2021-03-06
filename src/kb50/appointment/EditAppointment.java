package kb50.appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class EditAppointment extends FragmentActivity {

	private Spinner prioritySpinner;
	private Spinner locationSpinner;
	private static Button datePicker;
	private static Button timePicker;
	private static Button reminderDatePicker;
	private static Button reminderTimePicker;

	/*
	 * Number of times we will try to contact the google map server after
	 * timeouts to resolve an address to longitude and latitude
	 */
	private static final int MAX_RETRY = 5;
	/* Max Number of addresses that we get from resolving the address by name */
	private static final int MAX_ADDRESSES = 5;

	private EditText name;
	private EditText description;
	private EditText location;

	private Geocoder gc;
	
	private int appointment_id;

	private int owner_id;
	
	private List<EditText> fields;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_appointment);
		locationSpinner = (Spinner) findViewById(R.id.location_spinner);
		fillFields();
		fields = new ArrayList<EditText>();
		fields.add(name);
		fields.add(description);
		fields.add(location);
	}

	private void fillFields() {
		Intent i = getIntent();
		appointment_id = Integer.parseInt(i.getStringExtra("id"));

		// TODO: Fill ALL fields
		try {
			final SharedPreferences mSharedPreference = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			int id = mSharedPreference.getInt("id", 0);
			owner_id = id;
			List<Object> appointments = new Controller().new Select(
					"http://eduweb.hhs.nl/~13061798/GetAppointments.php?id="
							+ id).execute(new ApiConnector()).get();

			if (!appointments.isEmpty()) {
				for (Object o : appointments) {
					Appointment a = (Appointment) o;

					if (a.getId() == appointment_id) {
						prioritySpinner = (Spinner) findViewById(R.id.priority_spinner);

						// Create an ArrayAdapter using the String array and a
						// default spinner
						// layout
						ArrayAdapter<CharSequence> adapter = ArrayAdapter
								.createFromResource(this,
										R.array.priority_array,
										android.R.layout.simple_spinner_item);
						// Specify the layout to use when the list of choices
						// appears
						adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						// Apply adapter to the spinner
						prioritySpinner.setAdapter(adapter);

						switch (a.getPriority()) {
						case 1:
							prioritySpinner.setSelection(2);
							break;
						case 2:
							prioritySpinner.setSelection(1);
							break;
						case 3:
							prioritySpinner.setSelection(0);
						}

						datePicker = (Button) findViewById(R.id.date_picker);
						String dateTime = a.getDate();
						String[] parts = dateTime.split("\\s+");
						String date = parts[0];
						String time = parts[1];
						datePicker.setText(date);

						timePicker = (Button) findViewById(R.id.time_picker);
						timePicker.setText(time);

						// TODO: add reminder date & time to DB
						reminderDatePicker = (Button) findViewById(R.id.reminder_date_picker);
						reminderTimePicker = (Button) findViewById(R.id.reminder_time_picker);

						name = (EditText) findViewById(R.id.appointment_name_field);
						name.setText(a.getName());

						description = (EditText) findViewById(R.id.appointment_desc_field);
						description.setText(a.getDescription());

						location = (EditText) findViewById(R.id.location_field);
						location.setText(a.getLocation());
					}
				}

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void onClickCancel(View v) {
		this.finish();
	}

	public void onClickSubmit(View v) {
		String dateTime = "";
		String[] reminderDate = null;
		String[] reminderTime = null;

		for (EditText t : fields) {
			t.setBackgroundResource(android.R.drawable.editbox_background);
		}
		if (emptyFields() == false) {
			try {
				//dateTime = tempdate.toString();	
				String[] date = datePicker.getText().toString().split("-");
				int day = Integer.parseInt(date[0]);
				int month = Integer.parseInt(date[1]);
				int year = Integer.parseInt(date[2]);
				
				if (day>31){
					int tempp = day;
					day = year;
					year = tempp;
				}
				
				dateTime = year + "-" + month + "-" + day + " "
						+ timePicker.getText().toString();
				
				
				reminderDate = reminderDatePicker.getText().toString()
						.split("-");
				reminderTime = reminderTimePicker.getText().toString()
						.split(":");

			} catch (NumberFormatException e) {
				Toast.makeText(this, "Pick a date and time please!",
						Toast.LENGTH_SHORT).show();
				return;
			}
			catch(Exception e){
				
			}
			String priority = prioritySpinner.getSelectedItem().toString();
			int p = setPriority(priority);

			Appointment a = new Appointment();
			a.setName(name.getText().toString());
			a.setDescription(description.getText().toString());
			a.setDate(dateTime);
			a.setOwner(owner_id);
			a.setLocation(locationSpinner.getSelectedItem().toString());

			a.setPriority(p);
			if (setAlarm(reminderDate, reminderTime) == true) {
				new Controller().new Insert(a,
						"http://eduweb.hhs.nl/~13061798/EditAppointment.php?id="
								+ appointment_id).execute(new ApiConnector());
				setResult(RESULT_OK);
				this.finish();
			} else {
				return;
			}
		} else {
			Toast.makeText(this, "Please fill in every field!",
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean setAlarm(String[] date, String[] time) {
		try {
			int day = Integer.parseInt(date[0]);
			int month = Integer.parseInt(date[1]);
			int year = Integer.parseInt(date[2]);

			int hour = Integer.parseInt(time[0]);
			int minute = Integer.parseInt(time[1]);

			Calendar cal = new GregorianCalendar();

			cal.set(Calendar.DAY_OF_MONTH, day);
			cal.set(Calendar.MONTH, month - 1);
			cal.set(Calendar.YEAR, year);

			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 00);

			Intent intentAlarm = new Intent(EditAppointment.this,
					AlarmReceiver.class);

			intentAlarm.putExtra("appointment name", name.getText().toString());
			intentAlarm.putExtra("date", datePicker.getText().toString());
			intentAlarm.putExtra("time", timePicker.getText().toString());

			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

			alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
					PendingIntent.getBroadcast(this, 1, intentAlarm,
							PendingIntent.FLAG_UPDATE_CURRENT));

			return true;
		} catch (NumberFormatException e) {
			Toast.makeText(this,
					"Pick a date and time for the reminder please",
					Toast.LENGTH_SHORT).show();
			return false;
		}

	}

	public void onClickDatePicker(View v) {
		switch (v.getId()) {
		case R.id.date_picker:
			DialogFragment dateFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putString("btn", "editNormal");
			dateFragment.setArguments(args);
			dateFragment.show(getSupportFragmentManager(), "datePicker");
			break;
		case R.id.reminder_date_picker:
			DialogFragment reminderDateFragment = new DatePickerFragment();
			Bundle args2 = new Bundle();
			args2.putString("btn", "editReminder");
			reminderDateFragment.setArguments(args2);
			reminderDateFragment
					.show(getSupportFragmentManager(), "datePicker");
			break;
		}
	}

	public void onClickTimePicker(View v) {
		switch (v.getId()) {
		case R.id.time_picker:
			DialogFragment timeFragment = new TimePickerFragment();
			Bundle args = new Bundle();
			args.putString("btn", "editNormal");
			timeFragment.setArguments(args);
			timeFragment.show(getSupportFragmentManager(), "timePicker");
			break;
		case R.id.reminder_time_picker:
			DialogFragment reminderTimeFragment = new TimePickerFragment();
			Bundle args2 = new Bundle();
			args2.putString("btn", "editReminder");
			reminderTimeFragment.setArguments(args2);
			reminderTimeFragment
					.show(getSupportFragmentManager(), "timePicker");
			break;
		}
	}

	public static void setDateButton(String text) {
		datePicker.setText(text);
	}

	public static void setReminderDateButton(String text) {
		reminderDatePicker.setText(text);
	}

	public static void setTimeButton(String text) {
		timePicker.setText(text);
	}

	public static void setReminderTimeButton(String text) {
		reminderTimePicker.setText(text);
	}

	private int setPriority(String priority) {
		int p;

		if (priority.equals("High")) {
			p = 3;
		} else if (priority.equals("medium")) {
			p = 2;
		} else {
			p = 1;
		}

		return p;
	}

	private void hideSoftKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public void onClickLocation(View v) {
		hideSoftKeyboard(v);

		gc = new Geocoder(this);

		String address = location.getText().toString();

		System.err.println(address + "!!!!!!!!!!!!!!!!!!!!!!!");

		try {
			if (Geocoder.isPresent()) {
				List<Address> addrList = null;

				boolean worked = false;
				int count = 0;
				while (!worked && count < MAX_RETRY) {
					try {
						addrList = gc
								.getFromLocationName(address, MAX_ADDRESSES);
						worked = true;
					} catch (Exception te) {
						System.err.println(te
								+ " Exception occurred, will retry max "
								+ MAX_RETRY + " times");
						count++;
					}
				}
				if (addrList != null) {
					System.err.println("!!!!!! Found the following addresses:");
					// Create an ArrayAdapter using the String array and a
					// default spinner
					// layout
					ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
							this, android.R.layout.select_dialog_singlechoice);
					// Specify the layout to use when the list of choices
					// appears
					adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

					// Loop over all the addresses returned by google maps
					// and add them
					// into the spinner adapter array
					StringBuilder sb;
					for (Address addr : addrList) {
						// debug info can be deleted
						System.err.println(addr.getAddressLine(0)
								+ " Country: " + addr.getCountryName()
								+ " Postal Code: " + addr.getPostalCode()
								+ " Locality: " + addr.getLocality() + " lon: "
								+ addr.getLongitude() + " lat: "
								+ addr.getLatitude());

						// Add the address into the adapter for the spinner
						sb = new StringBuilder(addr.getAddressLine(0));
						// if (addr.getFeatureName() != null
						// && addr.getFeatureName().length() > 0) {
						// sb.append(", ").append(addr.getFeatureName());
						// }
						if (addr.getLocality() != null
								&& addr.getLocality().length() > 0) {
							sb.append(" ").append(addr.getLocality());
						}
						if (addr.getPremises() != null
								&& addr.getPremises().length() > 0) {
							sb.append(" ").append(addr.getPremises());
						}
						if (addr.getCountryCode() != null
								&& addr.getCountryCode().length() > 0) {
							sb.append(" ").append(addr.getCountryCode());
						}
						// if (addr.getAdminArea() != null
						// && addr.getAdminArea().length() > 0) {
						// sb.append("5 ").append(addr.getAdminArea());
						// }
						adapter.add(sb.toString());
					}
					// Apply adapter to the spinner
					// locationSpinner.setAdapter(adapter.createFromResource(this,
					// adapter.(android.R.layout.simple_spinner_item),
					// R.layout.spinner_item));
					locationSpinner.setAdapter(adapter);
					locationSpinner.performClick();
				}

				// does not work
				// location.setText(locationSpinner.getSelectedItem().toString());

			} else {
				System.err
						.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! GEOCODER SERVICE IS NOT AVAILABLE");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private boolean emptyFields() {
		String aName = name.getText().toString();
		String desc = description.getText().toString();
		String loc = location.getText().toString();

		if (aName.isEmpty() || desc.isEmpty() || loc.isEmpty()) {
			for (EditText t : fields) {
				if (t.getText().toString().isEmpty()) {
					t.setBackgroundResource(R.drawable.empty_field);
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
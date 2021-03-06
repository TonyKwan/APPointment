package kb50.appointment;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

	private String btn;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Bundle args = getArguments();
		btn = args.getString("btn");
		
		//Use current date as default
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
		String date = String.valueOf(dayOfMonth) + "-" + String.valueOf(monthOfYear + 1) + "-" + String.valueOf(year);
		if(btn.equals("normal")){
			NewAppointment.setDateButton(date);
		}else if(btn.equals("reminder")){
			NewAppointment.setReminderDateButton(date);
		}else if(btn.equals("editNormal")){
			EditAppointment.setDateButton(date);
		}else if(btn.equals("editReminder")){
			EditAppointment.setReminderDateButton(date);
		}
	}
}

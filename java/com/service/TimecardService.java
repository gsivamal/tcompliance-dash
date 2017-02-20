package com.service;

import static com.app.RedisKeys.driver_list;
import static com.app.RedisKeys.tk;
import static com.app.RedisKeys.timecard_suffix;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.common.NuUtil;
import com.domain.StatusMessage;
import com.modal.Driver;
import com.modal.DriverList;
import com.modal.Timecard;
import com.modal.TimecardAddInfo;
import com.modal.TimecardEntry;
import com.modal.TimecardList;
import com.util.CSV;

import wjw.shiro.redis.RedisPoolManager;

public class TimecardService {

	String TOTAL_STR = "Total";

	public StatusMessage saveTimecard(String driverid, String yearmonth, String timecarddata) {

		StatusMessage status = new StatusMessage(driverid, "Time card has been submitted successfully!");

		// company-1-driver-1-timecard
		String timecardid = driverid + timecard_suffix;

		System.out.println("Timecard id:" + timecardid);

		Map<String, String> map = new HashMap<String, String>();
		map.put(yearmonth, timecarddata);

		try {
			RedisPoolManager.hmset(timecardid, map);
		} catch (Exception e) {
			e.printStackTrace();
			status = null;
		} finally {
			// redis.close();
			// redisClient.shutdown();
		}

		return status;
	}
	
	// Timecard -is JSONArray
	public StatusMessage createTimecard(String driverid, String year, String month) {
		
		StatusMessage status = new StatusMessage(driverid, "Time card created successfully!");
		
		Timecard tc = new Timecard();
		
		// DB Call
		Map<String, String> dgmap = RedisPoolManager.hgetAll(driverid + timecard_suffix);

		String tcvalue = dgmap.get(year + tk + month);

		// check exist for this month
		if (tcvalue == null) {
			
			LocalDate date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1);
			int lengthOfMonth = date.lengthOfMonth();

			for (int i = 1; i <= lengthOfMonth; i++) {

				TimecardEntry entry = new TimecardEntry();
				entry.put("dutydate", date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear());
				entry.put("dutyday", date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
				entry.put("starttime", "");
				entry.put("endtime", "");
				entry.put("totalworked", "");
				entry.put("weektotal", "");
				entry.put("totaldriving", "");
				entry.put("equips", "");
				entry.put("headquarters", "");
				tc.put(entry);

				date = date.plusDays(1);
			}

			// total entry
			TimecardEntry totalentry = new TimecardEntry();
			totalentry.put("dutydate", "");
			totalentry.put("dutyday", "");
			totalentry.put("starttime", "");
			totalentry.put("endtime", "");
			totalentry.put("totalworked", TOTAL_STR);
			totalentry.put("weektotal", "0.00");
			totalentry.put("totaldriving", "0.00");
			totalentry.put("equips", "");
			totalentry.put("headquarters", "");
			tc.put(totalentry);

			// save to DB
			Map<String, String> map = new HashMap<String, String>();
			map.put(year + "-" + month, tc.toString());
			String resp = RedisPoolManager.hmset(driverid + timecard_suffix, map);
			if(!resp.equals("OK")){
				status = new StatusMessage(driverid, "ERROR: Error while creating timecard !");
			}			
		}
		
		return status;
		
	}

	// Timecard -is JSONArray
	public Timecard getTimecard(String driverid, String year, String month) {

		Timecard tc = new Timecard();

		// DB Call
		Map<String, String> dgmap = RedisPoolManager.hgetAll(driverid + timecard_suffix);

		String tcvalue = dgmap.get(year + tk + month);

		// if exist for this month
		if (tcvalue != null) {
			
			tc = new Timecard(tcvalue);

			// calculate the weektotal, based on settings
			double weektotal = 0;
			double monthtotal = 0;
			double monthdrivingtotal = 0;

			for (int i = 0; i < tc.length(); i++) {

				TimecardEntry entry = new TimecardEntry(tc.get(i).toString());

				String totalworked = entry.get("totalworked").toString();

				// working hours
				if (totalworked.length() > 0 && (!totalworked.equals(TOTAL_STR))) {
					weektotal = weektotal + Float.parseFloat(totalworked);
					monthtotal = monthtotal + Float.parseFloat(totalworked);
				}

				if (entry.get("dutyday").equals("Fri")) {
					entry.put("weektotal", weektotal);
					tc.put(i, entry);
					weektotal = 0;
				}

				// driving hours
				String totaldriving = entry.get("totaldriving").toString();

				if (totaldriving.length() > 0) {
					monthdrivingtotal = monthdrivingtotal + Float.parseFloat(totaldriving);

					// System.out.println("monthdrivingtotal:"+
					// monthdrivingtotal );

				}

				if (totalworked.trim().equals(TOTAL_STR)) {
					entry.put("weektotal", monthtotal); // special row, note
														// keys would not match
														// as you think
					entry.put("totaldriving", monthdrivingtotal);
					tc.put(i, entry);
				}

			} // for

		}

		return tc;
	}

	// Ex: "2:45" to 2.75
	private float hourstodecimal(String hourmin) {

		if (hourmin.indexOf(":") > 0) {
			String h = hourmin.split(":")[0];

			String m = hourmin.split(":")[1];
			if (m.equals("15"))
				m = "25";
			else if (m.equals("30"))
				m = "50";
			else if (m.equals("45"))
				m = "75";

			return Float.parseFloat(h + "." + m);
		} else {
			return 0;
		}

	}

	// Ex: 2.75 to "2:45"
	private String decimaltohours(double hourmin) {

		String hm = String.valueOf(hourmin).replace(".", ":");

		// String h[] = hm.split(":");

		if (hm.indexOf(":") > 0) {
			String h = hm.split(":")[0];
			String m = hm.split(":")[1];

			if (m.equals("0"))
				m = "00";
			else if (m.equals("25"))
				m = "15";
			else if (m.equals("5")) // float be never .50, would be treated as
									// .5 only
				m = "30";
			else if (m.equals("75"))
				m = "45";

			return (h + ":" + m);
		}

		return "0";
	}

	public TimecardList getTimecardAll(String driverid, String year) {

		TimecardList tclist = new TimecardList();

		// DB Call
		Map<String, String> dgmap = RedisPoolManager.hgetAll(driverid + timecard_suffix);

		dgmap.forEach((k, v) -> {

			System.out.println("k:" + k);

			// timecard entries stored as 2016-7
			if (k.startsWith(year) && (!k.contains("isnodrivingduties"))) {
				JSONObject tclistentry = new JSONObject();
				tclistentry.put("month", k.substring(k.indexOf("-") + 1));
				// tclistentry.put("timecard", v); //it is JSONArray

				Timecard tc = new Timecard(v);
				double monthtotal = 0;
				CSV equips = new CSV();
				for (int i = 0; i < tc.length(); i++) {
					TimecardEntry entry = new TimecardEntry(tc.get(i).toString());
					String totalworked = entry.get("totalworked").toString();
					// calc
					if (totalworked.length() > 0 && (!totalworked.equals(TOTAL_STR))) {
						monthtotal = monthtotal + Float.parseFloat(totalworked);
					}
					// equips
					if (entry.get("equips").toString().length() > 0) {
						System.out.println(entry.get("equips"));
						equips.append(entry.get("equips").toString());
					}
				} // for

				tclistentry.put("monthtotalworked", monthtotal);
				tclistentry.put("equips", equips.getValues());

				tclist.put(tclistentry);
			}
		});

		System.out.println("data:" + tclist);

		return tclist;
	}

	public TimecardList getTimecardForAllDrivers(String companyid, String year, String month) throws Exception {

		DriverList drivers = new DriverService().getDriverAll(companyid);

		TimecardList tclist = new TimecardList(); // it is JSONArray

		drivers.forEach(d -> {

			Driver driver = (Driver) d;

			// DB Call
			Map<String, String> dgmap = RedisPoolManager.hgetAll(driver.get("id") + timecard_suffix);

			String tcvalue = dgmap.get(year + tk + month);

			if (tcvalue != null) {
				JSONObject tclistentry = new JSONObject();
				tclistentry.put("driver", driver.getFirstname());
				tclistentry.put("timecard", new Timecard(tcvalue)); // it is
																	// JSONArray

				tclist.put(tclistentry);
			}

		});

		return tclist;
	}

	public StatusMessage saveTimecardEntry(String driverid, String yearmonth, String dutydate,
			String timecardentrydata, String loginemail) {

		// LocalDate dutydate = NuUtil.toLocateDate(selecteddutydate);

		StatusMessage status = new StatusMessage(dutydate, "Time card created successfully!");

		// Ex: 2016-7
		// String yearmonth = dutydate.getYear() + "-" +
		// dutydate.getMonthValue();

		System.out.println("yearmonth:" + yearmonth);

		Map<String, String> dgmap = RedisPoolManager.hgetAll(driverid + timecard_suffix);

		for (String k : dgmap.keySet()) {
			if (k.equals(yearmonth)) {
				Timecard tc = new Timecard(dgmap.get(k));

				for (int i = 0; i < tc.length(); i++) {
					TimecardEntry entry = new TimecardEntry(tc.get(i).toString());
					System.out.println("dutydate:" + entry.get("dutydate") + ", dutydate1:" + dutydate);
					if (entry.get("dutydate").equals(dutydate)) {
						// System.out.println("*** MATCH FOUND ***");
						// tc.remove(i);
						
						TimecardEntry newentry = new TimecardEntry(timecardentrydata);

						// check the totalhours, to check entry already made
						if(entry.get("totalworked").toString().length() > 0){
							//record change log
							String starttime = entry.getString("starttime");
							String endtime = entry.getString("endtime");
							String totaldriving = entry.getString("totaldriving");
							try {
								Double totalworked = Double.parseDouble(entry.get("totalworked").toString());
								
								Double newtotalworked = Double.parseDouble(newentry.get("totalworked").toString());
								
								//System.out.println("starttime:"+ starttime + ", endtime:"+ endtime + ", totaldriving:"+ totaldriving);
	
								newentry.put("prev_starttime", starttime);
								newentry.put("prev_endtime", endtime);
								newentry.put("prev_totaldriving", totaldriving);
								newentry.put("change_totalworked", (totalworked - newtotalworked));
								newentry.put("updatedby", loginemail);
							}catch(Exception e){
								e.printStackTrace(); //just audit, any error keep moving :O)
							}
						}
						
						newentry.put("dutydate", entry.get("dutydate"));
						newentry.put("dutyday", entry.get("dutyday"));
						tc.put(i, newentry);

						status = new StatusMessage(dutydate, "Time card updated successfully!");
						break;
					}
				} // for

				if (tc.length() > 0) {
					Map<String, String> map = new HashMap<String, String>();
					map.put(yearmonth, tc.toString());

					String OK = RedisPoolManager.hmset(driverid + timecard_suffix, map);
					System.out.println("Timecard creation:" + OK);
				}
			}
		}

		return status;
	}

	public TimecardAddInfo getTimecardAdditionalInfo(String driverid, String year, String month) {

		TimecardAddInfo tc = new TimecardAddInfo();

		// DB Call
		Map<String, String> dgmap = RedisPoolManager.hgetAll(driverid + timecard_suffix);

		String ischecked = dgmap.get(year + tk + month + tk + "isnodrivingduties");

		if (ischecked == null || ischecked.equals("false")) {
			tc.put("isnodrivingduties", "false");
		} else {
			tc.put("isnodrivingduties", "true");
		}

		return tc;
	}

	public StatusMessage saveTimecardAdditionalInfo(String driverid, String yearmonth, String addinfodata) {

		StatusMessage status = new StatusMessage(yearmonth, "Time card updated successfully!");

		TimecardAddInfo ai = new TimecardAddInfo(addinfodata);

		String isnodrivingduties = ai.get("isnodrivingduties").toString();

		Map<String, String> map = new HashMap<String, String>();
		map.put(yearmonth + tk + "isnodrivingduties", isnodrivingduties);

		String OK = RedisPoolManager.hmset(driverid + timecard_suffix, map);

		System.out.println("saveTimecardAdditionalInfo:" + OK);

		// TODO Auto-generated method stub
		return status;

	}

	public TimecardList getTimecardForWholeYear(String companyid, String year) throws Exception {

		DriverList drivers = new DriverService().getDriverAll(companyid);

		TimecardList tclist = new TimecardList(); // it is JSONArray of
													// JSONArray

		drivers.forEach(d -> {
			Driver driver = (Driver) d;
			// DB Call
			Map<String, String> dgmap = RedisPoolManager.hgetAll(driver.get("id") + timecard_suffix);

			for (int month = 1; month <= 12; month++) {
				String tcvalue = dgmap.get(year + tk + month);
				if (tcvalue != null) {

					Timecard timecard = new Timecard(tcvalue);

					for (int i = 0; i < timecard.length(); i++) {
						TimecardEntry entry = new TimecardEntry(timecard.get(i).toString());
						if (entry.get("equips").toString().length() > 0) {
							JSONObject tclistentry = new JSONObject();
								tclistentry.put("driver", driver.getFirstname() + "," + driver.getLastname());
								tclistentry.put("month", month);
								tclistentry.put("starttime", entry.get("starttime"));
								tclistentry.put("endtime", entry.get("endtime"));
								tclistentry.put("equips", entry.get("equips"));
								tclistentry.put("totaldriving", entry.get("totaldriving"));
								tclistentry.put("dutydate", entry.get("dutydate"));
								try{
									tclistentry.put("prev_starttime", entry.get("prev_starttime"));
									tclistentry.put("prev_endtime", entry.get("prev_endtime"));
									tclistentry.put("prev_totaldriving", entry.get("prev_totaldriving"));
									tclistentry.put("change_totalworked", entry.get("change_totalworked"));
									tclistentry.put("updatedby", entry.get("updatedby"));
								}catch(JSONException e){ //may be no entry found
									System.out.println("JSONException:"+ e.getLocalizedMessage() );
								}
								tclist.put(tclistentry);
						}
					} // for
				}
			}
		});

		return tclist;
	}

}

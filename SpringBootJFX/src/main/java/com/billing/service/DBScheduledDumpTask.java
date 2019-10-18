package com.billing.service;

import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * Task to Periodically take DB backup based on configured time period
 */
@Component
public class DBScheduledDumpTask extends TimerTask {
	
	@Autowired
	DBBackupService dbBackupService;
	
	public void run() {
		System.out.println("-- Running scheduled DB Dump Task --");
		dbBackupService.createDBDump(null);
	}
}
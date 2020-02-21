// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.importer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public class UnassignUserToProjectMassCSVImporter {

	private SimpleCSVImporter csvImporter = new SimpleCSVImporter();
	private DeveloperAdministration administration;


	public UnassignUserToProjectMassCSVImporter(DeveloperAdministration administration) {
		this.administration=administration;
	}

	public void importUsersFromProjectUnassignmentsByCSV(File file) throws IOException {
		List<CSVRow> rows = csvImporter.importCSVFile(file, 2, 1);

		for (CSVRow row: rows) {
			importRow(row);
		}

	}

	private void importRow(CSVRow row) {
		Iterator<CSVColumn> it = row.columns.iterator();
		String projectId = it.next().cell.trim();
		String users = it.next().cell.trim();

		if (users.isEmpty()) {
			return;
		}
		for (String userId: users.split(",")) {
			administration.unassignUserFromProject(userId.trim(), projectId);
		}
	}

}

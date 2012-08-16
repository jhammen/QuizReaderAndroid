/*
 This file is part of QuizReader.

 QuizReader is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 QuizReader is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with QuizReader.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quizreader.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class BaseDao {

	protected QRDatabaseHelper databaseHelper;
	protected SQLiteDatabase db;

	public BaseDao(Context context) {
		databaseHelper = new QRDatabaseHelper(context);
	}

	public void open() {
		db = databaseHelper.getWritableDatabase();
	}

	public void open(SQLiteDatabase database) {
		db = database;
	}

	public SQLiteDatabase getDatabase() {
		return db;
	}

	public void close() {
		databaseHelper.close();
	}

}

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class QRDatabaseHelper extends SQLiteOpenHelper {

	private Context context;

	public QRDatabaseHelper(Context context) {
		super(context, "QuizReader", null, 1);
		this.context = context;
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (String sql : loadScripts(context, "create.sql")) {
			System.out.println("loading sql: " + sql);
			db.execSQL(sql);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		throw new RuntimeException("upgrade not yet implemented");
		// TODO Auto-generated method stub
	}

	private String[] loadScripts(Context context, String scriptName) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			AssetManager assetManager = context.getAssets();
			byte buf[] = new byte[2048];
			InputStream is = assetManager.open(scriptName);
			int len;
			while ((len = is.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			baos.close();
			is.close();
			return baos.toString().split(";");
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

}

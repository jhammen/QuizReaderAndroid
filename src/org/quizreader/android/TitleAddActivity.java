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
package org.quizreader.android;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.quizreader.android.database.Title;
import org.quizreader.android.database.TitleDao;
import org.quizreader.android.qzz.FileUtil;
import org.quizreader.android.qzz.QzzFile;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TitleAddActivity extends ListActivity {

	private TitleDao titleDao;
	private List<File> quizFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title_add);
		// load all file paths
		Set<String> openFiles = new HashSet<String>();
		titleDao = new TitleDao(this);
		titleDao.open();
		for (Title title : titleDao.getAllTitles()) {
			openFiles.add(title.getFilepath());
		}
		titleDao.close();
		// show all files in the download folder
		File downloadDir = FileUtil.getDownloadDir(); // inline this
		if (downloadDir != null) { // no media - error handling?

			quizFiles = new ArrayList<File>();
			for (File testFile : downloadDir.listFiles()) {
				if (testFile.getName().endsWith(".qzz")) {
					if (!openFiles.contains(testFile.getAbsolutePath())) {
						quizFiles.add(testFile);
					}
				}
			}

			ArrayAdapter<File> fileList = new ArrayAdapter<File>(this, R.layout.title_add_row, quizFiles);
			setListAdapter(fileList);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		final File file = quizFiles.get(position);
		try {
			final QzzFile qzzFile = new QzzFile(file);
			LoadDefinitionsTask loadTitleDefsTask = new LoadDefinitionsTask(this, null) {

				@Override
				protected void setup() {
					title = new Title();
					title.setName(qzzFile.getTitle());
					title.setAuthor(qzzFile.getAuthor());
					title.setLanguage("fr");
					title.setFilepath(file.getAbsolutePath());
					long titleId = TitleDao.insertTitle(db, title);
					title.setId(Long.toString(titleId));
				}

				@Override
				protected void onPostExecute(Integer result) {
					super.onPostExecute(result);
					finish(); // activity is done when task is
				}

			};
			loadTitleDefsTask.execute(qzzFile.getCommonDefinitionReader());
		} catch (Exception ex) {
			ex.printStackTrace();
			// TODO: handle
		}
	}
}

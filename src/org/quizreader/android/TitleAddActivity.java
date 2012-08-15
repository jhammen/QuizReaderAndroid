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
		// first add title to db
		final Title title = new Title();
		titleDao.open();
		title.setName("Title goes here");
		title.setLanguage("fr");
		title.setFilepath(file.getAbsolutePath());
		titleDao.saveTitle(title);
		titleDao.close();

		LoadDefinitionsTask loadTitleDefsTask = new LoadDefinitionsTask(this, title) {

			@Override
			protected void onCancelled() {
				titleDao.open();
				titleDao.deleteTitle(title.getId());
				titleDao.close();
			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				finish();
			}

		};
		try {
			QzzFile qzzFile = new QzzFile(file);
			loadTitleDefsTask.execute(qzzFile.getCommonDefinitionReader());
		} catch (Exception ex) {
			ex.printStackTrace();
			// TODO: handle
		}
	}
}

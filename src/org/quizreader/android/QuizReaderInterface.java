package org.quizreader.android;

import java.util.List;

import org.quizreader.android.database.Definition;
import org.quizreader.android.database.DefinitionDao;
import org.quizreader.android.database.Title;

import android.content.Context;
import android.widget.Toast;

public class QuizReaderInterface {
	Context context;
	Title title;
	DefinitionDao defDao;

	QuizReaderInterface(Context c, Title t) {
		context = c;
		title = t;
		defDao = new DefinitionDao(c);
	}

	// @JavascriptInterface
	public void showDef(String word) {
		defDao.open();
		List<Definition> definitions = defDao.getDefinitions(title.getId(), word, title.getLanguage());
		defDao.close();
		for (Definition def : definitions) {
			Toast.makeText(context, def.getText(), Toast.LENGTH_SHORT).show();
		}
	}
}
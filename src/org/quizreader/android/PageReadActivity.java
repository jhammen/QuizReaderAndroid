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

import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quizreader.android.database.Definition;
import org.quizreader.android.database.DefinitionDao;
import org.quizreader.android.database.Word;
import org.quizreader.android.database.WordDao;
import org.quizreader.android.qzz.QzzFile;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PageReadActivity extends BaseQuizReadActivity {

	public static final int RESULT_END_TITLE = RESULT_FIRST_USER;

	private WordDao wordDao;

	/** Called when the activity is first created. */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_read);
		WebView webview = (WebView) findViewById(R.id.webView);
		webview.setWebViewClient(new QRWebViewClient());
		webview.setWebChromeClient(new WebChromeClient() {
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.d("QuizReader", cm.message() + ", line " + cm.lineNumber() + " " + cm.sourceId());
				return true;
			}
		});
		WebSettings webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webview.addJavascriptInterface(new QuizReaderInterface(), "qr");
		try {
			wordDao = new WordDao(this);
			QzzFile qzzFile = new QzzFile(title.getFilepath(), this);
			URL htmlURL = qzzFile.getHTML(title.getId(), title.getSection());
			if (htmlURL == null) { // beyond the last section
				setResult(RESULT_END_TITLE);
				finish();
			}
			String externalForm = htmlURL.toExternalForm();
			webview.loadUrl(externalForm + "?paragraph=" + title.getParagraph());
		} catch (Exception ex) {
			webview.loadData(ex.toString(), "text/plain", null);
		}
	}

	public class QuizReaderInterface {
		DefinitionDao defDao = new DefinitionDao(PageReadActivity.this);

		// @JavascriptInterface
		public void showDef(String token) {
			wordDao.open();
			Word word = wordDao.getWord(token, title.getLanguage());
			wordDao.close();
			for (Definition def : word.getDefinitions()) {
				Toast.makeText(PageReadActivity.this, def.getText(), Toast.LENGTH_SHORT).show();
			}
			// drop quiz level!
		}

		public int getQuizLevel(String word) {
			return (int) Math.floor(Math.random() * 12);
		}

		public String getEntry(String token) throws JSONException {
			wordDao.open();
			List<Word> words = wordDao.getWordAndRoots(token, title.getLanguage());
			wordDao.close();
			JSONArray ret = new JSONArray();
			for (Word word : words) {
				JSONObject json = new JSONObject();
				json.put("word", word.getToken());
				json.put("level", word.getQuizLevel());
				// add definitions
				JSONArray jsonArray = new JSONArray();
				for (Definition def : word.getDefinitions()) {
					JSONObject defObj = new JSONObject();
					// defObj.put("type", definition.get);
					defObj.put("text", def.getText());
					jsonArray.put(defObj);
				}
				json.put("defs", jsonArray);
				ret.put(json);
			}
			return ret.toString();
		}

		public void updateQuizLevel(String token, int delta) {
			// increment from the current level
		}

		public void updateParagraph(int number) {
			// TODO: increment paragraph and save in db
		}

		public void finish() {
			PageReadActivity.this.finish();
		}
	}

	private class QRWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if ("www.quizreader.org".equals(Uri.parse(url).getHost())) {
				return false;
			}
			// do nothing
			return true;
		}
	}
}

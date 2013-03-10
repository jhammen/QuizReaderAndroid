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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;

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
	private Random random;

	private QzzFile qzzFile;

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
			random = new Random();
			wordDao = new WordDao(this);
			qzzFile = new QzzFile(title.getFilepath(), this);
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
			Toast.makeText(PageReadActivity.this, definitionString(word), Toast.LENGTH_SHORT).show();
		}

		private StringBuilder definitionString(Word word) {
			StringBuilder defString = new StringBuilder();
			for (int i = 0; i < word.getDefinitions().size(); i++) {
				Definition def = word.getDefinitions().get(i);
				if (i != 0) {
					defString.append("; ");
				}
				defString.append(def.getText());
			}
			return defString;
		}

		// @JavascriptInterface
		public String getEntries(String token) throws JSONException {
			wordDao.open();
			List<Word> words = wordDao.getWordAndRoots(token, title.getLanguage());
			wordDao.close();
			JSONArray ret = new JSONArray();
			for (Word word : words) {
				JSONObject json = wordToJson(word);
				ret.put(json);
			}
			return ret.toString();
		}

		private JSONObject wordToJson(Word word) throws JSONException {
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
			return json;
		}
		
		public String getSimilarEntry(String token) throws JSONException {
			int attempts = 0;
			while (attempts++ < 5) {
				int randIndex = token.length() > 1 ? random.nextInt(token.length() - 1) : 0;
				String like = token.charAt(0) + "%" + token.charAt(randIndex) + "%";
				wordDao.open();
				Word quizWordLike = wordDao.getQuizWordLike(title.getLanguage(), token, like);
				wordDao.close();
				if (quizWordLike != null) {
					return wordToJson(quizWordLike).toString();
				}
			}
			return getUnrelatedDefinition(token, token);
		}

		public String getUnrelatedDefinition(String token, String token2) throws JSONException {
			wordDao.open();
			Word randomWord = wordDao.getRandomQuizWord(title.getLanguage(), token, token2);
			wordDao.close();
			return wordToJson(randomWord).toString();
		}

		// @JavascriptInterface
		public void updateQuizLevel(String token, int level) {
			wordDao.open();
			wordDao.updateLevel(token, level);
			wordDao.close();
		}

		// @JavascriptInterface
		public void updateParagraph(int index) {
			title.setParagraph(index);
			updateTitle();
		}

		// @JavascriptInterface
		public void endPage() throws IOException {
			URL htmlURL = qzzFile.getHTML(title.getId(), title.getSection());
			if (htmlURL == null) { // beyond the last section
				setResult(RESULT_END_TITLE);
			}
			else {
				title.setSection(title.getSection() + 1);
				title.setParagraph(0);
				updateTitle();
			}
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

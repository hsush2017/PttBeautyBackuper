package choYM.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import choYM.Util.HtmlUtil;

/**
 * 備份表特板照片
 * 
 * @author choYM
 * @since 2018-01-20
 *
 */
public class BackupHandler {
	/**
	 * 設定檔路徑
	 */
	private static final String CONFIG_PATH = "config.properties";

	/**
	 * 表特版URL
	 */
	private static final String BEAUTY_URL = "https://www.ptt.cc/bbs/Beauty/index.html";

	/**
	 * imgur圖片空間網址正則
	 */
	private static final String IMG_URL_REGEX = "^https?://(i.)?(m.)?imgur.com/.*.jpg$";

	/**
	 * 文章內容網址前綴
	 */
	private static final String PTT_PREFIX = "https://www.ptt.cc/";

	/**
	 * 是否備份上一頁
	 */
	private boolean previousPage = true;

	Properties prop = null;

	public void start() {
		// 讀取參數檔
		this.prop = this.readConfig();

		// 備份
		if (this.prop != null) {
			try {
				this.doBackup();
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 讀取config設定檔
	 * 
	 * @return
	 * @throws IOException
	 */
	private Properties readConfig() {
		Properties p = null;
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;

		try {
			fileInputStream = new FileInputStream(new File(CONFIG_PATH));
			inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");

			// load properties
			p = new Properties();

			p.load(inputStreamReader);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return p;
	}

	/**
	 * 執行備份
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	private void doBackup() throws MalformedURLException, IOException, ParseException {
		Date backupDate = new Date();
		String currentPageUrl = BEAUTY_URL;

		System.out.println("備份開始");
		do {
			Document currentPage = HtmlUtil.loadHtml(currentPageUrl); // 開啟標題頁
			Elements posts = this.getPosts(currentPage); // 取得文章列表
			posts = this.filter(posts, backupDate); // 取出使用者感興趣的文章列表

			// 讀取每篇文章, 下載圖片
			for (Element post : posts) {
				// 讀取頁面
				Document page = HtmlUtil.loadHtml(PTT_PREFIX + post.getElementsByTag("a").attr("href"));

				// 資料夾若存在, 表示先前已經下載過, 跳過; 否則建立資料夾
				String filePath = this.generateFilePath(page);
				File f = new File(filePath);
				if (!f.exists()) {
					f.mkdirs();
				} else {
					continue;
				}

				// 取得imgur超連結, 進行下載
				System.out.println("備份........." + post.getElementsByClass("title").first().text());
				Elements links = page.getElementsByAttributeValueMatching("href", Pattern.compile(IMG_URL_REGEX));
				for (int i = 0; i < links.size(); i++) {
					this.startDownload(links.get(i).attr("href"), f, String.valueOf(i + 1));
				}
			}

			// 上一頁
			currentPageUrl = PTT_PREFIX
					+ currentPage.getElementsByClass("btn-group-paging").first().child(1).attr("href");
		} while (this.previousPage);
		System.out.println("備份結束");
	}

	/**
	 * 下載圖片
	 * 
	 * @param href
	 *            圖片位址
	 * @param dir
	 *            資料夾路徑
	 * @param fileName
	 *            檔名
	 * @throws IOException
	 */
	private void startDownload(String href, File dir, String fileName) throws IOException {
		URL url = new URL(href);
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;

		while (-1 != (n = in.read(buf))) {
			out.write(buf, 0, n);
		}
		out.close();
		in.close();

		FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + "\\" + String.valueOf(fileName) + ".jpg");
		fos.write(out.toByteArray());
		fos.close();
	}

	/**
	 * 產生檔案路徑
	 * 
	 * @param post
	 *            某篇文章內容
	 * @return
	 * @throws ParseException
	 */
	private String generateFilePath(Document post) throws ParseException {
		Elements headers = post.getElementsByClass("article-metaline");

		// 日期
		Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US)
				.parse(headers.last().children().last().text());
		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
		String timeStr = new SimpleDateFormat("HHmmss").format(date);

		// 文章標題
		String article = headers.get(1).getElementsByClass("article-meta-value").first().text()
				.replaceAll("[:\\\\/*?|<>]", "_");

		StringBuilder sb = new StringBuilder(this.prop.getProperty("save_path"));
		sb.append(dateStr + File.separator);
		sb.append(timeStr + "_" + article);
		
		return sb.toString();
	}

	/**
	 * 取得文章列表
	 * 
	 * @param document
	 *            當前頁面
	 * @return
	 */
	private Elements getPosts(Document document) {
		Elements posts = document.getElementsByClass("r-list-container").first().children();
		
		// 移除公告區的文章
		for (int i = 0; i < posts.size(); i++) {
			if ("r-list-sep".equals(posts.get(i).attr("class"))) {
				posts = new Elements(posts.subList(0, i));
				break;
			}
		}

		// 反轉文章列表, 依照發布時間排序
		Collections.reverse(posts);

		return posts;
	}

	/**
	 * 根據使用者設定篩選文章
	 * 
	 * @param backupDate
	 *            備份時間
	 * @param prop
	 *            設定檔
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private Elements filter(Elements posts, Date backupDate) throws MalformedURLException, IOException, ParseException {
		Elements e = new Elements();

		for (Element post : posts) {
			if (this.isValidPost(post, backupDate)) {
				e.add(post);
			}
		}

		return e;
	}

	/**
	 * 檢查文章是否符合使用者條件
	 * 
	 * @param post
	 *            文章標題資訊
	 * @param backupDate
	 *            備份時間
	 * @param prop
	 *            設定檔
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws ParseException
	 */
	private boolean isValidPost(Element post, Date backupDate)
			throws MalformedURLException, IOException, ParseException {
		// 作者
		String propAuthor = prop.getProperty("author").trim();
		String postAuthor = post.getElementsByClass("author").text();
		if (!propAuthor.isEmpty() && !propAuthor.equals(postAuthor)) {
			return false;
		}

		// 關鍵字
		String propKeyword = prop.getProperty("title_key_word").trim();
		String title = post.getElementsByTag("a").text();
		if (!propKeyword.isEmpty() && !title.contains(propKeyword)) {
			return false;
		}

		// 推文數
		int propPushAmount = prop.getProperty("push_amount").isEmpty() ? 0
				: Integer.parseInt(this.prop.getProperty("push_amount"));
		int pushAmount = 0;
		String pushStr = post.getElementsByClass("hl").text();
		if (pushStr.isEmpty())
			pushAmount = 0;
		else if (pushStr.contains("X"))
			pushAmount = -1;
		else if (pushStr.contains("爆"))
			pushAmount = 100;
		else
			pushAmount = Integer.parseInt(pushStr);
		if (pushAmount < propPushAmount) {
			return false;
		}

		// 若發文時間超過備份時間24小時以上視為invalid
		if (!post.getElementsByTag("a").isEmpty()) {
			Document d = HtmlUtil.loadHtml(PTT_PREFIX + post.getElementsByTag("a").attr("href"));
			String postDateStr = d.getElementsByClass("article-metaline").last().children().last().text();
			Date postDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US).parse(postDateStr);
			long diff = backupDate.getTime() - postDate.getTime();
			long diffDays = diff / (24 * 60 * 60 * 1000);

			if (diffDays >= 1) {
				this.previousPage = false;
			}

			return diffDays < 1;
		} else {
			return false;
		}
	}
}

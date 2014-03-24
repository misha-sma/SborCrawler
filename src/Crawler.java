import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Crawler {
	private static final String BASE_URL = "http://www.all-sbor.net";

	public static void main(String[] args) {
		String url = "http://www.all-sbor.net/board/cat_29.html?page=1&pagestyle=collapsed&stronpage=50&part=2";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
			httpGet.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
			httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpGet.setHeader("User-Agent",
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
			CloseableHttpResponse response = httpClient.execute(httpGet);
			try {
				System.out.println(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String html = EntityUtils.toString(entity, "cp1251");
				System.out.println("html=" + html);
				EntityUtils.consume(entity);
				int pagesCount = getPagesCount(html);
				System.out.println("pagesCount=" + pagesCount);
				List<String> urls = getAdUrls(html);
				System.out.println("urls=" + urls);
				System.out.println("urls count=" + urls.size());
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void dowloadUrls(List<String> urls, CloseableHttpClient httpClient) {
		try {
			for (String url : urls) {
				HttpGet httpGet = new HttpGet(url);
				httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
				httpGet.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
				httpGet.setHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				httpGet.setHeader("User-Agent",
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
				CloseableHttpResponse response;
				response = httpClient.execute(httpGet);
				try {
					System.out.println(response.getStatusLine());
					HttpEntity entity = response.getEntity();
					String html = EntityUtils.toString(entity, "cp1251");
					System.out.println("html=" + html);
					EntityUtils.consume(entity);
					String text = parseText(html);
				} finally {
					response.close();
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String parseText(String html) {
		return "";
	}

	private static int getPagesCount(String text) {
		int index = text.lastIndexOf("&amp;pagestyle=collapsed&amp;stronpage=50&amp;part=2\">");
		if (index == -1) {
			return -1;
		}
		int index2 = text.lastIndexOf('=', index);
		String pagesCountStr = text.substring(index2 + 1, index);
		return Integer.parseInt(pagesCountStr);
	}

	private static List<String> getAdUrls(String text) {
		List<String> urls = new LinkedList<String>();
		String[] dirtyUrls = text.split("<a href=\"/board/details_");
		for (int i = 1; i < dirtyUrls.length; ++i) {
			int index = dirtyUrls[i].indexOf("\">");
			if (index == -1) {
				continue;
			}
			String title = parseForPrefixWithDelimeter(dirtyUrls[i], "\">", "</a>");
			if (!isTitleTrue(title)) {
				continue;
			}
			String url = BASE_URL + "/board/details_" + dirtyUrls[i].substring(0, index);
			urls.add(url);
		}
		return urls;
	}

	private static boolean isTitleTrue(String title) {
		title = title.toLowerCase();
		if (title.contains("диски") || title.contains("комплект") || title.contains("генератор")
				|| title.contains("радиатор") || title.contains("сабуфер") || title.contains("запчасти")
				|| title.contains("стартер") || title.contains("шины") || title.contains("резину")) {
			return false;
		}
		return true;
	}

	private static String parseForPrefixWithDelimeter(String text, String prefix, String delimeter) {
		int prefixIndex = text.indexOf(prefix);
		if (prefixIndex == -1) {
			return "";
		}
		int delimeterIndex = text.indexOf(delimeter, prefixIndex + prefix.length());
		if (delimeterIndex == -1) {
			return "";
		}
		return text.substring(prefixIndex + prefix.length(), delimeterIndex);
	}
}

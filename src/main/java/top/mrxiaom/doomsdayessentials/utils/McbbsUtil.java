package top.mrxiaom.doomsdayessentials.utils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个很简单的库，目前只用来获取帖子操作
 * 
 * @author MrXiaoM
 */
public class McbbsUtil {
	public static enum Operation {
		// 暂不完整，欢迎补全
		CLOSE("关闭"),
		MOVE("移动"),
		HIGH_LIGHT("设置高亮"),
		ADD_ICON("添加图标"),
		ADD_STAMP("添加图章"),
		BE_ESSENCED("加入精华"),
		UP_SERVER("提升(服务器/交易代理提升卡)"),
		UP("提升(提升卡)"),
		UNKNOWN("[ MrXiaoM \n Product ]");

		final String displayText;

		Operation(String displayText) {
			this.displayText = displayText;
		}

		public String getDisplayText() {
			return displayText;
		}

		public static Operation getOperation(String value) {
			for (Operation o : Operation.values()) {
				if (o.displayText.contains(value)) {
					return o;
				}
			}
			return Operation.UNKNOWN;
		}
	}

	public static class ThreadOperation {
		final String uid;
		final String name;
		final Operation operation;
		final String operationString;
		final String time;
		final String term;

		public String getUid() {
			return uid;
		}

		public String getName() {
			return name;
		}

		public String getOperationString() {
			return operationString;
		}

		public String getTime() {
			return time;
		}

		public String getTerm() {
			return term;
		}

		private ThreadOperation(String uid, String name, String operation, String time, String term) {
			super();
			this.uid = uid;
			this.name = name;
			this.operationString = operation;
			this.operation = Operation.getOperation(operation);
			this.time = time;
			this.term = term;
		}

		@Override
		public String toString() {
			return "thread{uid=" + uid + ",name=" + name + ",time=" + time + ",operation="
					+ operation.name().toUpperCase() + ", operationString=" + operationString + ",term=" + term + "}";
		}

		public Operation getOperation() {
			return operation;
		}
	}

	public static List<ThreadOperation> getThreadOperation(String tid) throws IOException {
		return getThreadOperation(tid, "UTF-8");
	}

	public static List<ThreadOperation> getThreadOperation(String tid, String charset) throws IOException {
		List<ThreadOperation> list = new ArrayList<>();
		URLConnection connection = new URL("https://www.mcbbs.net/forum.php?mod=misc&action=viewthreadmod&tid=" + tid)
				.openConnection();
		connection.setRequestProperty("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36 Homo/114514.1919810 Edg/92.0.902.78");
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
		String line;
		StringBuilder content = new StringBuilder();
		String uid = "";
		String name = "";
		String time = "";
		String operation = "";
		String term = "";
		while ((line = reader.readLine()) != null)
			content.append(line);
		int i, j = 0, l = 0;
		if ((i = content.indexOf("<table class=\"list\"")) < 0)
			return list;
		content = new StringBuilder(content.substring(i, content.indexOf("</table>", i)).replace("<td >", "<td>"));
		i = content.indexOf("<td>");
		while (i >= 0) {
			i += 4;
			if (j > 3) {
				String text = content.substring(i, content.indexOf("</td>", i));
				if (text.contains("<a href=\"") && text.contains("uid=")) {
					// uid 和 用户名
					int k = text.indexOf("uid=") + 4;
					uid = text.substring(k, text.indexOf("\"", k));
					name = text.substring(text.indexOf(">") + 1, text.indexOf("</a>"));
					l++;
				} else if (text.contains("<span title=\"")) {
					// 特殊时间处理 (如“N小时前”的详细时间获取)
					int k = text.indexOf("<span title=\"") + 13;
					time = text.substring(k, text.indexOf("\"", k));
				} else {
					// 时间
					if (l % 5 == 2)
						time = text;
					// 操作
					if (l % 5 == 3)
						operation = text;
					if (l % 5 == 4) {
						// 时限
						term = text;
						list.add(new ThreadOperation(uid, name, operation, time, term));
						uid = name = operation = time = "";
					}
				}
				l++;
			}
			i = content.indexOf("<td>", i);
			j++;
		}
		return list;
	}

	public static class UserInfo {
		final String uid;
		final String username;
		final String userGroup;
		final String onlineTime;
		final String regTime;
		final String lastVisit;
		final String lastActiveTime;
		final String lastPostTime;
		final String timeRegion;
		final String usedSpace;
		final int goldGrain;
		final int emerald;
		final int netherStar;
		final int contribution;
		final int love;
		final int diamond;

		private UserInfo(String uid, String username, String userGroup, String onlineTime, String regTime,
				String lastVisit, String lastActiveTime, String lastPostTime, String timeRegion, String usedSpace,
				int goldGrain, int emerald, int netherStar, int contribution, int love, int diamond) {
			this.uid = uid;
			this.username = username;
			this.userGroup = userGroup;
			this.onlineTime = onlineTime;
			this.regTime = regTime;
			this.lastVisit = lastVisit;
			this.lastActiveTime = lastActiveTime;
			this.lastPostTime = lastPostTime;
			this.timeRegion = timeRegion;
			this.usedSpace = usedSpace;
			this.goldGrain = goldGrain;
			this.emerald = emerald;
			this.netherStar = netherStar;
			this.contribution = contribution;
			this.love = love;
			this.diamond = diamond;
		}

		public String toString() {
			return "UserInfo{uid=" + uid + ",username=" + username + ",userGroup=" + userGroup + ",onlineTime="
					+ onlineTime + ",regTime=" + regTime + ",lastVisit=" + lastVisit + ",lastActiveTime="
					+ lastActiveTime + "," + "lastPostTime=" + lastPostTime + ",timeRegion=" + timeRegion
					+ ",usedSpace=" + usedSpace + ",goldGrain=" + goldGrain + "," + "emerald=" + emerald
					+ ",netherStar=" + netherStar + ",contribution=" + contribution + ",love=" + love + ",diamond="
					+ diamond + "}";
		}

		public boolean isPromotionUserGroup() {
			return userGroup.contains("Lv.0") || userGroup.contains("Lv.1") || userGroup.contains("Lv.2")
					|| userGroup.contains("Lv.3") || userGroup.contains("Lv.4") || userGroup.contains("Lv.5")
					|| userGroup.contains("Lv.6") || userGroup.contains("Lv.7") || userGroup.contains("Lv.8")
					|| userGroup.contains("Lv.9") || userGroup.contains("Lv.10") || userGroup.contains("Lv.11")
					|| userGroup.contains("Lv.12");
		}

		public String getUserGroup() {
			return userGroup;
		}

		public String getOnlineTime() {
			return onlineTime;
		}

		public String getRegTime() {
			return regTime;
		}

		public String getLastVisit() {
			return lastVisit;
		}

		public String getLastActiveTime() {
			return lastActiveTime;
		}

		public String getLastPostTime() {
			return lastPostTime;
		}

		public String getTimeRegion() {
			return timeRegion;
		}

		public String getUsedSpace() {
			return usedSpace;
		}

		public int getGoldGrain() {
			return goldGrain;
		}

		public int getEmerald() {
			return emerald;
		}

		public int getNetherStar() {
			return netherStar;
		}

		public int getContribution() {
			return contribution;
		}

		public int getLove() {
			return love;
		}

		public int getDiamond() {
			return diamond;
		}

		public String getUid() {
			return uid;
		}

		public String getUsername() {
			return username;
		}

	}

	@Nullable
	public static UserInfo getUserInfoUid(String uid) throws IOException {
		return getUserInfoUid(uid, "UTF-8");
	}

	@Nullable
	public static UserInfo getUserInfoUid(String uid, String charset) throws IOException {
		return getUserInfo(uid, true, charset);
	}

	@Nullable
	public static UserInfo getUserInfoUsername(String name) throws IOException {
		return getUserInfoUsername(name, "UTF-8");
	}

	@Nullable
	public static UserInfo getUserInfoUsername(String name, String charset) throws IOException {
		return getUserInfo(name, false, charset);
	}

	@Nullable
	public static UserInfo getUserInfo(String value, boolean isUid, String charset) throws IOException {
		URLConnection connection = new URL("https://www.mcbbs.net/home.php?mod=space&" + (isUid ? "uid=" : "username=")
				+ value + "&do=profile&from=space").openConnection();
		connection.setRequestProperty("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36 Homo/114514.1919810 Edg/92.0.902.78");
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
		String line;
		StringBuilder content = new StringBuilder();
		while ((line = reader.readLine()) != null)
			content.append(line);
		if (content.toString().contains("<title>提示信息 -") && !content.toString().contains("的个人资料"))
			return null;
		String showUid = content.substring(content.indexOf("(UID: ") + 6,
				content.indexOf(")", content.indexOf("(UID: ")));
		String username = content.substring(content.indexOf("<title>") + 7, content.indexOf("的个人资料"));
		int i = content.lastIndexOf("<h2 class=\"mbn\">活跃概况</h2>");
		String userGroup = "";
		String onlineTime = "";
		String regTime = "";
		String lastVisit = "";
		String lastActiveTime = "";
		String lastPostTime = "";
		String timeRegion = "";
		String usedSpace = "";
		String goldGrain = "";
		String emerald = "";
		String netherStar = "";
		String contribution = "";
		String love = "";
		String diamond = "";
		if (i >= 0) {

			String content2 = content.substring(i, content.indexOf("</div>", i));
			i = content2.indexOf("<li>");
			while (i >= 0) {
				i += 4;
				String text = content2.substring(i, content2.indexOf("</li>", i));
				if (text.contains("用户组")) {
					// System.out.println(text);
					int k = text.lastIndexOf("target=\"_blank\">");
					if (k < 0)
						continue;
					userGroup = text.substring(k + 16, text.indexOf("</a>", k));
				} else {
					int k = text.indexOf("</em>");
					String temp = text.substring(k + 5);
					if (text.contains("<em>在线时间</em>")) {
						onlineTime = temp;
					}
					if (text.contains("<em>注册时间</em>")) {
						regTime = temp;
					}
					if (text.contains("<em>最后访问</em>")) {
						lastVisit = temp;
					}
					if (text.contains("<em>上次活动时间</em>")) {
						lastActiveTime = temp;
					}
					if (text.contains("<em>上次发表时间</em>")) {
						lastPostTime = temp;
					}
					if (text.contains("<em>所在时区</em>")) {
						timeRegion = temp;
					}
				}
				i = content2.indexOf("<li>", i);
			}
		}
		if ((i = content.lastIndexOf("<h2 class=\"mbn\">统计信息</h2>")) >= 0) {
			String content2 = content.substring(i, content.indexOf("</div>", i));
			i = content2.indexOf("<li>");
			while (i >= 0) {
				i += 4;
				String text = content2.substring(i, content2.indexOf("</li>", i));
				int k = text.indexOf("</em>");
				String temp = text.substring(k + 5).contains(" ") ? text.substring(k + 5).split(" ")[0]
						: text.substring(k + 5);
				if (text.contains("<em>已用空间</em>")) {
					usedSpace = temp;
				}
				if (text.contains("<em>金粒</em>")) {
					goldGrain = temp;
				}
				if (text.contains("<em>宝石</em>")) {
					emerald = temp;
				}
				if (text.contains("<em>下界之星</em>")) {
					netherStar = temp;
				}
				if (text.contains("<em>贡献</em>")) {
					contribution = temp;
				}
				if (text.contains("<em>爱心</em>")) {
					love = temp;
				}
				if (text.contains("<em>钻石</em>")) {
					diamond = temp;
				}
				i = content2.indexOf("<li>", i);
			}
		}
		return new UserInfo(showUid, username, userGroup, onlineTime, regTime, lastVisit, lastActiveTime, lastPostTime,
				timeRegion, usedSpace, strToInt(goldGrain, 0), strToInt(emerald, 0), strToInt(netherStar, 0),
				strToInt(contribution, 0), strToInt(love, 0), strToInt(diamond, 0));
	}

	public static int strToInt(String s, int nullReturnValue) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			return nullReturnValue;
		}
	}
}

package top.mrxiaom.doomsdayessentials;

import java.util.LinkedList;

public class TPSMonitor {

	// NeverLag TPSWatcher START
	protected long lastPoll = System.currentTimeMillis();
	private final LinkedList<Double> history = new LinkedList<>();

	public void updateTps() {
		final long startTime = System.currentTimeMillis();
		try {
			long timeSpent = startTime - lastPoll;
			if (timeSpent <= 0) {
				return;
			}
			if (history.size() > 50 * 10) { // 历史记录保留10秒
				history.poll();
			}
			double tps = calcuateTPS(timeSpent);
			if (tps < 21) {
				history.add(tps);
			}
		} finally {
			lastPoll = startTime;
		}
	}

	// 求上个 10 秒内服务器的平均TPS
	public double getAverageTPS() {
		double avg = 0.0D;
		for (Double tps : this.history) {
			if (tps != null) {
				avg += tps;
			}
		}
		return avg / history.size();
	}

	public double getTps() {
		return history.getLast();
	}

	protected double calcuateTPS(long interval) {
		return 1D / (interval / 1000D);
	}
	// NeverLag TPSWatcher END
}

package top.mrxiaom.doomsdayessentials.utils;

import top.mrxiaom.doomsdayessentials.Main;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class RedstoneCounter {

	// 保存红石每触发的次数
	private int syncRestoneCount;
	private final AtomicInteger asyncRestoneCount = new AtomicInteger(0);

	// 记录一分钟内的红石触发次数
	private final ConcurrentLinkedDeque<Integer> asyncOneMinutesRecord = new ConcurrentLinkedDeque<>();
	private final ReentrantLock asyncLock = new ReentrantLock();
	private final LinkedList<Integer> syncOneMinutesRecord = new LinkedList<>();

	final Main plugin;

	public RedstoneCounter(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				if (asyncOneMinutesRecord.size() >= 60) { // 双重检查锁定
					asyncLock.lock();
					try {
						if (asyncOneMinutesRecord.size() >= 60) {
							asyncOneMinutesRecord.removeFirst();
						}
					} finally {
						asyncLock.unlock();
					}
				}
				asyncOneMinutesRecord.add(asyncRestoneCount.getAndSet(0));

			}
		}, 20L, 20L);
		plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				if (syncOneMinutesRecord.size() >= 60) {
					syncOneMinutesRecord.removeFirst();
				}
				syncOneMinutesRecord.add(syncRestoneCount);
				syncRestoneCount = 0;

			}
		}, 20L, 20L);
	}

	public int getRedstoneAvgCount(boolean forceSync) {
		Deque<Integer> deque = enterThreadSafe(forceSync);
		try {
			if (deque.isEmpty()) {
				return 0;
			}
			int total = 0;
			for (Integer count : deque) {
				total += count;
			}
			return total / 60;
		} finally {
			leaveThreadSafe(forceSync);
		}
	}

	public int getRedstoneRealTimeCount(boolean forceSync) {
		Deque<Integer> deque = enterThreadSafe(forceSync);
		try {
			if (deque.isEmpty()) {
				return 0;
			}
			return deque.getLast();
		} finally {
			leaveThreadSafe(forceSync);
		}
	}

	private Deque<Integer> enterThreadSafe(boolean forceSync) {
		Deque<Integer> deque;
		if (forceSync) {
			deque = syncOneMinutesRecord;
		} else {
			deque = asyncOneMinutesRecord;
			asyncLock.lock();
		}
		return deque;
	}

	private void leaveThreadSafe(boolean forceSync) {
		try {
			if (!forceSync)
				asyncLock.unlock();
		} catch (IllegalMonitorStateException ex) {
			// ignore
		}
	}

	public void updateRedstoneCount(boolean forceSync) {
		if (forceSync) {
			syncRestoneCount++;
		} else {
			asyncRestoneCount.incrementAndGet();
		}
	}

}
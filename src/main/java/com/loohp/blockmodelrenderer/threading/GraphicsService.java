package com.loohp.blockmodelrenderer.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GraphicsService {
	
	public static final int DEFAULT_KEEP_ALIVE = 3;
	private static ThreadPoolExecutor service;
	
	public static synchronized void start(int keepAliveTime) {
		if (service == null || service.isShutdown()) {
			ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("BlockModelRenderer Graphics Processing Thread #%d").build();
			service = (ThreadPoolExecutor) Executors.newFixedThreadPool(8, factory);
			if (keepAliveTime > 0) {
				service.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);
				service.allowCoreThreadTimeOut(true);
			}
		}
	}
	
	public static <T> Future<T> execute(Callable<T> task) {
		start(DEFAULT_KEEP_ALIVE);
		return service.submit(task);
	}
	
	public static Future<?> execute(Runnable task) {
		start(DEFAULT_KEEP_ALIVE);
		return service.submit(task);
	}
	
	public static synchronized void shutdown() {
		service.shutdown();
	}

}

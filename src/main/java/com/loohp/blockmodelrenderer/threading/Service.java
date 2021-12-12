package com.loohp.blockmodelrenderer.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class Service {
	
	private static final ExecutorService SERVICE;
	
	static {
		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("BlockModelRenderer Graphics Processing Thread #%d").build();
		SERVICE = Executors.newFixedThreadPool(8, factory);
	}
	
	public static <T> Future<T> execute(Callable<T> task) {
		return SERVICE.submit(task);
	}
	
	public static Future<?> execute(Runnable task) {
		return SERVICE.submit(task);
	}

}

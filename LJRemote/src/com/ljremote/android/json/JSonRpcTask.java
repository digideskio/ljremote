package com.ljremote.android.json;

import android.os.AsyncTask;

public abstract class JSonRpcTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>{
	
	private LJClientService ljService;

	void bind(LJClientService service){
		this.ljService= service;
	}
	
	public <T> T getClientProxy(Class<T> proxyInterface) throws Exception{
		return ljService == null ? null : ljService.getClientProxy(proxyInterface);
	}
	
}


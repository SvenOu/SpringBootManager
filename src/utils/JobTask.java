package utils;

import javafx.concurrent.Task;

public abstract class JobTask<V> extends Task<V>{
    private V v;

    @Override
    protected V call() throws Exception {
        onPreCall();
        v = onCall();
        return v;
    }

    @Override
    protected void done() {
        super.done();
        onDone(v);
    }

    protected void onPreCall(){

    };

    public abstract V  onCall();

    protected void onDone(V v){

    };

    public Task excuteJob() {
        JobExecutor.getInstance().execute(this);
        return this;
    }
}

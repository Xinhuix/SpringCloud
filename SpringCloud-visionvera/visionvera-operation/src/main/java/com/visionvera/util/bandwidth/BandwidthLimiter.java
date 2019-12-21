package com.visionvera.util.bandwidth;


public class BandwidthLimiter {
	
	/* KB */
    private static Long KB = 1024l;

    /* The smallest count chunk length in bytes */
    private static Long CHUNK_LENGTH = 1024l * 10;

    /* How many bytes will be sent or receive */
    private int bytesWillBeSentOrReceive = 0;

    /* When the last piece was sent or receive */
    private long lastPieceSentOrReceiveTick = System.nanoTime();

    
    /* Default rate is 1024KB/s */
    private int maxRate = 1024;

    /* Time cost for sending CHUNK_LENGTH bytes in nanoseconds, 1秒读或写所使用的时间，再转换成纳秒 */
    private long timeCostPerChunk = (1000000000l * CHUNK_LENGTH) / (this.maxRate * KB);
    //private long timeCostPerChunk = (CHUNK_LENGTH / (this.maxRate * KB)) * 1000000000l;
    
    private String currentThreadIp = "";
    
    public BandwidthLimiter() {
        
    }
    /**
     * Initialize a BandwidthLimiter object with a certain rate.
     * 
     * @param maxRate
     *            the download or upload speed in KBytes
     */
    public BandwidthLimiter(int maxRate) {
        this.setMaxRate(maxRate);
    }
    
    public void setCurrentThreadIp(String ip) {
    	this.currentThreadIp = ip;
    }
    
    private synchronized void resetTimeCostPerChunk(){
    	//获取当前正在执行的下载进程数量
    	int threadCount = BandwidthUtils.getDownloadCount(this.currentThreadIp);
    	int maxRate = BandwidthUtils.getDownLoadLimit(BandwidthUtils.MAX_RATE);
    	maxRate = maxRate <= 0 ? (1 * 1024) : (maxRate * 1024);
    	if(this.maxRate != maxRate) {
    		this.maxRate = maxRate;
    	}
    	if(threadCount > 1) {
    		this.timeCostPerChunk = (1000000000l * CHUNK_LENGTH) / (this.maxRate * KB / threadCount);
    	}else{
    		this.timeCostPerChunk = (1000000000l * CHUNK_LENGTH) / (this.maxRate * KB);
    	}
    }

    /**
     * Set the max upload or download rate in KB/s. maxRate must be grater than
     * 0. If maxRate is zero, it means there is no bandwidth limit.
     * 
     * @param maxRate
     *            If maxRate is zero, it means there is no bandwidth limit.
     * @throws IllegalArgumentException
     */
    public synchronized void setMaxRate(int maxRate)
            throws IllegalArgumentException {
        if (maxRate < 0) {
            throw new IllegalArgumentException("maxRate can not less than 0");
        }
        this.maxRate = maxRate < 0 ? 0 : maxRate;
        if (maxRate == 0){
            this.timeCostPerChunk = 0;
        }else{
            this.timeCostPerChunk = (1000000000l * CHUNK_LENGTH) / (this.maxRate * KB);
        }
    }

    /**
     * Next 1 byte should do bandwidth limit.
     */
    public synchronized void limitNextBytes() {
        this.limitNextBytes(1);
    }

	/**
	 * Next len bytes should do bandwidth limit<br> 
	 * 原理：当发送或接收的字节数超过指定阀值(CHUNK_LENGTH)时，自动让读或写暂停: <br>
	 * 1）当发送字节数(bytesWillBeSentOrReceive)花费的时间没有超过timeCostPerChunk时，表示正常，
	 * 不让其进入睡眠状态； <br>
	 * 2）当发送字节数(bytesWillBeSentOrReceive)花费的时间超过timeCostPerChunk时，则让线程进入睡眠状态； <br><br>
	 * <font color=red>网上找个一个模型：</font> <br>
	 * 简易模型可以这么做（假定期望控制速度为 100KB/s）： <br>
	 * ◎ 获取当前时间戳 current； <br>
	 * ◎ 计算当前时间距离上次读取时间实际间隔多少ms； <br>
	 * ◎ 计算这段间隔时间内，按目标控制速度，应读取的字节数； <br>
	 * ◎ 将当前时间戳 current 时间设置给 上次读取时间戳 last <br>
	 * ◎ Sleep 10ms； <br>
	 * ◎ 循环到最前面
	 * 
	 * @param len
	 */
    public synchronized void limitNextBytes(int len) {
        this.bytesWillBeSentOrReceive += len;
        //System.out.println("bytesWillBeSentOrReceive==========="+bytesWillBeSentOrReceive);
        /* We have sent CHUNK_LENGTH bytes */
        while (this.bytesWillBeSentOrReceive > CHUNK_LENGTH) {
        	this.resetTimeCostPerChunk();
            long nowTick = System.nanoTime();
            //计算发送CHUNK_LENGTH字节的数据流花费的时间是否在正确的范围(timeCostPerChunk)内
            long missedTime = this.timeCostPerChunk - (nowTick - this.lastPieceSentOrReceiveTick);
            //System.out.println("===missedTime==============="+missedTime+"========timeCostPerChunk======"+timeCostPerChunk);
            if (missedTime > 0) {
                try {
                    Thread.sleep(missedTime / 1000000, (int) (missedTime % 1000000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.bytesWillBeSentOrReceive -= CHUNK_LENGTH;
            this.lastPieceSentOrReceiveTick = nowTick + (missedTime > 0 ? missedTime : 0);
        }
    }
}

package com.visionvera.util.bandwidth;

import java.io.IOException;
import java.io.InputStream;
/**
 * 下载控制类
 * @ClassName: DownloadLimiter   
 * @author chenting
 * @date 2017年3月8日 
 *
 */
public class InputStreamLimiter extends InputStream {
	private InputStream is = null;
    private BandwidthLimiter bandwidthLimiter = null;
    
    public InputStreamLimiter(InputStream is, BandwidthLimiter bandwidthLimiter)
    {
        this.is = is;
        this.bandwidthLimiter = bandwidthLimiter;
    }
    @Override
    public int read() throws IOException {
        if(this.bandwidthLimiter != null)
            this.bandwidthLimiter.limitNextBytes();
        return this.is.read();
    }

    public int read(byte b[], int off, int len) throws IOException
    {
        if (bandwidthLimiter != null)
            bandwidthLimiter.limitNextBytes(len);
        return this.is.read(b, off, len);
    }
}

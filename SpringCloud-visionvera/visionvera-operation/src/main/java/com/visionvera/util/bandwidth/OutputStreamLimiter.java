package com.visionvera.util.bandwidth;

import java.io.IOException;
import java.io.OutputStream;
/**
 * 上传控制类
 * @ClassName: UploadLimiter   
 * @author chenting
 * @date 2017年3月8日 
 *
 */
public class OutputStreamLimiter extends OutputStream {
	private OutputStream os = null;
    private BandwidthLimiter bandwidthLimiter = null;
    
    public OutputStreamLimiter(OutputStream os, BandwidthLimiter bandwidthLimiter)
    {
        this.os = os;
        this.bandwidthLimiter = bandwidthLimiter;
    }
    
    @Override
    public void write(int b) throws IOException {
        if (bandwidthLimiter != null)
            bandwidthLimiter.limitNextBytes();
        this.os.write(b);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        if (bandwidthLimiter != null)
            bandwidthLimiter.limitNextBytes(len);
        this.os.write(b, off, len);
    }

}

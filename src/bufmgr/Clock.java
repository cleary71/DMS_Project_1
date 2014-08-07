/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bufmgr;

import static bufmgr.FrameDescriptor.State.*;

/**
 *
 * @author Ward
 */
public class Clock extends Replacer{
    
    int count;
    protected FrameDescriptor[] frametab;
               
    public Clock(BufMgr bufferManager) {
        super(bufferManager);
        this.frametab = bufferManager.frametab;
        if (bufferManager.frametab[0]== null)
            bufferManager.initializeFrametab();
       
     System.out.println("Clock bufmgr is " + bufferManager.frametab[0] + " " + frametab);
         count = 0;
        setUp();
     }
    
    public void setUp() {
        for(int i = 0; i < frametab.length; i++)
            frametab[i] = new FrameDescriptor();
    }
    
    @Override
    public void newPage(FrameDescriptor fdesc) {
        fdesc.setState(PINNED);
    }

    @Override
     public void freePage(FrameDescriptor fdesc) {
        fdesc.setState(AVAILABLE);        
    }
    
    @Override
    public void pinPage(FrameDescriptor fdesc) {
        //if (frameNo < 0 || frameNo > mgr.getNumBuffers()) {
           // throw new InvalidFrameNumberException(null,
	//				"CLOCK::pin Invalid frame Number");
        //}

        if (fdesc.getState() == AVAILABLE || fdesc.getState() == REFERENCED) {
            fdesc.setState(PINNED);
        }
       
    }

    @Override
    public void unpinPage(FrameDescriptor fdesc) {
        //if (frameNo < 0 || frameNo > mgr.getNumBuffers()) {
            //throw new InvalidFrameNumberException(null,
	//				"CLOCK::pin Invalid frame Number");
        //}

        
    }
 
    @Override
    public int pickVictim() {
        for(int i = 0; i < frametab.length * 2; i++){
            System.out.println("frametab is " + frametab + " count is " + count);
            if (frametab[count].getState()== AVAILABLE) {
                return count;
            } else if (frametab[count].getPinCount()== 0) {
                if (frametab[count].getState() == REFERENCED) {
                    frametab[count].setState(AVAILABLE);
                } else {
                    return count;
                }
            }
            count++;
            count = count % frametab.length;
        }
        return -1;
    }
}

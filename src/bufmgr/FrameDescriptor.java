/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bufmgr;

import static bufmgr.FrameDescriptor.State.AVAILABLE;
import global.PageId;

/**
 *
 * @author Ward
 */
public class FrameDescriptor {
    public enum State {
        AVAILABLE,
        PINNED,
        REFERENCED
    }
    private State state;
    private int pinCount;
    private boolean dirty;
    private PageId pageId;
    private int frameNumber;

    public FrameDescriptor() {
        state = AVAILABLE;
        pinCount = 0;
        dirty = false;
        pageId = new PageId();
        frameNumber = 0;
    }
    
    public FrameDescriptor(PageId id, int frame) {
        
    }
    
    public void incrementPinCount() {
        pinCount++;
    }
    
    public void decrementPinCount() {
        pinCount--;
    }
    
    public State getState() {
        return state;
    }

    public int getPinCount() {
        return pinCount;
    }

    public boolean isDirty() {
        return dirty;
    }

    public PageId getPageId() {
        return pageId;
    }

    public int getFrameNumber() {
        return frameNumber;
    }
 
    public void setState(State state) {
        this.state = state;
    }

    public void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setPageId(PageId pageId) {
        this.pageId = pageId;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    
}

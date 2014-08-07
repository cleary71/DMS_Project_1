package bufmgr;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * <h3>Minibase Buffer Manager</h3>
 * The buffer manager manages an array of main memory pages.  The array is
 * called the buffer pool, each page is called a frame.  
 * It provides the following services:
 * <ol>
 * <li>Pinning and unpinning disk pages to/from frames
 * <li>Allocating and deallocating runs of disk pages and coordinating this with
 * the buffer pool
 * <li>Flushing pages from the buffer pool
 * <li>Getting relevant data
 * </ol>
 * The buffer manager is used by access methods, heap files, and
 * relational operators.
 */
public class BufMgr implements GlobalConst {   
    FrameDescriptor[] frametab = null;										
    Hashtable<Integer, FrameDescriptor> pageFrameTable = null;
    Page[] bufferPool = null;
    int numframes = 0;
    Clock clock = null;

  /**
   * Constructs a buffer manager by initializing member data.  
   * 
   * @param numframes number of frames in the buffer pool
   */
  public BufMgr(int numframes) {
      this.numframes = numframes;
      frametab = new FrameDescriptor[numframes];
      bufferPool = new Page[numframes];
      pageFrameTable = new Hashtable<>();
      clock = new Clock(this);
  }
  
  public void initializeFrametab() {
    for(int i = 0; i < frametab.length; i++)
        frametab[i] = new FrameDescriptor();
    if (bufferPool[0]== null){
        for(int i = 0; i < numframes; i++)
            bufferPool[i] = new Page();
    }
        
  }

  /**
   * The result of this call is that disk page number pageno should reside in
   * a frame in the buffer pool and have an additional pin assigned to it, 
   * and mempage should refer to the contents of that frame. <br><br>
   * 
   * If disk page pageno is already in the buffer pool, this simply increments 
   * the pin count.  Otherwise, this<br> 
   * <pre>
   * 	uses the replacement policy to select a frame to replace
   * 	writes the frame's contents to disk if valid and dirty
   * 	if (contents == PIN_DISKIO)
   * 		read disk page pageno into chosen frame
   * 	else (contents == PIN_MEMCPY)
   * 		copy mempage into chosen frame
   * 	[omitted from the above is maintenance of the frame table and hash map]
   * </pre>		
   * @param pageno identifies the page to pin
   * @param mempage An output parameter referring to the chosen frame.  If
   * contents==PIN_MEMCPY it is also an input parameter which is copied into
   * the chosen frame, see the contents parameter. 
   * @param contents Describes how the contents of the frame are determined.<br>  
   * If PIN_DISKIO, read the page from disk into the frame.<br>  
   * If PIN_MEMCPY, copy mempage into the frame.<br>  
   * If PIN_NOOP, copy nothing into the frame - the frame contents are irrelevant.<br>
   * Note: In the cases of PIN_MEMCPY and PIN_NOOP, disk I/O is avoided.
   * @throws IllegalArgumentException if PIN_MEMCPY and the page is pinned.
   * @throws IllegalStateException if all pages are pinned (i.e. pool is full)
   */
  public void pinPage(PageId pageno, Page mempage, int contents) {

    FrameDescriptor frame = pageFrameTable.get(pageno.pid);
    Page page;
    if (frame != null) {
        frametab[frame.getFrameNumber()].incrementPinCount();
        mempage.setPage(bufferPool[frame.getFrameNumber()]);
        //returnPageInfo(pageno, frame);
    } else {
        int frameNumber = clock.pickVictim();
        
        if (frameNumber != -1) {
            FrameDescriptor victimFrame = pageFrameTable.get(frameNumber);

            if (victimFrame != null && victimFrame.isDirty()) {
                try {
                    flushPage(victimFrame.getPageId());
                } catch (Exception e) {
                    //throw new BufMgrException(e,"BufrMgr::pinPage: page number not found in flushPage");
                }
            }
            
            if (contents == PIN_MEMCPY) {
                if(bufferPool[frameNumber]==null)
                    bufferPool[frameNumber] = new Page();
                bufferPool[frameNumber].copyPage(mempage);
                mempage.setPage(bufferPool[frameNumber]);
                frametab[frameNumber].incrementPinCount();
                frametab[frameNumber].setState(FrameDescriptor.State.PINNED);
                frametab[frameNumber].setPageId(pageno);
            }
            
            if (contents == PIN_DISKIO) {
                page = bufferPool[pageno.pid];
                Minibase.DiskManager.read_page(pageno, page);
                bufferPool[frameNumber].copyPage(page);
                mempage.setPage(bufferPool[frameNumber]);
                frametab[frameNumber].incrementPinCount();
                frametab[frameNumber].setPageId(pageno);
            }
 
            if (pageFrameTable.replace(pageno.pid, frametab[frameNumber]) == null)
                    pageFrameTable.put(pageno.pid, frametab[frameNumber]);

        }
    }

  } // public void pinPage(PageId pageno, Page page, int contents)
  
  /**
   * Unpins a disk page from the buffer pool, decreasing its pin count.
   * 
   * @param pageno identifies the page to unpin
   * @param dirty UNPIN_DIRTY if the page was modified, UNPIN_CLEAN otherwise
   * @throws IllegalArgumentException if the page is not in the buffer pool
   *  or not pinned
   */
  public void unpinPage(PageId pageno, boolean dirty) {
    FrameDescriptor frame = pageFrameTable.get(pageno.pid);

    if (frame != null) {
        if (frame.getPinCount() == 0) {
            System.out.println("Throw exception");
        } else {
            frame.decrementPinCount();
            frame.setDirty(dirty);
        }
    } else {
        System.out.println("Throw exception");
    }
 } // public void unpinPage(PageId pageno, boolean dirty)
  
  /**
   * Allocates a run of new disk pages and pins the first one in the buffer pool.
   * The pin will be made using PIN_MEMCPY.  Watch out for disk page leaks.
   * 
   * @param firstpg input and output: holds the contents of the first allocated page
   * and refers to the frame where it resides
   * @param run_size input: number of pages to allocate
   * @return page id of the first allocated page
   * @throws IllegalArgumentException if firstpg is already pinned
   * @throws IllegalStateException if all pages are pinned (i.e. pool exceeded)
   */
  public PageId newPage(Page firstpg, int run_size) {
      if(getUnpinnedCount()==0)
          throw new IllegalStateException();
      
      PageId pageNumber = Minibase.DiskManager.allocate_page(run_size);
      FrameDescriptor frame = pageFrameTable.get(pageNumber.pid);
      if (frame != null || frame.getPinCount() > 0 )
          System.out.println(" new Page exception");// throw new IllegalArgumentException();
      
      pinPage(pageNumber, firstpg, PIN_MEMCPY);
      
      return pageNumber;
   } // public PageId newPage(Page firstpg, int run_size)

  public int getUnpinnedCount() {
      int unpinned = 0;
      for(FrameDescriptor frame: frametab) {
          if(frame.getPinCount() == 0) {
              unpinned++;
          }
      }
      return unpinned;
  }
  /**
   * Deallocates a single page from disk, freeing it from the pool if needed.
   * 
   * @param pageno identifies the page to remove
   * @throws IllegalArgumentException if the page is pinned
   */
  public void freePage(PageId pageno) {

    FrameDescriptor frame = pageFrameTable.get(pageno.pid);

    if (frame != null && frame.getPinCount() > 0) {
        throw new IllegalArgumentException();
    } else {
        Minibase.DiskManager.deallocate_page(pageno);
    }
  } // public void freePage(PageId firstid)

  /**
   * Write all valid and dirty frames to disk.
   * Note flushing involves only writing, not unpinning or freeing
   * or the like.
   * 
   */

  /**
   * Write a page in the buffer pool to disk, if dirty.
   * 
   * @throws IllegalArgumentException if the page is not in the buffer pool
   */
public void flushPage(PageId pageno) {
	  
    FrameDescriptor frame = pageFrameTable.get(pageno);

    if (frame != null) {
        if (frame.isDirty()) {
            try {
                Minibase.DiskManager.write_page(pageno, bufferPool[frame.getFrameNumber()]);
            } catch (Exception e) {
                //throw new BufMgrException(e, "BufrMgr::flushPage: page cant be freed by diskmanager");
            }
        }
        frame.setDirty(false);
    } else {
        //throw new IllegalArgumentException();
    }	    
}
  
  public void flushAllPages() {
    for (FrameDescriptor frame: frametab) {
        flushPage(frame.getPageId());
    }    
  }

   /**
   * Gets the total number of buffer frames.
   */
  public int getNumFrames() {
    return numframes;
  }
  
  public int getNumBuffers() {
      return numframes;
  }

  /**
   * Gets the total number of unpinned buffer frames.
   */
  public int getNumUnpinned() {
    int unpinned = 0;
    PageId pageId= new PageId();
    for (int i = 0; i < pageFrameTable.size(); i++) {
        pageId.pid = i;
        FrameDescriptor frame = pageFrameTable.get(pageId);
        if (frame != null && frame.getPinCount() == 0) {
            unpinned++;
        } else if (frame == null) {
            unpinned++;
        }
    }
    return unpinned;
  }

} // public class BufMgr implements GlobalConst

package bufmgr;

import global.GlobalConst;

/**
 * Base class for buffer pool replacement policies.
 */
abstract class Replacer implements GlobalConst {

  /** Reference back to the buffer manager's frame table. */
  protected FrameDescriptor[] frametab;

  // --------------------------------------------------------------------------

  /**
   * Constructs the replacer, given the buffer manager.
   */
  protected Replacer(BufMgr bufmgr) {
      System.out.println(" Replacer bufmgr is " + bufmgr);
    this.frametab = bufmgr.frametab;
  }

  /**
   * Notifies the replacer of a new page.
   */
  public abstract void newPage(FrameDescriptor fdesc);

  /**
   * Notifies the replacer of a free page.
   */
  public abstract void freePage(FrameDescriptor fdesc);

  /**
   * Notifies the replacer of a pined page.
   */
  public abstract void pinPage(FrameDescriptor fdesc);

  /**
   * Notifies the replacer of an unpinned page.
   */
  public abstract void unpinPage(FrameDescriptor fdesc);

  /**
   * Selects the best frame to use for pinning a new page.
   * 
   * @return victim frame number, or -1 if none available
   */
  public abstract int pickVictim();

} // abstract class Replacer implements GlobalConst

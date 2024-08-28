package com.distocraft.dc5000.install.ant;

/**
 * Provides a BufferedReader with a readLine method that
 * blocks for only a specified number of seconds. If no
 * input is read in that time, a specified default
 * string is returned. 
 * @author eharpyl
 */
public class TimedBufferedReader extends java.io.BufferedReader {

  private boolean linefeed        = true;
  private int   timeout     = 0;
  private String  defaultString   = "";

  /**
   * TimedBufferedReader constructor.
   * @param in java.io.Reader
   */
  TimedBufferedReader(java.io.Reader in) {
    super(in);
  }

  /**
   * TimedBufferedReader constructor.
   * @param in java.io.Reader
   * @param sz int Size of the input buffer.
   */
  TimedBufferedReader(java.io.Reader in, int sz) {
    super(in, sz);
  }

  /** 
   * Defines whether feedback is shown to user or not.
   * @param show boolean
   */
  public void setUserFeedback(boolean show) {
    linefeed = show;
  }
  /**
   * Sets number of seconds to block for input.
   * @param seconds int
   */
  public void setTimeout(int seconds) {
    timeout=seconds;
  }

  /**
   * Sets defaultString to use if no input is read.
   * @param str java.lang.String
   */
  public void setDefaultString(String str) {
    defaultString = str;
  }

  public String readLine() throws java.io.IOException {
    int msec = 0;
    int sec  = 0;
    while (!this.ready()) {
      try { 
        Thread.sleep(10); 
      } catch (InterruptedException e) { 
        break; 
      }
      if (msec > 99) {
        sec++;
        msec = 0;
      } else {
        msec++;
        if (sec >= timeout) {
          if (linefeed) {
            System.out.print("\nRequest timeouted. Default "+defaultString+ " is used.");
          }
          return defaultString;
        }
      }
    }
    return super.readLine();
  }
}
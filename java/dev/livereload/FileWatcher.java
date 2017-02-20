package dev.livereload;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author salai
 * @see http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */
// TODO make a start/stop/join method
public class FileWatcher implements Runnable {
	
  private static final Logger LOG = Logger.getLogger(FileWatcher.class.getName());
  
  private final WatchService _watcher;
  private final Map<WatchKey, Path> _keys;
  private final Path _docroot;
  
  private final AtomicBoolean _running = new AtomicBoolean(false);

  public WebSocketHandler listener;
  
  private List<Pattern> _patterns;

  // register the dir to watch
  public FileWatcher(Path docroot) throws Exception {
    _docroot = docroot;
    this._watcher = docroot.getFileSystem().newWatchService();
    this._keys = new HashMap<WatchKey, Path>();

    // register directory and sub-directories    
    registerAll(_docroot);
  }
  
  //Register the given directory, and all its sub-directories, with the WatchService.
  private void registerAll(final Path start) throws IOException {
    // register directory and sub-directories
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
    	  		WatchKey key = dir.register(_watcher, ENTRY_MODIFY); //ENTRY_DELETE, ENTRY_CREATE - watch only modify
    	  			_keys.put(key, dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void notify(String path) throws Exception {
    if (_patterns != null) {
      for (Pattern p : _patterns) {
        LOG.finer("Testing pattern: " + p + " against string: " + path);
        if (p.matcher(path).matches()) {
          LOG.fine("Skipping file: " + path + " thanks to pattern: " + p);
          return;
        }
      }
    }
    LOG.fine("File " + path + " changed, triggering refresh");
    //WebSocketHandler l = listener;
    if (this.listener != null) {
    	this.listener.notifyChange(path);
    }
  }

  @SuppressWarnings("unchecked")
  static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

 
  //start the thread
  void startWatcher(WebSocketHandler lsnr) throws Exception {
	  
	this.listener = lsnr;  
	  
    if (_running.compareAndSet(false, true)) {
    	
      System.out.println("In startWatcher loop...");  
    	
      Thread t = new Thread(this);
      //t.setDaemon(true);
      t.start();
    }
  }

  void stopWatcher() throws Exception {
    _running.set(false);
    _watcher.close();
  }

  /**
   * Process all events for keys queued to the watcher
   *
   * @throws Exception
   */
  @Override
  public void run() {
	  
	System.out.println("In run..."+_running.get());
	
    try {
      while (_running.get()) {
    	  
        // wait for key to be signaled
        WatchKey key = _watcher.take();
        
        Path dir = _keys.get(key);
        if (dir == null) {
          System.err.println("WatchKey not recognized!!");
          continue;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();
          // TBD - provide example of how OVERFLOW event is handled
          if (kind == OVERFLOW) {
            continue;
          }

          // Context for directory entry event is the file name of entry
          WatchEvent<Path> ev = cast(event);
          Path name = ev.context();
          Path child = dir.resolve(name);

          //System.out.println("In name:"+ name);
          //System.out.println("In child:"+ child);
          //System.out.println("In kind:"+ kind);
          
          //ignore file watching on these file types
          //if the file no extension, like mvr consent upload
          //ignore - driversign canvas json
          if( name.toString().endsWith(".json") || name.toString().endsWith(".pdf") || name.toString().endsWith(".png") || name.toString().endsWith(".jpg") || name.toString().endsWith(".css") || (name.toString().indexOf(".")==-1) ){
        	  
        	  System.out.println(name +": Do not notify");
        	  
          } else {
	          if (kind == ENTRY_MODIFY) {
	            notify(_docroot.relativize(child).toString());
	          } 
          }
          	//do not watch CREATE
          	//else if (kind == ENTRY_CREATE) {
            // if directory is created, and watching recursively, then
            // register it and its sub-directories
            //if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
            //  registerAll(child);
            //}
           //}
        }

        // reset key and remove from set if directory no longer accessible
        boolean valid = key.reset();
        if (!valid) {
          _keys.remove(key);

          // all directories are inaccessible
          if (_keys.isEmpty()) {
            break;
          }
        }
      }
      
    } catch (InterruptedException | ClosedWatchServiceException exc) {
      // stop
    } catch (Exception exc) {
      exc.printStackTrace();
    } finally {
      _running.set(false);
    }
  }

  public void set_patterns(List<Pattern> _patterns) {
    this._patterns = _patterns;
  }

  public List<Pattern> get_patterns() {
    return _patterns;
  }
  
  public static void main(String[] args) {
	  
	  try {
	  	//FileWatcher f = new FileWatcher(new File("G:/Buffer/livereload-jvm-master/livereload-jvm-master/src/main/java/net_alchim31_livereload/").toPath());
	  		//f.startWatcher();
	  		
	  }catch(Exception e){
		  e.printStackTrace();
	  }	
	  
  }  
  
}


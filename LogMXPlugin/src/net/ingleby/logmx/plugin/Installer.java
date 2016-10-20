/*
* MIT License
* 
* Copyright (c) 2016 Graeme Ingleby - graeme@ingleby.net
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package net.ingleby.logmx.plugin;

import com.lightyosft.logmx.gotosource.GotoSourceManager;
import com.lightyosft.logmx.gotosource.GotoSourceUtils;
import com.lightyosft.logmx.gotosource.business.PluginConfig;
import com.lightyosft.logmx.gotosource.business.SourceLocation;
import com.lightyosft.logmx.gotosource.business.SourcePreview;
import com.lightyosft.logmx.gotosource.network.SocketListener;
import com.lightyosft.logmx.gotosource.util.PluginLogger;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInstall;
import org.openide.text.Line;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import net.ingleby.logmx.plugin.options.LogMXGotoSourcePanel;
import org.openide.util.RequestProcessor;

public class Installer extends ModuleInstall {

    private static final Object MUTEX = new Object();

    private static final Logger LOG = Logger.getLogger("logmx.netbeans");

    private static SocketListener socketListener = null;

    /**
     * Start a GotoSource instance (stopping a previous one if necessary)
     */
    public static void startGotoSource() {
        RequestProcessor.getDefault().post(
                new Runnable() {
            @Override
            public void run() {
                synchronized (MUTEX) {
                    // safety
                    if (socketListener != null) {
                        socketListener.stop();
                    }

                    // 
                    /**
                     * Need to load configuration from persisted store
                     */
                    PluginConfig config = new PluginConfig(
                            NbPreferences.forModule(LogMXGotoSourcePanel.class).get("listenAddress", "127.0.0.1"),
                            NbPreferences.forModule(LogMXGotoSourcePanel.class).getInt("listenPort", 7789),
                            NbPreferences.forModule(LogMXGotoSourcePanel.class).getBoolean("enabled", true)
                    );

                    /**
                     * LogMX GotoSourceManager
                     */
                    GotoSourceManager srcMgr = new GotoSourceManager() {
                        @Override
                        public void goToSourceLocation(SourceLocation sl) {
                            if (!openEditorAt(sl)) {
                                displayGotoError(sl);
                            }
                        }

                        @Override
                        public SourcePreview previewSourceLocation(SourceLocation sl) {
                            return generatePreview(sl);
                        }
                    };

                    /**
                     * IDE Logger interface
                     */
                    PluginLogger logger = new PluginLogger() {
                        @Override
                        public void info(String string) {
                            LOG.log(Level.INFO, string);
                        }

                        @Override
                        public void error(String string) {
                            LOG.log(Level.SEVERE, string);
                        }

                        @Override
                        public void error(String string, Throwable thrwbl) {
                            LOG.log(Level.SEVERE, string, thrwbl);
                        }
                    };

                    /**
                     * Create and start the socket listener
                     */
                    socketListener = SocketListener.getInstance(srcMgr, logger);
                    socketListener.setConfig(config);
                    socketListener.stop();
                    if (config.isEnabled()) {
                        socketListener.start();
                    }
                }
            }
        });
    }

    /**
     * Stop a running GotoSource instance if there is one
     */
    public static void stopGotoSource() {
        RequestProcessor.getDefault().post(
                new Runnable() {
            @Override
            public void run() {
                synchronized (MUTEX) {
                    if (socketListener != null) {
                        socketListener.stop();
                        socketListener = null;
                    }
                }
            }
        });
    }

    /**
     * Install LogMX socket listener
     */
    @Override
    public void restored() {
        startGotoSource();
    }

    /**
     * Generate a preview snippet for the specified file/line
     *
     * @param sl
     * @return
     */
    private static SourcePreview generatePreview(SourceLocation sl) {
        String filePath = sl.getPackageName().replace(".", "/") + "/" + sl.getFileName();
        FileObject fo = GlobalPathRegistry.getDefault().findResource(filePath);
        if (fo != null) {
            try {
                return GotoSourceUtils.previewFile(fo.getInputStream(), sl.getLineNumber());
            } catch (FileNotFoundException ex) {
                LOG.log(Level.SEVERE, "Unable to parse \"" + sl.getFileName() + "\"", ex);
                return new SourcePreview(false, "Unable to parse \"" + sl.getFileName() + "\": " + ex.getLocalizedMessage(), 0);
            }
        }
        return new SourcePreview(false, "Unable to find \"" + sl.getFullyQualifiedMethod() + "(" + sl.getFileName() + ":" + sl.getLineNumber() + ")\" in this workspace", 0);
    }

    /**
     * Displays a source file and moves to a specific line
     *
     * @param sl
     */
    private static boolean openEditorAt(final SourceLocation sl) {
        String filePath = sl.getPackageName().replace(".", "/") + "/" + sl.getFileName();
        FileObject fo = GlobalPathRegistry.getDefault().findResource(filePath);
        if (fo != null) {
            try {
                DataObject d = DataObject.find(fo);
                if (d != null) {
                    final LineCookie lc = DataObject.find(fo).getLookup().lookup(LineCookie.class);
                    if (lc != null) {
                        // Make sure we are on the EDT
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Line l = lc.getLineSet().getOriginal(sl.getLineNumber() - 1);
                                l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);

                                // move main window to font...
                                mainWindowToFront();
                            }
                        });
                        return true;
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.SEVERE, "Unable to open \"" + sl.getFileName() + "\"", ex);
            }
        }
        return false;
    }

    /**
     * Displays an error dialog and brings the main window to the foreground
     *
     * @param sl
     */
    private static void displayGotoError(final SourceLocation sl) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Move main window to the front
                mainWindowToFront();

                // Display error dialog
                String errorMsg = "Unable to find \"" + sl.getFullyQualifiedMethod() + "(" + sl.getFileName() + ":" + sl.getLineNumber() + ")\" in this workspace";
                NotifyDescriptor d = new NotifyDescriptor(errorMsg, "LogMX Goto Source Error", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }

    /**
     *
     * Brings the application to the front
     *
     * @param script
     * @return
     */
    private static void mainWindowToFront() {
        WindowManager.getDefault().getMainWindow().toFront();

        // If OS X toFront above does not work, so...
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("AppleScriptEngine");
            if (engine == null) {
                engine = mgr.getEngineByName("AppleScript");
            }
            if (engine != null) {
                try {
                    engine.eval("tell me to activate");
                } catch (ScriptException e) {
                }
            }
        }
    }

    @Override
    public void close() {
        stopGotoSource();
    }

    @Override
    public void uninstalled() {
        stopGotoSource();
    }    
}

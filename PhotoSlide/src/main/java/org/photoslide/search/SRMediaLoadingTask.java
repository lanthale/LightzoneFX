/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.photoslide.search;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.controlsfx.control.GridView;
import org.photoslide.App;
import org.photoslide.MainViewController;
import org.photoslide.ThreadFactoryBuilder;
import org.photoslide.browserlighttable.MediaLoadingTask;
import org.photoslide.browsermetadata.MetadataController;
import org.photoslide.datamodel.MediaFileLoader;
import org.photoslide.datamodel.FileTypes;
import org.photoslide.datamodel.MediaFile;

/**
 *
 * @author selfemp
 */
public class SRMediaLoadingTask extends Task<MediaFile> {
    
    private final SearchToolsController searchController;
    private final ObservableList<MediaFile> fullMediaList;
    private final GridView<MediaFile> imageGrid;
    private final MediaFileLoader fileLoader;
    private final ArrayList<String> queryList;
    private final MainViewController mainController;
    private final MetadataController metaController;
    private final ScheduledExecutorService executor;
    
    public SRMediaLoadingTask(ArrayList<String> queryList, SearchToolsController control, ObservableList<MediaFile> fullMediaList, GridView<MediaFile> imageGrid, MetadataController mc, MainViewController mv) {
        this.searchController = control;
        executor = Executors.newScheduledThreadPool(20, new ThreadFactoryBuilder().setNamePrefix("SRMediaLoadingTask").build());
        this.fullMediaList = fullMediaList;
        this.imageGrid = imageGrid;
        fileLoader = new MediaFileLoader();
        this.queryList = queryList;
        mainController = mv;
        metaController = mc;
    }
    
    @Override
    protected MediaFile call() throws Exception {
        if (queryList.isEmpty()) {
            throw new IOException("Nothing found!");
        }
        for (String query : queryList) {
            if (this.isCancelled()) {
                return null;
            }
            try ( Statement stm = App.getSearchDBConnection().createStatement();  ResultSet rs = stm.executeQuery(query)) {
                rs.next();
                String mediaURL = rs.getString("pathStorage");
                MediaFile mediaItem = new MediaFile();
                mediaItem.setMediaType(MediaFile.MediaTypes.IMAGE);
                mediaItem.setName(Path.of(mediaURL).getFileName().toString());
                mediaItem.setPathStorage(Path.of(mediaURL));
                try {
                    loadItem(this, mediaItem, mediaURL);
                    updateValue(mediaItem);
                } catch (Exception e) {                    
                    mediaItem.setMediaType(MediaFile.MediaTypes.NONE);
                }
                if (this.isCancelled() == true) {
                    return null;
                }                
            }
        }
        return null;
    }
    
    public void shutdown() {
        executor.shutdownNow();
    }
    
    private void loadItem(Task task, MediaFile mediaItem, String mediaURL) throws Exception {        
        mediaItem.readEdits();
        if (this.isCancelled() == true) {
            return;
        }
        mediaItem.getCreationTime();
        if (mainController.isMediaFileBookmarked(mediaItem)) {
            mediaItem.setBookmarked(true);
        }
        if (this.isCancelled() == true) {
            return;
        }
        
        if (FileTypes.isValidVideo(mediaURL)) {
            if (this.isCancelled() == true) {
                return;
            }
            mediaItem.setMediaType(MediaFile.MediaTypes.VIDEO);
        } else if (FileTypes.isValidImage(mediaURL)) {
            mediaItem.setMediaType(MediaFile.MediaTypes.IMAGE);
            try {
                metaController.readBasicMetadata(this, mediaItem);
            } catch (IOException ex) {
                Logger.getLogger(MediaLoadingTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (this.isCancelled() == true) {
                task.cancel();
                return;
            }
            if (this.isCancelled() == true) {
                return;
            }
        } else {
            mediaItem.setMediaType(MediaFile.MediaTypes.NONE);
        }
    }
    
    @Override
    protected void updateValue(MediaFile v) {
        if (v != null) {
            super.updateValue(v);
            Platform.runLater(() -> {
                fullMediaList.add(v);
            });
        }
    }
    
}

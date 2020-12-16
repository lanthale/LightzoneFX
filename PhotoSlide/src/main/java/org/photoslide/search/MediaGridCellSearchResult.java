/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.photoslide.search;

import org.photoslide.datamodel.*;
import org.controlsfx.control.GridCell;

/**
 *
 * @author selfemp
 */
public class MediaGridCellSearchResult extends GridCell<MediaFile> {

    private final MediaFile mediaFile;

    public MediaGridCellSearchResult() {
        this.setId("MediaGridCell");
        mediaFile = new MediaFile();
        managedProperty().bind(mediaFile.managedProperty());
        visibleProperty().bind(mediaFile.visibleProperty());
    }

    /**
     * Here all properties of the mediaFile must be set. If not no graphic
     * update is happening
     *
     * @param item to be updated
     * @param empty only on creation otherwise should be always false
     */
    @Override
    protected void updateItem(MediaFile item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {

        } else {
            mediaFile.setSize(item.getHeight(), item.getWidth());
            mediaFile.setVisible(item.isVisible());
            mediaFile.setManaged(item.isManaged());
            mediaFile.setName(item.getName());
            mediaFile.setDeleted(item.isDeleted());
            mediaFile.setSelected(item.isSelected());
            mediaFile.setRating(item.getRatingProperty().get());
            mediaFile.setRotationAngle(item.getRotationAngleProperty().get());
            mediaFile.setCropView(item.getCropView());
            mediaFile.setRecordTime(item.getRecordTime());
            mediaFile.setVideoSupported(item.getVideoSupported());
            mediaFile.setCreationTime(item.getCreationTime());
            mediaFile.setStackName(item.getStackName());
            mediaFile.setStackPos(item.getStackPos());
            mediaFile.setStacked(item.isStacked());
            mediaFile.setSubViewSelected(item.isSubViewSelected());  
            mediaFile.setFilterList(item.getFilterList());
            mediaFile.setUnModifiyAbleImage(item.getUnModifiyAbleImage());
            mediaFile.getPlaces().set(item.getPlaces().get());
            mediaFile.getFaces().set(item.getFaces().get());
            mediaFile.getComments().set(item.getComments().get());
            if (mediaFile.isSubViewSelected()) {
                this.setId("MediaGridCellSelectedStackedDetails");
            } else {
                this.setId("MediaGridCellStackedDetails");
            }
            switch (item.getMediaType()) {
                case VIDEO -> {
                    mediaFile.setMedia(item.getMedia(), item.getVideoSupported());
                    setGraphic(mediaFile);
                }
                case IMAGE -> {
                    mediaFile.setImage(item.getImage());
                    setGraphic(mediaFile);
                }
                default -> {
                }
            }
        }
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    
}
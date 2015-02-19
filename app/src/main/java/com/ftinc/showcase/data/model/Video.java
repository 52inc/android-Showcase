package com.ftinc.showcase.data.model;

import java.io.File;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;

/**
 * Project: VideoLooperProject
 * Package: com.ftapps.kiosk.model
 * Created by drew.heavner on 10/3/14.
 */
@Table("videos")
public class Video extends Model{

    @Column("path")
    public String file;

    @Column("name")
    public String name;

    @Column("thumbnail_path")
    public String thumbnail = "";

    /**
     * Empty constructor
     */
    public Video(){
        super();
    }

    /**
     * File constructor
     * @param videoFile
     */
    public Video(File videoFile){
        this();
        file = videoFile.getAbsolutePath();
        name = videoFile.getName();
    }

}

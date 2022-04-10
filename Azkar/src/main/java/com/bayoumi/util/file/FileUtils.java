package com.bayoumi.util.file;

import com.bayoumi.models.Muezzin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static void addFilesNameToList(File folder, List<String> list) {
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    list.add(file.getName());
                }
            }
        }
    }

    public static void addFilesNameToListRemoveExtension(File folder, List<String> list, String extension) {
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    list.add(file.getName().replace(extension, ""));
                }
            }
        }
    }

    public static ObservableList<String> getAudioList() {
        ObservableList<String> audioFiles = FXCollections.observableArrayList("بدون صوت");
        addFilesNameToList(new File("jarFiles/audio"), audioFiles);
        return audioFiles;
    }

    public static void main(String[] args) {
        List<Muezzin> muezzinList = new ArrayList<>();
        for (String s : getAdhanFilesNames()) {
            for (Muezzin muezzin : Muezzin.values()) {
                if (s.equals(muezzin.getFileName())) {
                    muezzinList.add(muezzin);
                }
            }
        }
        muezzinList.forEach((muezzin) -> {
            System.out.println(muezzin);
        });
    }

    public static List<Muezzin> getAdhanList() {
        List<Muezzin> muezzinList = new ArrayList<>();
        for (String s : getAdhanFilesNames()) {
            for (Muezzin muezzin : Muezzin.values()) {
                if (s.equals(muezzin.getFileName())) {
                    muezzinList.add(muezzin);
                }
            }
        }
        return muezzinList;
    }

    public static List<String> getAdhanFilesNames() {
        List<String> audioFiles = new ArrayList<>();
        addFilesNameToList(new File(Muezzin.PARENT_PATH), audioFiles);
        return audioFiles;
    }
}

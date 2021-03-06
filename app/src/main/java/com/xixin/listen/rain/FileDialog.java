package com.xixin.listen.rain;

/**
 * Created by xia on 2018/3/31.
 */

import android.app.Activity;

import android.app.Dialog;

import android.content.DialogInterface;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;

import android.support.v7.app.AlertDialog;

import android.util.Log;
import android.view.WindowManager;

import java.io.File;

import java.io.FilenameFilter;

import java.util.ArrayList;

import java.util.List;


class FileDialog {

    private static final String PARENT_DIR = "..";

    private final String TAG = getClass().getName();

    private String[] fileList;

    private File currentPath;

    public interface FileSelectedListener {

        void fileSelected(File file);

    }

    public interface DirectorySelectedListener {

        void directorySelected(File directory);

    }

    private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<FileDialog.FileSelectedListener>();

    private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<FileDialog.DirectorySelectedListener>();

    private final Activity activity;

    private boolean selectDirectoryOption;

    private String fileEndsWith;


    /**
     * @param activity
     * @param initialPath
     */

    public FileDialog(Activity activity, File initialPath) {

        this(activity, initialPath, null);

    }


    public FileDialog(Activity activity, File initialPath, String fileEndsWith) {

        this.activity = activity;

        setFileEndsWith(fileEndsWith);

        if (!initialPath.exists()) initialPath = Environment.getExternalStorageDirectory();

        loadFileList(initialPath);

    }


    /**
     * @return file dialog
     */

    public Dialog createFileDialog() {

        Dialog dialog = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);//, R.style.AlertDialo

        builder.setTitle(currentPath.getPath());

        if (selectDirectoryOption) {

            builder.setPositiveButton("Select directory", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    Log.d(TAG, currentPath.getPath());

                    fireDirectorySelectedEvent(currentPath);

                }

            });

        }


        builder.setItems(fileList, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                String fileChosen = fileList[which];

                File chosenFile = getChosenFile(fileChosen);

                if (chosenFile.isDirectory()) {

                    loadFileList(chosenFile);

                    dialog.cancel();

                    dialog.dismiss();

                    showDialog();

                } else fireFileSelectedEvent(chosenFile);

            }

        });


        dialog = builder.show();

        //自下义内容
        realOpenFileDialog = dialog;
        realBuilder = builder;

        setBackGroundColor(Color.argb(0xff, 0x88, 0x88, 0x88));

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = 900;
        params.height = 1600;
//        params.setColorMode(col);
        dialog.getWindow().setAttributes(params);


//
//        builder.setView(vie)

        return dialog;

    }

    public Dialog realOpenFileDialog = null;
    public AlertDialog.Builder realBuilder = null;

    public void setBackGroundColor(int color) {
        if (realOpenFileDialog != null)
            realOpenFileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(color));
    }

    public void setTitle(String title) {
        if (realBuilder != null)
            realBuilder.setTitle(title);
    }


    public void addFileListener(FileSelectedListener listener) {

        fileListenerList.add(listener);

    }


    public void removeFileListener(FileSelectedListener listener) {

        fileListenerList.remove(listener);

    }


    public void setSelectDirectoryOption(boolean selectDirectoryOption) {

        this.selectDirectoryOption = selectDirectoryOption;

    }


    public void addDirectoryListener(DirectorySelectedListener listener) {

        dirListenerList.add(listener);

    }


    public void removeDirectoryListener(DirectorySelectedListener listener) {

        dirListenerList.remove(listener);

    }


    /**
     * Show file dialog
     */

    public void showDialog() {

        createFileDialog().show();

    }


    private void fireFileSelectedEvent(final File file) {

        fileListenerList.fireEvent(new ListenerList.FireHandler<FileSelectedListener>() {

            public void fireEvent(FileSelectedListener listener) {

                listener.fileSelected(file);

            }

        });

    }


    private void fireDirectorySelectedEvent(final File directory) {

        dirListenerList.fireEvent(new ListenerList.FireHandler<DirectorySelectedListener>() {

            public void fireEvent(DirectorySelectedListener listener) {

                listener.directorySelected(directory);

            }

        });

    }


    private void loadFileList(File path) {

        this.currentPath = path;

        List<String> r = new ArrayList<>();

        if (path.exists()) {

            if (path.getParentFile() != null) r.add(PARENT_DIR);

            FilenameFilter filter = new FilenameFilter() {

                public boolean accept(File dir, String filename) {

                    File sel = new File(dir, filename);

                    if (!sel.canRead()) return false;

                    if (selectDirectoryOption) return sel.isDirectory();

                    else {

                        boolean endsWith = fileEndsWith != null ? filename.toLowerCase().endsWith(fileEndsWith) : true;

                        //自己加的，可能破坏类的可移植性
                        if (filename.toLowerCase().endsWith("mp3") || filename.toLowerCase().endsWith("wma") || filename.toLowerCase().endsWith("wav"))
                            endsWith = true;
                        else
                            endsWith = false;

                        return endsWith || sel.isDirectory();

                    }

                }

            };

            String[] fileList1 = path.list(filter);

            //自己加的
//            FileList = fileList1;

            for (String file : fileList1) {

                r.add(file);

            }

        }

        fileList = (String[]) r.toArray(new String[]{});

    }

    //以下三个函数是自己加的
    public static String[] FileList;

    public static void initFileList(String currentFileName) {
        String initDir = "/storage/";
        if (currentFileName != null && currentFileName.lastIndexOf("/") > 0)
            initDir = currentFileName.substring(0, currentFileName.lastIndexOf("/"));

        File dir = new File(initDir);

        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String filename) {

                File sel = new File(dir, filename);

                if (!sel.canRead()) return false;
                else {


                    boolean endsWith = false;

                    //自己加的，可能破坏类的可移植
                    if (filename.toLowerCase().endsWith("mp3") || filename.toLowerCase().endsWith("wma") || filename.toLowerCase().endsWith("wav"))
                        endsWith = true;
                    else
                        endsWith = false;

                    return endsWith || sel.isDirectory();
                }

            }

        };

        FileList = dir.list(filter);
    }

    public static String getDir(String currentFileName) {
        String initDir = "/storage/";
        if (currentFileName != null && currentFileName.lastIndexOf("/") > 0)
            initDir = currentFileName.substring(0, currentFileName.lastIndexOf("/"));
        return initDir + "/";
    }

    public static String getForwardLession(String currentFileName) {
        initFileList(currentFileName);
        for (int i = 0; i < FileList.length; i++) {
            if ((getDir(currentFileName) + FileList[i]).trim().equals(currentFileName.trim())) {
                int forwardIndex = (i == 0 ? 0 : i - 1);
                return getDir(currentFileName) + FileList[forwardIndex];
            }
        }
        return getDir(currentFileName) + FileList[0];
    }

    public static String getNextLession(String currentFileName) {
        initFileList(currentFileName);
        for (int i = 0; i < FileList.length; i++) {
            if ((getDir(currentFileName) + FileList[i]).trim().equals(currentFileName.trim())) {
                int forwardIndex = (i == FileList.length - 1 ? FileList.length - 1 : i + 1);
                return getDir(currentFileName) + FileList[forwardIndex];
            }
        }
        return getDir(currentFileName) + FileList[FileList.length - 1];//+"/n"+String.valueOf(FileList.length)+"/n"+(getDir(currentFileName)+FileList[6]).trim()+"/n"+currentFileName;
    }

    private File getChosenFile(String fileChosen) {

        if (fileChosen.equals(PARENT_DIR)) return currentPath.getParentFile();

        else return new File(currentPath, fileChosen);

    }


    private void setFileEndsWith(String fileEndsWith) {

        this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;

    }

}


class ListenerList<L> {

    private List<L> listenerList = new ArrayList<L>();


    public interface FireHandler<L> {

        void fireEvent(L listener);

    }


    public void add(L listener) {

        listenerList.add(listener);

    }


    public void fireEvent(FireHandler<L> fireHandler) {

        List<L> copy = new ArrayList<L>(listenerList);

        for (L l : copy) {

            fireHandler.fireEvent(l);

        }

    }


    public void remove(L listener) {

        listenerList.remove(listener);

    }


    public List<L> getListenerList() {

        return listenerList;

    }

}
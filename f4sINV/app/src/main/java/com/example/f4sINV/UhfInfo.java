package com.example.f4sINV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UhfInfo {
    private ArrayList<HashMap<String, String>> tagList = null;
    private long time;
    private int count=0;
    private int tagNumber=0;
    private int selectIndex=-1;
    private String selectItem;

    private HashMap<String, Integer> mapTempDatas;
    private int errF4s =0;                      //Errores que se traspasan entre Fragments

    public int getErrF4s() {
        return errF4s;
    }

    public void setErrF4s(int errF4s) {
        this.errF4s = errF4s;
    }

    public ArrayList<HashMap<String, String>> getTagList() {
        return tagList;
    }

    public void setTagList(ArrayList<HashMap<String, String>> tagList) {
        this.tagList = tagList;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(int tagNumber) {
        this.tagNumber = tagNumber;
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        this.selectIndex = selectIndex;
    }

    public HashMap<String, Integer> getTempDatas() {
        return mapTempDatas;
    }

    public void setTempDatas(HashMap<String, Integer>  mapTempDatas) {
        this.mapTempDatas = mapTempDatas;
    }

    public String getSelectItem() {
        return selectItem;
    }

    public void setSelectItem(String selectItem) {
        this.selectItem = selectItem;
    }
}

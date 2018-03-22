package com.gdut.water.mymap3d.locationsearch.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    //private static final int COUNT = 10;

    //设立一个List<String>数组存储位置检索的内容
    public static final List<String> content = new ArrayList<>();

    static {
        // Add items.
       addContentData();
        for (int i = 1; i <= content.size(); i++) {
            addItem(createDummyItem(i,content.get(i-1)));
        }
    }

    private static void addContentData(){
        content.add("Poi关键字检索");
        content.add("Poi周边检索");
        content.add("沿途搜索");
        content.add("Route路径规划");
        content.add("云图检索");
        content.add("行政区域边界查询");
        content.add("当前位置测距/面积");
        content.add("历史搜索构建热力图");

    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position,String content) {
        return new DummyItem(String.valueOf(position), content, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}

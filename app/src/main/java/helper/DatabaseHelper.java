package helper;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库名称和版本号
    // 数据库名称和版本号
    private static final String DATABASE_NAME = "page_tree.db";
    private static final int DATABASE_VERSION = 2;

    // 表格和列的定义
    public static final String TABLE_PAGES = "pages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_PARENT_ID = "parent_id";
    public static final String COLUMN_OPEN_TIME = "open_time"; // 添加打开时间列名
    public static final String COLUMN_THUMBNAIL = "thumbnail"; // 添加缩略图列名

    // 创建表格的SQL语句
    private static final String DATABASE_CREATE =
            "CREATE TABLE " + TABLE_PAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_URL + " TEXT NOT NULL, " +
                    COLUMN_PARENT_ID + " INTEGER, " +
                    COLUMN_OPEN_TIME + " INTEGER, " +
                    COLUMN_THUMBNAIL + " BLOB, " +
                    "FOREIGN KEY(" + COLUMN_PARENT_ID + ") REFERENCES " + TABLE_PAGES + "(" + COLUMN_ID + ")" +
                    ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // 方法：将WebPage对象转换为ContentValues对象
    private ContentValues webPageToContentValues(WebPage webPage) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, webPage.getTitle());
        values.put(COLUMN_URL, webPage.getUrl());
        values.put(COLUMN_OPEN_TIME, webPage.getOpenTime().getTime());
        values.put(COLUMN_THUMBNAIL, webPage.getThumbnail());
        values.put(COLUMN_PARENT_ID, webPage.getParentId()); // 保存parent-id到数据库表中
        return values;
    }


    // 方法：将数据库表中的记录转换为WebPage对象
    private WebPage cursorToWebPage(Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL));
        long openTimeTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OPEN_TIME));
        byte[] thumbnail = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_THUMBNAIL));
        long parentId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARENT_ID)); // 从数据库表中读取parent-id
       /* if (parentId == 0) {
            parentId = -1;
        }*/
        Date openTime = new Date(openTimeTimestamp);
        WebPage webPage = new WebPage(title, url, openTime);
        webPage.setThumbnail(thumbnail);
        webPage.setParentId(parentId); // 设置parent-id到WebPage对象
        int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
        if (idColumnIndex == -1) {
            // Handle the case when the column is not found
            Log.e("TAG", "Column not found: " + COLUMN_ID);
            return null;
        }

        long id = cursor.getLong(idColumnIndex);
        webPage.setId(id);
        return webPage;
    }

    // 方法：获取所有WebPage对象列表
    // 获取所有WebPage对象列表
    public List<WebPage> getAllWebPages() {
        List<WebPage> webPages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询数据库，获取所有页面数据
        Cursor cursor = db.query(TABLE_PAGES, null, null, null, null, null, null);

        // 遍历Cursor对象，将每一行记录转换为WebPage对象并加入列表
        if (cursor.moveToFirst()) {
            do {
                WebPage webPage = cursorToWebPage(cursor);
                webPages.add(webPage);
                //Log.d("TAG", "parentId: " + parentId);
                //Log.d("TAG", "遍历：" + webPage.getTitle()+"  id:  "+webPage.getId()+" Parent id："+webPage.getParentId());
            } while (cursor.moveToNext());
        }

        // 关闭Cursor和数据库连接
        cursor.close();
        db.close();

        return webPages;
    }
    // 获取所有顶级WebPage对象列表
    public List<WebPage> getTopLevelWebPages() {
        Log.d("TAG", "getTopLevelWebPages method called");

        List<WebPage> webPages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询数据库，获取所有顶级页面数据（没有父页面的页面）
        Cursor cursor = db.query(TABLE_PAGES, null, COLUMN_PARENT_ID + " = -1", null, null, null, null);

        // 遍历Cursor对象，将每一行记录转换为WebPage对象并加入列表
        if (cursor.moveToFirst()) {
            do {
                WebPage webPage = cursorToWebPage(cursor);
                webPages.add(webPage);
                Log.d("TAG", "TopLevel WebPage: title" + webPage.getTitle());
            } while (cursor.moveToNext());
        }

        // 关闭Cursor和数据库连接
        cursor.close();
        db.close();

        return webPages;
    }


    // 更新父页面ID
    public void updateParentId(long pageId, long parentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARENT_ID, parentId);

        db.update(TABLE_PAGES, values, COLUMN_ID + "=?", new String[]{String.valueOf(pageId)});

        db.close();
    }
    public boolean Existed(String url){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_ID};
        String selection = COLUMN_URL + "=?";
        String[] selectionArgs = {url};
        Cursor cursor = db.query(TABLE_PAGES, projection, selection, selectionArgs, null, null, null);

        boolean isExists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return isExists;
    }


  /*  public boolean Existed(String url){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_ID};
        String selection = "SUBSTR(" + COLUMN_URL + ", 0, INSTR(" + COLUMN_URL + ", '?q=') + 3) = ?";
        String[] selectionArgs = {getQueryKeyword(url)};
        Cursor cursor = db.query(TABLE_PAGES, projection, selection, selectionArgs, null, null, null);

        boolean isExists = cursor.getCount() > 0;

        cursor.close();
        db.close();
        Log.d(TAG, "Existed: "+selection);
        return isExists;
    }
*/
    // 辅助方法，用于提取查询关键词及其之前的部分
    private String getQueryKeyword(String url) {
        Log.d(TAG, "start get ");
        int keywordIndex = url.indexOf("?q=");
        if (keywordIndex >= 0) {
            Log.d(TAG, "getQueryKeyword1: "+url);
            return url.substring(0, keywordIndex + 3);
        }
        Log.d(TAG, "getQueryKeyword: "+url);
        return url;
    }

    // 根据父页面ID获取子页面列表
    public List<WebPage> getChildWebPages(long parentId) {
        return getWebPagesByParentId(parentId);
    }

    // 私有方法：根据父页面ID获取WebPage对象列表
    public List<WebPage> getWebPagesByParentId(long parentId) {
        List<WebPage> webPages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询数据库，根据传入的 parentId 来返回对应的子页面数据
        Cursor cursor;


        cursor = db.query(TABLE_PAGES, null, COLUMN_PARENT_ID + "=?", new String[]{String.valueOf(parentId)}, null, null, null);

        // 遍历Cursor对象，将每一行记录转换为WebPage对象并加入列表
        if (cursor.moveToFirst()) {
            do {
                WebPage webPage = cursorToWebPage(cursor);
                webPages.add(webPage);
                Log.d("TAG", "WebPages: title" + webPage.getTitle()+' '+parentId);
            } while (cursor.moveToNext());
        }

        // 关闭Cursor和数据库连接
        cursor.close();
        db.close();

        return webPages;
    }
    public List<WebPage> getAllChildWebPages(WebPage parentPage) {
        List<WebPage> allChildPages = new ArrayList<>();

        // 获取指定页面的直接子页面
        List<WebPage> directChildPages = getChildWebPages(parentPage.getId());

        // 遍历直接子页面，将其加入到所有子页面列表中
        allChildPages.addAll(directChildPages);

        // 遍历直接子页面，递归获取其所有子页面并加入到列表中
        for (WebPage directChildPage : directChildPages) {
            List<WebPage> nestedChildPages = getAllChildWebPages(directChildPage);
            allChildPages.addAll(nestedChildPages);
        }

        return allChildPages;
    }

    // 方法：判断页面是否有子页面
    public boolean webPageHasChildren(WebPage webPage) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PAGES, null, COLUMN_PARENT_ID + "=?", new String[]{String.valueOf(webPage.getId())}, null, null, null);

        boolean hasChildren = cursor.getCount() > 0;

        // 关闭Cursor和数据库连接
        cursor.close();
        db.close();

        return hasChildren;
    }


    // 方法：更新指定的WebPage对象
    public void updateWebPage(WebPage webPage) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 将WebPage对象转换为ContentValues对象
        ContentValues values = webPageToContentValues(webPage);

        // 更新数据库中指定的记录
        db.update(TABLE_PAGES, values, COLUMN_ID + "=?", new String[]{String.valueOf(webPage.getId())});

        // 关闭数据库连接
        db.close();
    }

    public long getPageIdByUrl(String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_ID};
        String selection = COLUMN_URL + "=?";
        String[] selectionArgs = {url};
        Cursor cursor = db.query(TABLE_PAGES, projection, selection, selectionArgs, null, null, null);

        long pageId = -1;
        if (cursor.moveToFirst()) {
            pageId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }

        cursor.close();
        db.close();

        return pageId;
    }

    public long getParentIdByUrl(String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_PARENT_ID};
        String selection = COLUMN_URL + "=?";
        String[] selectionArgs = {url};
        Cursor cursor = db.query(TABLE_PAGES, projection, selection, selectionArgs, null, null, null);

        long parentId = -1;
        if (cursor.moveToFirst()) {
            parentId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARENT_ID));
        }

        cursor.close();
        db.close();

        return parentId;
    }

    public void moveChildPagesToParent(WebPage parentPage, List<WebPage> childPages) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            long parentId = parentPage.getId();

            for (WebPage childPage : childPages) {
                childPage.setParentId(parentId);
                updateWebPage(childPage);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error moving child pages to parent: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    public void moveSelectedItemsAndDelete(WebPage selectedParent, List<WebPage> selectedItemsToMove) {
        for (WebPage selectedItem : selectedItemsToMove) {
            if (webPageHasChildren(selectedItem)) {
                Log.d(TAG, "parentid:"+selectedItem);

                    long newParentId = (selectedParent != null) ? selectedItem.getParentId() : -1;

                    Log.d(TAG, "moveSelectedItemsAndDelete: "+selectedItem.getParentId());
                    // 更新子项目的parentID为新的parentID
                    updateParentIdForChildren(selectedItem.getId(), newParentId);

                    // 删除当前项目
                    deleteWebPage(selectedItem.getId());


            } else {
                deleteWebPage(selectedItem.getId());
            }
        }
    }


    public void deleteWebPage(long webPageId) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(webPageId) };

        // 删除具有指定 ID 的记录
        db.delete(TABLE_PAGES, selection, selectionArgs);

        db.close();
    }

    public void updateParentIdForChildren(long currentParentId, long newParentId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARENT_ID, newParentId);

        String selection = COLUMN_PARENT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(currentParentId) };

        db.update(TABLE_PAGES, values, selection, selectionArgs);
        db.close();
    }

    // 方法用于插入WebPage数据到数据库
    // 方法用于插入WebPage数据到数据库
    public long insertWebPage(WebPage webPage, long parentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, webPage.getTitle());
            values.put(COLUMN_URL, webPage.getUrl());
            values.put(COLUMN_OPEN_TIME, webPage.getOpenTime().getTime());
            values.put(COLUMN_THUMBNAIL, webPage.getThumbnail());
            //values.put(COLUMN_ID,webPage.getId());

            // 设置父页面ID，如果是顶级页面则传入-1

            values.put(COLUMN_PARENT_ID, parentId);

            long id = db.insert(TABLE_PAGES, null, values);

            db.setTransactionSuccessful(); // 标记事务成功
            Log.d("TAG", "saved id: " + id + ","  + webPage.getTitle() + " parent id：" + parentId);

            return id;
        } catch (Exception e) {
            Log.e("TAG", "Error inserting data into database: " + e.getMessage());
            return -1;
        } finally {
            db.endTransaction(); // 结束事务
            db.close(); // 关闭数据库连接
        }
    }



}
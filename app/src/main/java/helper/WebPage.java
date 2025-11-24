package helper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class WebPage implements Serializable {
    private long id; // 数据库中的唯一标识符
    private String title;
    private String url;
    private Date openTime;
    private byte[] thumbnail; // 缩略图的数据
    private int level; // 添加 level 属性

    private long parentId;

    private boolean checked;

    // 用于在数据库中读取数据时调用，带有5个参数
    public WebPage(long id, String title, String url, Date openTime, long parentId, byte[] thumbnail) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.openTime = openTime;
        this.parentId=parentId;
        this.thumbnail = thumbnail;
    }

    // 用于在创建新的WebPage时调用，id和thumbnail为空
    public WebPage(String title, String url, Date openTime) {
        this.title = title;
        this.url = url;
        this.openTime = openTime;
        this.thumbnail = null;
        //this.parentId=-1;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    // 设置父页面ID的方法
    public void setParentId(long parentId) {
        this.parentId = parentId;
    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    // 获取父页面ID的方法
    public long getParentId() {
        return parentId;
    }

    public void setChecked(boolean isChecked) {
        // 在 WebPage 类中添加一个名为 "checked" 的属性，用于存储选中状态
        checked = isChecked;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        WebPage other = (WebPage) obj;
        return Objects.equals(title, other.title) && Objects.equals(url, other.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, url);
    }

    public void setId(long id) {
       this.id=id;
    }

    private boolean expanded; // 添加一个用于存储展开状态的属性

    // 构造方法和其他方法不变




    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}

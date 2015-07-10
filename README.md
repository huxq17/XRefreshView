# XRefreshView


##最新的使用说明请移步[我的博客](http://blog.csdn.net/footballclub/article/details/46678521 "description")


一、写在开头的话

之所以写这个东西是因为项目中有用到，需要给stickylistheaders加个刷新，其实就是个framelayout里面有个listview的自定义view布局，但是一些知名的刷新框架我试了下都不支持，pulltoRefresh和XListView都是自己实现了一个可刷新的view，然后让我们来直接使用这个可刷新的view，从而达到可以上拉下拉刷新的目的。我这个需求需要的是一个我告诉他什么时候需要刷新他就能帮我刷新的框架，也就是说不管什么view，只要能告诉框架自己什么时候需要刷新，框架就可以给你在什么时候刷新，从这个角度上来说，这个框架是万能的，适用于所有的view，他就是我接下来要介绍的——XRefreshView。

二、效果图

![效果图](http://img.my.csdn.net/uploads/201507/09/1436432130_6606.gif) 

gif上看的效果可能不是很好有点卡卡的，但是在真机上面，还是挺流畅的。

三、简单用法

XRefreshView现阶段默认支持ListView，GridView，WebView，当然还可以支持scrollview，textview，只不过现在没有加进去，加进去也很简单的。

对于任何想要刷新的view，只需要在想要刷新的view外面套一层XRefreshView就可以了，就像这样
[html] view plaincopy

    <com.andview.refreshview.XRefreshView xmlns:android="http://schemas.android.com/apk/res/android"  
        xmlns:xrefreshview="http://schemas.android.com/apk/res-auto"  
        xmlns:tools="http://schemas.android.com/tools"  
        android:id="@+id/custom_view"  
        android:layout_width="match_parent"  
        android:layout_height="match_parent"  
        android:background="#fff"  
        android:orientation="vertical"  
        xrefreshview:isHeightMatchParent="true"  
        xrefreshview:isWidthMatchParent="true" >  
      
        <ListView  
            android:id="@+id/lv"  
            android:layout_width="match_parent"  
            android:layout_height="wrap_content" />  
        <!-- xmlns:xrefreshview="http://schemas.android.com/apk/res/com.andview.refreshview" -->  
      
    </com.andview.refreshview.XRefreshView>  

其中的xrefreshview标签是我自定义的一些属性
[html] view plaincopy

    <declare-styleable name="XRefreshView">  
           <attr name="isHeightMatchParent" format="boolean" />  
           <attr name="isWidthMatchParent" format="boolean" />  
           <attr name="autoRefresh" format="boolean" />  
       </declare-styleable>  
       <declare-styleable name="StickyListHeadersListView">  

isHeightMatchParent和isWidthMatchParent代表宽高是否充满parent，为什么需要这两个属性呢？

嗯，是这样的，我在做stickylistheaders下拉刷新的时候，发现会丢帧、卡顿的情况。之后在解决问题的过程中，我发现主要的耗时都发生在onMeasure过程中，也就是说系统在测量view的尺寸的时候会消耗太多的时间从而导致卡顿。知道了问题在哪，接下来就得去解决他。很明显有两种方案：1、重写stickylistheaders，使其在测量的时候效率更高；2、找自己的原因。第一种方案代价有点大，所以我就在找自己的原因，后来在网上看到这么一篇文章，Android View.onMeasure方法的理解

他其中有提到，当我们设置width或height为fill_parent时，容器在布局时调用子view的measure方法传入的模式是EXACTLY，因为子view会占据剩余容器的空间，所以它大小是确定的，当设置为 wrap_content时，容器传进去的是AT_MOST,表示子view的大小最多是多少。然后我有看了看我的布局
[html] view plaincopy

    <com.andview.refreshview.XRefreshView xmlns:android="http://schemas.android.com/apk/res/android"  
        xmlns:xrefreshview="http://schemas.android.com/apk/res-auto"  
        xmlns:tools="http://schemas.android.com/tools"  
        android:id="@+id/custom_view"  
        android:layout_width="match_parent"  
        android:layout_height="match_parent"  
        android:background="#fff"  
        xrefreshview:autoRefresh="true" >  
      
        <com.example.xrefreshviewdemo.stickyListHeaders.StickyListHeadersListView  
            android:id="@+id/sticky_list"  
            android:layout_width="match_parent"  
            android:layout_height="wrap_content"  
            />  
      
    </com.andview.refreshview.XRefreshView>  

height是设为wrap_content的，当我换成match_parent以后问题就解决了。我就在想，可能是因为系统在测量的时候，你告诉系统一个精确的尺寸，系统会非常高兴，一高兴效率就高了。

所以如果你用的是一个比较复杂的view，而且用刷新会有卡顿的话，不妨设置宽高为match_parent，而XRefreshView则自动帮你做了这件事，默认的isHeightMatchParent和isWidthMatchParent都为true。但是如果你有自己特殊的需要，你可以在布局中把他们设为false。

autoRefresh是指刚进入页面时view是否自动刷新


3.1 XRefreshView默认支持的view


接下来对于XRefreshView默认支持的view来说，在java代码里需要这样做
[java] view plaincopy

    // 设置是否可以上拉刷新  
            refreshView.setPullLoadEnable(true);  

设置是否可以上拉刷新，默认是不可以的
[java] view plaincopy

    // 设置刷新view的类型  
            refreshView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);  

设置要刷新view的类型，先看下XRefreshViewType这个类
[java] view plaincopy

    /** 
     * 支持下拉上拉刷新的view 
     * @author huxq17@163.com 
     * 
     */  
    public enum XRefreshViewType {  
        NOSCROLLVIEW, ABSLISTVIEW, SCROLLVIEW, WEBVIEW,NONE  
    }  

分别是不可滚动的view，abslistview，scrollview，webview和其他

在使用的时候需要注意，只有XRefreshView默认支持的view才需要设置view的类型，但如果你是自定义view就不需要设置了。
[java] view plaincopy

    //设置是否可以自动刷新  
            refreshView.setAutoRefresh(true);  

设置是否自动刷新，默认为false。这里也可以在xml里配置，但优先级是java代码里更高一些，也就是说如果在xml中配置为false，但是java代码中设置为true的话，那么到时是会自动刷新的，不过话又说回来，谁会闲的蛋疼这么做呢。
[java] view plaincopy

    public static long lastRefreshTime;  

[java] view plaincopy

    //设置上次刷新的时间  
            refreshView.restoreLastRefreshTime(lastRefreshTime);  

设置上次刷新的时间，这里我先定义了一个静态long型的常量，之所以是静态是为了让程序能够一直保持他的值不变，这是我偷懒的写法，如果你真的需要在刷新的header上面显示多久之前刷新的话，你可以把这个值存到本地。lastRefreshTime是XRefreshView返回供应用持久化存储用的，具体在代码里是这样的：
[java] view plaincopy

    refreshView.setXRefreshViewListener(new XRefreshViewListener() {  
      
                @Override  
                public void onRefresh() {  
      
                    new Handler().postDelayed(new Runnable() {  
                        @Override  
                        public void run() {  
                            refreshView.stopRefresh();  
                            lastRefreshTime = refreshView.getLastRefreshTime();  
                        }  
                    }, 2000);  
                }  
      
                @Override  
                public void onLoadMore() {  
                    new Handler().postDelayed(new Runnable() {  
      
                        @Override  
                        public void run() {  
                            refreshView.stopLoadMore();  
                        }  
                    }, 2000);  
                }  
            });  

这里正真涉及到了上拉和下拉刷新这块，如果想要监听刷新事件，需要先注册监听，方法是refreshView.setXRefreshViewListener，参数是一个刷新的监听接口XRefreshViewListener，其中onRefresh是在下拉刷新时会回调，而onLoadMore则在上拉刷新时会回调，你需要做的就是在这里面处理数据的刷新，等到数据刷新完成以后，XRefreshView分别提供了stopRefresh和stopLoadMore来分别停止下拉刷新和来停止上拉刷新的UI。前面说到lastRefreshTime，在停止刷新的时候，会通过getLastRefreshTime这个方法把这次的刷新时间传递给你，你就可以做本地化存储，以供下次使用。

3.2自定义view

如果想要刷新自定义view，XRefreshView也是提供支持的。
[java] view plaincopy

    refreshView.setRefreshBase(new XRefreshContentViewBase() {  
      
                @Override  
                public boolean isTop() {  
                    return stickyLv.getFirstVisiblePosition() == 0;  
                }  
      
                @Override  
                public boolean isBottom() {  
                    return stickyLv.getLastVisiblePosition() == mTotalItemCount - 1;  
                }  
            });  

自定义view需要自己实现刷新规则来告诉XRefreshView什么时候自定义view到达了顶部，什么时候到达了底部。怎么样？是不是很方便！XRefreshView只需要你提供自定义view需要刷新的时机，其他一切都放心的交给XRefreshView吧，他会帮你解决一切！

接下来，XRefreshView会把刷新的header和footer丢出来，这样，在应用里就可以就可以定义自己的header和footer了。

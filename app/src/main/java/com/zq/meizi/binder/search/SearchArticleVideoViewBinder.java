package com.zq.meizi.binder.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.zq.meizi.ErrorAction;
import com.zq.meizi.IntentAction;
import com.zq.meizi.R;
import com.zq.meizi.api.IMobileSearchApi;
import com.zq.meizi.api.INewsApi;
import com.zq.meizi.bean.news.MultiNewsArticleDataBean;
import com.zq.meizi.bean.search.SearchVideoInfoBean;
import com.zq.meizi.utils.ImageLoader;
import com.zq.meizi.utils.NetWorkUtil;
import com.zq.meizi.utils.RetrofitFactory;
import com.zq.meizi.utils.SettingUtil;
import com.zq.meizi.utils.TimeUtil;
import com.zq.meizi.widget.CircleImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.drakeet.multitype.ItemViewBinder;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by Meiji on 2017/6/8.
 */

public class SearchArticleVideoViewBinder extends ItemViewBinder<MultiNewsArticleDataBean, SearchArticleVideoViewBinder.ViewHolder> {

    private static final String TAG = "NewsArticleHasVideoView";

    @NonNull
    @Override
    protected SearchArticleVideoViewBinder.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_news_article_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final SearchArticleVideoViewBinder.ViewHolder holder, @NonNull final MultiNewsArticleDataBean item) {

        final Context context = holder.itemView.getContext();

        try {
            String image = null;
            if (null != item.getMiddle_image()) {
                image = item.getMiddle_image().getUrl();
                if (!TextUtils.isEmpty(image)) {
                    if (NetWorkUtil.isWifiConnected(context)) {
                        // 加载高清图
                        image = image.replace("list", "large");
                    }
                    ImageLoader.loadCenterCrop(context, image, holder.iv_video_image, R.color.viewBackground, R.mipmap.error_image);
                }
            } else {
                holder.iv_video_image.setImageResource(R.mipmap.error_image);
            }

            if (null != item.getUser_info()) {
                String avatar_url = item.getUser_info().getAvatar_url();
                if (!TextUtils.isEmpty(avatar_url)) {
                    ImageLoader.loadCenterCrop(context, avatar_url, holder.iv_media, R.color.viewBackground);
                }
            }

            String tv_title = item.getTitle();
            String tv_source = item.getSource();
            String tv_comment_count = item.getComment_count() + "评论";
            String tv_datetime = item.getBehot_time() + "";
            if (!TextUtils.isEmpty(tv_datetime)) {
                tv_datetime = TimeUtil.getTimeStampAgo(tv_datetime);
            }
            int video_duration = item.getVideo_duration();
            String min = String.valueOf(video_duration / 60);
            String second = String.valueOf(video_duration % 10);
            if (Integer.parseInt(second) < 10) {
                second = "0" + second;
            }
            String tv_video_time = min + ":" + second;

            holder.tv_title.setText(tv_title);
            holder.tv_title.setTextSize(SettingUtil.getInstance().getTextSize());
            holder.tv_extra.setText(tv_source + " - " + tv_comment_count + " - " + tv_datetime);
            holder.tv_video_time.setText(tv_video_time);

            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setTitle(R.string.loading);

            final String finalImage = image;
            RxView.clicks(holder.itemView)
                    .throttleFirst(1300, TimeUnit.MILLISECONDS)
                    .doOnNext(new Consumer<Object>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Object o) throws Exception {
                            dialog.show();
                        }
                    })
                    .observeOn(Schedulers.io())
                    .switchMap(new Function<Object, ObservableSource<String>>() {
                        @Override
                        public ObservableSource<String> apply(@io.reactivex.annotations.NonNull Object o) throws Exception {
                            String url = item.getDisplay_url();
                            try {
                                Response<ResponseBody> response = RetrofitFactory.getRetrofit().create(INewsApi.class)
                                        .getNewsContentRedirectUrl(url).execute();
                                // 获取重定向后的 URL 用于拼凑API
                                if (response.isSuccessful()) {
                                    String httpUrl = response.raw().request().url().toString();
                                    if (!TextUtils.isEmpty(httpUrl) && httpUrl.contains("toutiao")) {
                                        String api = httpUrl + "info/";
                                        return Observable.just(api);
                                    } else {
                                        return null;
                                    }
                                } else {
                                    return null;
                                }
                            } catch (Exception e) {
                                ErrorAction.print(e);
                            }
                            return null;
                        }
                    })
                    .switchMap(new Function<String, ObservableSource<SearchVideoInfoBean>>() {
                        @Override
                        public ObservableSource<SearchVideoInfoBean> apply(@io.reactivex.annotations.NonNull String s) throws Exception {
                            return RetrofitFactory.getRetrofit().create(IMobileSearchApi.class)
                                    .getSearchVideoInfo(s);
                        }
                    })
                    .map(new Function<SearchVideoInfoBean, MultiNewsArticleDataBean>() {
                        @Override
                        public MultiNewsArticleDataBean apply(@io.reactivex.annotations.NonNull SearchVideoInfoBean bean) throws Exception {
                            // 获取视频 ID
                            String content = bean.getData().getContent();
                            if (!TextUtils.isEmpty(content)) {
                                Map<String, String> map = parseJson(content);
                                String id = map.get("id");
                                String imageUrl = map.get("imageUrl");
                                item.setVideo_id(id);
                                MultiNewsArticleDataBean.VideoDetailInfoBean.DetailVideoLargeImageBean videobean = new MultiNewsArticleDataBean.VideoDetailInfoBean.DetailVideoLargeImageBean();
                                MultiNewsArticleDataBean.VideoDetailInfoBean videoDetail = new MultiNewsArticleDataBean.VideoDetailInfoBean();
                                videobean.setUrl(finalImage);
                                videoDetail.setDetail_video_large_image(videobean);
                                item.setVideo_detail_info(videoDetail);
                            }
                            return item;
                        }
                    })
                    .subscribe(new Consumer<MultiNewsArticleDataBean>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull MultiNewsArticleDataBean dataBean) throws Exception {
                            dialog.dismiss();
//                            VideoContentActivity.launch(dataBean);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                            dialog.dismiss();
                            ErrorAction.print(throwable);
                        }
                    });
            holder.iv_dots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(context,
                            holder.iv_dots, Gravity.END, 0, R.style.MyPopupMenu);
                    popupMenu.inflate(R.menu.menu_share);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menu) {
                            int itemId = menu.getItemId();
                            if (itemId == R.id.action_share) {
                                IntentAction.send(context, item.getTitle() + "\n" + item.getShare_url());
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        } catch (Exception e) {
            ErrorAction.print(e);
        }
    }

    private Map<String, String> parseJson(String content) {
        Document doc = Jsoup.parse(content);
        Elements elements = doc.getElementsByClass("tt-video-box");
        String id = elements.get(0).attr("tt-videoid");
        String imageUrl = elements.get(0).attr("tt-poster");
        Map<String, String> map = new HashMap<>();
        if (!TextUtils.isEmpty(id)) {
            map.put("id", id);
        }
        if (!TextUtils.isEmpty(imageUrl)) {
            map.put("imageUrl", imageUrl);
        }
        return map;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView iv_media;
        private TextView tv_extra;
        private TextView tv_title;
        private ImageView iv_video_image;
        private TextView tv_video_time;
        private ImageView iv_dots;

        ViewHolder(View itemView) {
            super(itemView);
            this.iv_media = itemView.findViewById(R.id.iv_media);
            this.tv_extra = itemView.findViewById(R.id.tv_extra);
            this.tv_title = itemView.findViewById(R.id.tv_title);
            this.iv_video_image = itemView.findViewById(R.id.iv_video_image);
            this.tv_video_time = itemView.findViewById(R.id.tv_video_time);
            this.iv_dots = itemView.findViewById(R.id.iv_dots);
        }
    }
}

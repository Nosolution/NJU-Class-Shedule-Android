package com.lilystudio.wheretosleepinnju.course;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.lilystudio.wheretosleepinnju.R;
import com.lilystudio.wheretosleepinnju.app.app;
import com.lilystudio.wheretosleepinnju.data.bean.Course;
import com.lilystudio.wheretosleepinnju.data.db.CourseDbDao;
import com.lilystudio.wheretosleepinnju.utils.ImageResizer;
import com.lilystudio.wheretosleepinnju.utils.LogUtil;
import com.lilystudio.wheretosleepinnju.utils.Preferences;
import com.lilystudio.wheretosleepinnju.utils.ScreenUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by mnnyang on 17-10-19.
 */

public class CoursePresenter implements CourseContract.Presenter {

    private CourseContract.View mCourseView;

    public CoursePresenter(CourseContract.View mCourseView) {
        this.mCourseView = mCourseView;
    }

    @Override
    public void start() {
        //nothing
    }

    @Override
    public void loadBackground() {
        String path = Preferences.getString(app.mContext.getString(R.string.app_preference_bg_iamge_path), "");
        if (!TextUtils.isEmpty(path)) {
            loadImage(path);
        }else{
            LogUtil.e(this,"no background");
        }
    }

    private void loadImage(final String path) {
        Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(path,
                        ScreenUtils.getSWidth(), 0);
                if (bitmap == null){
                    subscriber.onError(new FileNotFoundException());
                }else{
                    subscriber.onNext(bitmap);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        mCourseView.setBackground(bitmap);
                    }
                });

    }

    @Override
    public void updateCourseViewData(final int csNameId) {
        Observable.create(new Observable.OnSubscribe<ArrayList<Course>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Course>> subscriber) {
                CourseDbDao dao = CourseDbDao.newInstance();
                final ArrayList<Course> courses = dao.loadCourses(csNameId);
                subscriber.onNext(courses);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<Course>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ArrayList<Course> courses) {
                        mCourseView.setCourseData(courses);
                    }
                });
    }

    @Override
    public void deleteCourse(int courseId) {
        CourseDbDao.newInstance().removeCourse(courseId);
        mCourseView.updateCoursePreference(); //must be main thread
    }
}

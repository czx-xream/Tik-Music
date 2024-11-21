package com.example.tikmusic;

import static com.example.widget.DiscView.DURATION_NEEDLE_ANIAMTOR;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.Service.MusicService;
import com.example.model.MusicData;
import com.example.utils.DisplayUtil;
import com.example.utils.FastBlurUtil;
import com.example.widget.BackgourndAnimationRelativeLayout;
import com.example.widget.DiscView;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayMusicFragment extends Fragment implements DiscView.IPlayInfo , View
        .OnClickListener {

    private DiscView mDisc;
    private Toolbar mToolbar;
    private SeekBar mSeekBar;
    private ImageView mIvPlayOrPause, mIvNext, mIvLast;
    private TextView mTvMusicDuration,mTvTotalMusicDuration;
    private BackgourndAnimationRelativeLayout mRootLayout;
    public static final int MUSIC_MESSAGE = 0;

    public static final String PARAM_MUSIC_LIST = "PARAM_MUSIC_LIST";

    @SuppressLint("HandlerLeak")
    private Handler mMusicHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mSeekBar.setProgress(mSeekBar.getProgress() + 1000);
            mTvMusicDuration.setText(duration2Time(mSeekBar.getProgress()));
            startUpdateSeekBarProgress();
        }
    };

    private MusicReceiver mMusicReceiver = new MusicReceiver();
    private List<MusicData> mMusicDatas = new ArrayList<>();
    @Override
    public void onAttach(@NotNull Context context){
        super.onAttach(context);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMusicDatas();
        initMusicReceiver();
        /*initial except Widgets*/
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_play_music, container, false);

                initView(rootView);


        /*Init View*/
//        mDisc = rootView.findViewById(R.id.discview);
//        mIvNext = rootView.findViewById(R.id.ivNext);
//        mIvLast = rootView.findViewById(R.id.ivLast);
//        mIvPlayOrPause = rootView.findViewById(R.id.ivPlayOrPause);
//        mTvMusicDuration = rootView.findViewById(R.id.tvCurrentTime);
//        mTvTotalMusicDuration = rootView.findViewById(R.id.tvTotalTime);
//        mSeekBar = rootView.findViewById(R.id.musicSeekBar);
//        mRootLayout = rootView.findViewById(R.id.rootLayout);

        /*Init View End*/

        makeStatusBarTransparent();

        return rootView;
    }


    private void initView(View rootView) {
        /*Bind the widgets to variable*/
        mDisc = rootView.findViewById(R.id.discview);
        mIvNext = rootView.findViewById(R.id.ivNext);
        mIvLast = rootView.findViewById(R.id.ivLast);
        mIvPlayOrPause = rootView.findViewById(R.id.ivPlayOrPause);
        mTvMusicDuration = rootView.findViewById(R.id.tvCurrentTime);
        mTvTotalMusicDuration = rootView.findViewById(R.id.tvTotalTime);
        mSeekBar = rootView.findViewById(R.id.musicSeekBar);
        mRootLayout = rootView.findViewById(R.id.rootLayout);

        mToolbar = rootView.findViewById(R.id.my_toolbar);


//        mDisc = (DiscView) mDisc.findViewById(R.id.discview);
//        mIvNext = (ImageView) mIvNext.findViewById(R.id.ivNext);
//        mIvLast = (ImageView) mIvLast.findViewById(R.id.ivLast);
//        mIvPlayOrPause = (ImageView) mIvPlayOrPause.findViewById(R.id.ivPlayOrPause);
//        mTvMusicDuration = (TextView) mTvMusicDuration.findViewById(R.id.tvCurrentTime);
//        mTvTotalMusicDuration = (TextView) mTvTotalMusicDuration.findViewById(R.id.tvTotalTime);
//        mSeekBar = (SeekBar) mSeekBar.findViewById(R.id.musicSeekBar);
//        mRootLayout = (BackgourndAnimationRelativeLayout) mRootLayout.findViewById(R.id.rootLayout);
//
//        mToolbar = (Toolbar) mToolbar.findViewById(R.id.my_toolbar);

//        setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        /*Bind END*/

//         mDisc.setPlayInfoListener(this);
//        /*Call back on View.OnClick()*/
//        mIvLast.setOnClickListener(this);
//        mIvNext.setOnClickListener(this);
//        mIvPlayOrPause.setOnClickListener(this);

        mDisc.setPlayInfoListener((DiscView.IPlayInfo) this);
        /*Call back on View.OnClick()*/
        mIvLast.setOnClickListener((View.OnClickListener) this);
        mIvNext.setOnClickListener((View.OnClickListener) this);
        mIvPlayOrPause.setOnClickListener((View.OnClickListener) this);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvMusicDuration.setText(duration2Time(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdateSeekBarProgree();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(seekBar.getProgress());
                startUpdateSeekBarProgress();
            }
        });

        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
        mDisc.setMusicDataList(mMusicDatas);
    }

    private void stopUpdateSeekBarProgree() {
        mMusicHandler.removeMessages(MUSIC_MESSAGE);
    }
    private void makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
            Window window = getActivity().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    private void initMusicDatas() {

        MusicData musicData1 = new MusicData(R.raw.music1, R.raw.ic_music1, "寻", "三亩地");
        MusicData musicData2 = new MusicData(R.raw.music2, R.raw.ic_music2, "Nightingale", "YANI");
        MusicData musicData3 = new MusicData(R.raw.music3, R.raw.ic_music3, "Cornfield Chase", "Hans Zimmer");

        mMusicDatas.add(musicData1);
        mMusicDatas.add(musicData2);
        mMusicDatas.add(musicData3);
        Log.d("MusicService", "Loaded Music: Name = " + musicData1.getMusicName() +
                ", Author = " + musicData1.getMusicAuthor() +
                ", ResId = " + musicData1.getMusicRes());
        /*Start Sevice*/
        Intent intent = new Intent(getContext(), MusicService.class);
//        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(PARAM_MUSIC_LIST, (Serializable) mMusicDatas);
        /*在这里，MainActivity 并不知道 MusicService 的具体实现细节，只是通过 Intent 传递数据，
        服务读取这些数据后完成任务。Intent 就像一个消息传递的信使，负责携带数据，但双方不需要直接关联*/
        requireActivity().startService(intent);
//        startService(intent);

    }
    private void initMusicReceiver() {
        Context context = requireContext();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_DURATION);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_COMPLETE);
        /*注册本地广播*/
        LocalBroadcastManager.getInstance(context).registerReceiver(mMusicReceiver,intentFilter);
    }

        private void try2UpdateMusicPicBackground(final int musicPicRes) {
        if (mRootLayout.isNeed2UpdateBackground(musicPicRes)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRootLayout.setForeground(foregroundDrawable);
                            mRootLayout.beginAnimation();
                        }
                    });
                }
            }).start();
        }
    }

    private Drawable getForegroundDrawable(int musicPicRes) {
        /*得到屏幕的宽高比，以便按比例切割图片一部分*/
        final float widthHeightSize = (float) (DisplayUtil.getScreenWidth(getContext())
                * 1.0 / DisplayUtil.getScreenHeight(getContext()));
//                final float widthHeightSize = (float) (DisplayUtil.getScreenWidth(MainActivity.this)
//                * 1.0 / DisplayUtil.getScreenHeight(getContext()) * 1.0);

        Bitmap bitmap = getForegroundBitmap(musicPicRes);
        int cropBitmapWidth = (int) (widthHeightSize * bitmap.getHeight());
        int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);

        /*切割部分图片*/
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth,
                bitmap.getHeight());
        /*缩小图片*/
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50, bitmap
                .getHeight() / 50, false);
        /*模糊化*/
        final Bitmap blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 8, true);

        final Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
        /*加入灰色遮罩层，避免图片过亮影响其他控件*/
        foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return foregroundDrawable;
    }

    private Bitmap getForegroundBitmap(int musicPicRes) {
        int screenWidth = DisplayUtil.getScreenWidth(getContext());
        int screenHeight = DisplayUtil.getScreenHeight(getContext());

//        int screenWidth = DisplayUtil.getScreenWidth(this);
//        int screenHeight = DisplayUtil.getScreenHeight(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), musicPicRes, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        if (imageWidth < screenWidth && imageHeight < screenHeight) {
            return BitmapFactory.decodeResource(getResources(), musicPicRes);
        }

        int sample = 2;
        int sampleX = imageWidth / DisplayUtil.getScreenWidth(getContext());
        int sampleY = imageHeight / DisplayUtil.getScreenHeight(getContext());

//        int sampleX = imageWidth / DisplayUtil.getScreenWidth(this);
//        int sampleY = imageHeight / DisplayUtil.getScreenHeight(this);

        if (sampleX > sampleY && sampleY > 1) {
            sample = sampleX;
        } else if (sampleY > sampleX && sampleX > 1) {
            sample = sampleY;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(getResources(), musicPicRes, options);
    }

    @Override
    public void onMusicInfoChanged(String musicName, String musicAuthor) {
//        getSupportActionBar().setTitle(musicName);
//        getSupportActionBar().setSubtitle(musicAuthor);
        //AppcompatAcitivity的方法
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(musicName);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(musicAuthor);
    }

    @Override
    public void onMusicPicChanged(int musicPicRes) {
        try2UpdateMusicPicBackground(musicPicRes);
    }

        @Override
    public void onMusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {
        switch (musicChangedStatus) {
            case PLAY:{
                play();
                break;
            }
            case PAUSE:{
                pause();
                break;
            }
            case NEXT:{
                next();
                break;
            }
            case LAST:{
                last();
                break;
            }
            case STOP:{
                stop();
                break;
            }
        }
    }

        @Override
    public void onClick(View v) {
        if (v == mIvPlayOrPause) {
            mDisc.playOrPause();
        } else if (v == mIvNext) {
            mDisc.next();
        } else if (v == mIvLast) {
            mDisc.last();
        }
    }

    private void play() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
        startUpdateSeekBarProgress();
    }

    private void pause() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PAUSE);
        stopUpdateSeekBarProgree();
    }

    private void stop() {
        stopUpdateSeekBarProgree();
        mIvPlayOrPause.setImageResource(R.drawable.ic_play);
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
        mSeekBar.setProgress(0);
    }

    private void next() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    private void last() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_LAST);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    private void complete(boolean isOver) {
        if (isOver) {
            mDisc.stop();
        } else {
            mDisc.next();
        }
    }

    private void optMusic(final String action) {
//        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(action));
    }

    private void seekTo(int position) {
        Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_SEEK_TO);
        intent.putExtra(MusicService.PARAM_MUSIC_SEEK_TO,position);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void startUpdateSeekBarProgress() {
        /*避免重复发送Message*/
        stopUpdateSeekBarProgree();
        mMusicHandler.sendEmptyMessageDelayed(0,1000);
    }

    /*根据时长格式化称时间文本*/
    private String duration2Time(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        return (min < 10 ? "0" + min : min + "") + ":" + (sec < 10 ? "0" + sec : sec + "");
    }

    private void updateMusicDurationInfo(int totalDuration) {
        mSeekBar.setProgress(0);
        mSeekBar.setMax(totalDuration);
        mTvTotalMusicDuration.setText(duration2Time(totalDuration));
        mTvMusicDuration.setText(duration2Time(0));
        startUpdateSeekBarProgress();
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                mIvPlayOrPause.setImageResource(R.drawable.ic_pause);
                int currentPosition = intent.getIntExtra(MusicService.PARAM_MUSIC_CURRENT_POSITION, 0);
                mSeekBar.setProgress(currentPosition);
                if(!mDisc.isPlaying()){
                    mDisc.playOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PAUSE)) {
                mIvPlayOrPause.setImageResource(R.drawable.ic_play);
                if (mDisc.isPlaying()) {
                    mDisc.playOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_DURATION)) {
                int duration = intent.getIntExtra(MusicService.PARAM_MUSIC_DURATION, 0);
                updateMusicDurationInfo(duration);
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_COMPLETE)) {
                boolean isOver = intent.getBooleanExtra(MusicService.PARAM_MUSIC_IS_OVER, true);
                complete(isOver);
            }
        }
    }


}



package com.scut.picturelibrary.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.scut.picturelibrary.R;
import com.scut.picturelibrary.manager.SurfaceViewManager;

/**
 * 视频播放界面
 * 
 * @author cyc
 * 
 */
public class VideoActivity extends Activity implements OnClickListener,
		OnTouchListener {

	public final static int MEDIA_TYPE_VIDEO = 2;
	// 视频播放进度条
	private SeekBar mVideoSeekBar;
	// 视频播放时间
	private TextView mVideoTime;
	// 上一个视频
	private ImageButton mVideoPre;
	// 播放视频
	private ImageButton mVideoPlay;
	// 下一个视频
	private ImageButton mVideoNext;
	// 视频播放界面
	private LinearLayout mVideoView;

	private LinearLayout llPlayLayout;

	private LinearLayout llSeekBarLayout;

	private SurfaceViewManager mSurfaceViewManager;

	private Display mDisplay;

	private MediaPlayer mediaPlayer;
	// 播放的文件路径
	private String filePath;
	// 窗口进入动画
	private Animation windowInAnimation;
	// 窗口退出动画
	private Animation windowOutAnimation;
	// 视频播放当前时间
	private int currentTime;
	// 声音管理器
	private AudioManager am;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);

		Intent intent = getIntent();
		filePath = intent.getStringExtra("filePath");

		llPlayLayout = (LinearLayout) findViewById(R.id.ll_video_play);
		llSeekBarLayout = (LinearLayout) findViewById(R.id.ll_video_seekbar);

		mVideoSeekBar = (SeekBar) findViewById(R.id.seb_video);
		mVideoSeekBar.setOnSeekBarChangeListener(change);
		mVideoTime = (TextView) findViewById(R.id.tv_video_time);
		mVideoPre = (ImageButton) findViewById(R.id.ibtn_video_pre);
		mVideoPre.setOnClickListener(this);
		mVideoPlay = (ImageButton) findViewById(R.id.ibtn_video_play);
		mVideoPlay.setOnClickListener(this);
		mVideoNext = (ImageButton) findViewById(R.id.ibtn_video_next);
		mVideoNext.setOnClickListener(this);

		windowInAnimation = AnimationUtils
				.loadAnimation(this, R.anim.window_in);
		windowOutAnimation = AnimationUtils.loadAnimation(this,
				R.anim.window_out);

		// 传入的第二个参数为媒体类型，第三个参数为文件路径
		mSurfaceViewManager = new SurfaceViewManager(this, MEDIA_TYPE_VIDEO,
				filePath);
		mediaPlayer = mSurfaceViewManager.getMyMediaPlayer();

		mVideoView = (LinearLayout) findViewById(R.id.ll_vedio_view);
		mVideoView.setOnTouchListener(this);
		mVideoView.addView(mSurfaceViewManager);

		am = (AudioManager) getSystemService(AUDIO_SERVICE);
		int result = am.requestAudioFocus(afChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		// 永久获取媒体焦点
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			initVideo();
		}

	}

	// 加载MediaPlayer
	private void initVideo() {
		mSurfaceViewManager.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(final MediaPlayer mp) {
				mp.start();
				mp.seekTo(0);
				mVideoSeekBar.setMax(mp.getDuration());
				updateThread();
			}

		});
	}

	// 更新seekbar和时间
	private void updateThread() {
		new Thread() {

			@Override
			public void run() {
				while (mediaPlayer.isPlaying()) {
					try {
						currentTime = mediaPlayer.getCurrentPosition();
						Message msg = new Message();
						msg.what = currentTime;
						handler.sendMessage(msg);
						sleep(1000);
					} catch (Exception e) {

					}
				}
			}

		}.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				setOrientationVideoLayout(mediaPlayer); // 竖屏情况下的缩放
			} else {
				LayoutParams lpLandscape = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				// 横屏则不缩放
				mSurfaceViewManager.setLayoutParams(lpLandscape);
			}

			mVideoSeekBar.setProgress(msg.what);
			mVideoTime.setText(mSurfaceViewManager.ShowTime(msg.what));
		}
	};

	// 设置视频缩放
	private void setOrientationVideoLayout(MediaPlayer mp) {
		// 获得视频的高度和宽度
		int mVideoWidth = mp.getVideoWidth();
		int mVideoHeight = mp.getVideoHeight();
		mDisplay = getWindowManager().getDefaultDisplay();
		// 如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
		if (mVideoWidth > mDisplay.getWidth()
				|| mVideoHeight > mDisplay.getHeight()) {
			float wRatio = (float) mVideoWidth / (float) mDisplay.getWidth();
			float hRatio = (float) mVideoHeight / (float) mDisplay.getHeight();
			// 选择大的一个进行缩放
			float ratio = Math.max(wRatio, hRatio);
			mVideoWidth = (int) Math.ceil((float) mVideoWidth / ratio);
			mVideoHeight = (int) Math.ceil((float) mVideoHeight / ratio);
			LayoutParams lpPortrait = new LayoutParams(mVideoWidth,
					mVideoHeight);
			mSurfaceViewManager.setLayoutParams(lpPortrait);

		}
	}

	// 控制控件所在布局的进出
	@Override
	public boolean onTouch(View view, MotionEvent event) {

		int eventaction = event.getAction();
		if (view.getId() != R.id.seb_video) {
			if (eventaction == MotionEvent.ACTION_DOWN) {
				if (llPlayLayout.getVisibility() == View.VISIBLE) {
					llPlayLayout.setVisibility(View.GONE);
					llPlayLayout.startAnimation(windowInAnimation);
				} else {
					llPlayLayout.setVisibility(View.VISIBLE);
					llPlayLayout.startAnimation(windowOutAnimation);
				}
				if (llSeekBarLayout.getVisibility() == View.VISIBLE) {
					llSeekBarLayout.setVisibility(View.GONE);
					llSeekBarLayout.startAnimation(windowOutAnimation);
				} else {
					llSeekBarLayout.setVisibility(View.VISIBLE);
					llSeekBarLayout.startAnimation(windowInAnimation);

				}
			}
		}

		return false;
	}

	// 进度条监听
	private OnSeekBarChangeListener change = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			mediaPlayer.seekTo(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {

		}

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

		}
	};

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.ibtn_video_pre:

			break;
		case R.id.ibtn_video_play:

			// 如果没有正在播放视频，则播放
			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.start();
				updateThread();
				mVideoPlay.setImageDrawable(getResources().getDrawable(
						R.drawable.img_video_play));
			} else if (mediaPlayer.isPlaying()) { // 如果正在播放视频，则暂停
				mediaPlayer.pause();
				mVideoPlay.setImageDrawable(getResources().getDrawable(
						R.drawable.img_video_pause));
			}
			break;
		case R.id.ibtn_video_next:

			break;
		}
	}

	// 如果有电话到来，则停止播放视频并记录位置
	@Override
	protected void onPause() {
		super.onPause();
		if (mediaPlayer.isPlaying()) {
			currentTime = mediaPlayer.getCurrentPosition();
			mediaPlayer.stop();
		}
	}

	// 获取播放位置
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentTime = savedInstanceState.getInt("currentTime");
	}

	// 电话结束，恢复播放位置
	@Override
	protected void onResume() {
		super.onResume();
		if (currentTime > 0) {
			mediaPlayer.start();
			mediaPlayer.seekTo(currentTime);
		}
	}

	// 保存播放位置
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentTime", currentTime);
	}

	//
	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			// 得到音频的焦点
			case AudioManager.AUDIOFOCUS_GAIN:
				if (mediaPlayer == null) {
					initVideo();
				} else if (!mediaPlayer.isPlaying()) {
					mediaPlayer.start();
				}
				break;
			// 长时间失去了这个音频的焦点
			case AudioManager.AUDIOFOCUS_LOSS:
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
					mediaPlayer.release();
					mediaPlayer = null;
				}
				am.abandonAudioFocus(afChangeListener);
				break;
			// 暂时的失去了音频的焦点,但是应该要马上回到焦点上
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				}
				break;
			// 暂时的失去了音频的焦点,但是你允许继续用小音量播放音频
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.setVolume(0.1f, 0.1f);
				}
				break;

			}

		}
	};

	// 调节媒体音量
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
							| AudioManager.FLAG_SHOW_UI);
			return true;

		case KeyEvent.KEYCODE_VOLUME_DOWN:
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
							| AudioManager.FLAG_SHOW_UI);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
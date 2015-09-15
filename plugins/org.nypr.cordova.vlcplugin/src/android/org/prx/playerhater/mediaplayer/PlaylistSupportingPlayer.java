/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.prx.playerhater.mediaplayer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.prx.playerhater.util.PlaylistParser;
import org.prx.playerhater.mediaplayer.Player.StateChangeListener;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;

public class PlaylistSupportingPlayer extends SynchronousPlayer implements
		StateChangeListener {
	private static final String HTTP = "http";
	private static final String HTTPS = "https";
	private Uri[] mPlaylist;
	private Context mContext = null;
	private int mQueuePosition = 0;
	private int streamType = -1;
	private PlaylistSupportingPlayer mCurrentPlayer = this;
	private PlaylistSupportingPlayer mNextPlayer = null;
	private boolean mDieOnCompletion = false;

	private LoadPlaylistTask mLoadPlaylistTask;
	private boolean mPreparingPlaylist = false;
	private float mLeftVolume;
	private float mRightVolume;

	@Override
	public synchronized void setDataSource(Context context, Uri uri)
			throws IllegalStateException, IOException,
			IllegalArgumentException, SecurityException {
		if (mLoadPlaylistTask != null) {
			mLoadPlaylistTask.cancel(true);
		}
		mPreparingPlaylist = false;
		if (mNextPlayer != null) {
			if (mNextPlayer != this) {
				mNextPlayer.release();
			}
			mNextPlayer = null;
		}
		if (mCurrentPlayer != this) {
			if (mCurrentPlayer != null) {
				mCurrentPlayer.release();
			}
			mCurrentPlayer = this;
		}
		mPlaylist = null;
		mContext = null;
		mQueuePosition = 0;
		if (uri.getScheme().equals(HTTP) || uri.getScheme().equals(HTTPS)) {
			loadPlaylist(context, uri);
		} else {
			setSingleSong(context, uri);
		}
	}

	private synchronized void loadPlaylist(final Context context, final Uri uri) {
		mLoadPlaylistTask = new LoadPlaylistTask(this, context, uri);
		mLoadPlaylistTask.execute();
	}

	private synchronized void setSingleSong(Context context, Uri uri) {
		try {
			super.setDataSource(context, uri);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mCurrentPlayer = this;
		mLoadPlaylistTask = null;
		mPlaylist = null;
		mContext = null;
		if (mPreparingPlaylist) {
			prepareAsync();
		}
	}

	private synchronized void setPlaylist(Context context, Uri[] playlist) {
		mNextPlayer = newPlayer();
		try {
			mNextPlayer.setDataSource(context, playlist[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setSingleSong(context, playlist[0]);
		mPlaylist = playlist;
		if (playlist.length > 2) {
			mContext = context;
		}
	}

	@Override
	public void prepareAsync() {
		if (mLoadPlaylistTask != null && !mPreparingPlaylist) {
			mPreparingPlaylist = true;
			onStateChanged();
		} else {
			mPreparingPlaylist = false;
			if (mCurrentPlayer == this) {
				super.prepareAsync();
			} else {
				mCurrentPlayer.prepareAsync();
			}
			if (mNextPlayer != null) {
				mNextPlayer.prepareAsync();
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		boolean handled = false;
		if (super.equals(mp)) { // This came from our own player.
			handled = super.onError(mp, what, extra);
		} else { // We're getting this callback from one of our other players.
			handled = super.onError(getBarePlayer(), what, extra);
		}
		if (!handled) {
			mDieOnCompletion = true;
		}
		return handled;
	}

	@Override
	public synchronized void onCompletion(MediaPlayer mp) {
		if (mDieOnCompletion) {
			mDieOnCompletion = false;
		} else if (mPlaylist != null) {
			mQueuePosition += 1;
			if (mQueuePosition < mPlaylist.length) {
				PlaylistSupportingPlayer tmp = mCurrentPlayer;
				mCurrentPlayer = mNextPlayer;
				mNextPlayer = tmp;
				mCurrentPlayer.startWithFade();
				if (mQueuePosition + 1 < mPlaylist.length) {
					mNextPlayer.reset();
					try {
						mNextPlayer.setDataSource(mContext,
								mPlaylist[mQueuePosition + 1]);
						mNextPlayer.prepareAsync();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					if (mNextPlayer != this) {
						mNextPlayer.release();
					}
					mNextPlayer = null;
				}
				return;
			}
		}
		super.onCompletion(getBarePlayer());
	}

	@Override
	public void onStateChanged(Player mediaPlayer, int state) {
		super.onStateChanged();
	}

	@Override
	public int getState() {
		if (mLoadPlaylistTask != null) {
			if (mPreparingPlaylist) {
				return StatelyPlayer.PREPARING;
			} else {
				return StatelyPlayer.INITIALIZED;
			}
		}
		if (mCurrentPlayer == this) {
			return super.getState();
		} else {
			return mCurrentPlayer.getState();
		}
	}

	@Override
	public void reset() {
		super.reset();
		if (mNextPlayer != null && mNextPlayer != this) {
			mNextPlayer.reset();
		}
		if (mCurrentPlayer != null && mCurrentPlayer != this) {
			mCurrentPlayer.reset();
		}
	}

	@Override
	public void release() {
		super.release();
		if (mNextPlayer != null && mNextPlayer != this) {
			mNextPlayer.release();
		}
		if (mCurrentPlayer != null && mCurrentPlayer != this) {
			mCurrentPlayer.release();
		}
	}

	@Override
	public void start() throws IllegalStateException {
		if (mCurrentPlayer == this) {
			super.start();
		} else {
			mCurrentPlayer.start();
		}
	}

	public void startWithFade() throws IllegalStateException {
		setVolume(0, 0);
		start();
		final Timer timer = new Timer(true);
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				setVolume(mLeftVolume + 0.1f, mRightVolume + 01.f);
				if (mLeftVolume >= 1.0f || mRightVolume >= 1.0f) {
					timer.cancel();
					timer.purge();
				}
			}

		};

		timer.schedule(timerTask, 200, 200);
	}

	@Override
	public void pause() throws IllegalStateException {
		if (mCurrentPlayer == this) {
			super.pause();
		} else {
			mCurrentPlayer.pause();
		}
	}

	@Override
	public void stop() throws IllegalStateException {
		if (mCurrentPlayer == this) {
			super.stop();
		} else {
			mCurrentPlayer.stop();
		}
	}

	@Override
	public void seekTo(int msec) {
		if (mCurrentPlayer == this) {
			super.seekTo(msec);
		} else {
			mCurrentPlayer.seekTo(msec);
		}
	}

	@Override
	public boolean isPlaying() {
		if (mCurrentPlayer == this) {
			return super.isPlaying();
		} else {
			return mCurrentPlayer.isPlaying();
		}
	}

	@Override
	public int getCurrentPosition() {
		if (mCurrentPlayer == this) {
			return super.getCurrentPosition();
		} else {
			return mCurrentPlayer.getCurrentPosition();
		}
	}

	@Override
	public int getDuration() {
		int duration = super.getDuration();
		if (mCurrentPlayer != null && mCurrentPlayer != this) {
			duration += mCurrentPlayer.getDuration();
		}
		if (mNextPlayer != null && mNextPlayer != this) {
			duration += mNextPlayer.getDuration();
		}
		return duration;
	}

	@Override
	public void setAudioStreamType(int streamtype) {
		streamType = streamtype;
		super.setAudioStreamType(streamType);
		if (mCurrentPlayer != this && mCurrentPlayer != null) {
			mCurrentPlayer.setAudioStreamType(streamType);
		}
		if (mNextPlayer != null && mNextPlayer != this) {
			mNextPlayer.setAudioStreamType(streamType);
		}
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		super.setVolume(leftVolume, rightVolume);
		this.mLeftVolume = leftVolume;
		this.mRightVolume = rightVolume;
		if (mCurrentPlayer != this && mCurrentPlayer != null) {
			mCurrentPlayer.setVolume(leftVolume, rightVolume);
		}
		if (mNextPlayer != this && mNextPlayer != null) {
			mNextPlayer.setVolume(leftVolume, rightVolume);
		}
	}

	@Override
	public boolean equals(MediaPlayer mp) {
		if (mCurrentPlayer != null && mCurrentPlayer != this) {
			if (mCurrentPlayer.equals(mp)) {
				return true;
			}
		}
		if (mNextPlayer != null && mNextPlayer != this) {
			if (mNextPlayer.equals(mp)) {
				return true;
			}
		}
		return super.equals(mp);
	}

	@Override
	public boolean conditionalPlay() {
		if (mCurrentPlayer == this) {
			return super.conditionalPlay();
		} else {
			return mCurrentPlayer.conditionalPlay();
		}
	}

	@Override
	public synchronized boolean conditionalPause() {
		if (mCurrentPlayer == this) {
			return super.conditionalPause();
		} else {
			return mCurrentPlayer.conditionalPause();
		}
	}

	@Override
	public synchronized boolean conditionalStop() {
		if (mCurrentPlayer == this) {
			return super.conditionalStop();
		} else {
			return mCurrentPlayer.conditionalStop();
		}
	}

	@Override
	public synchronized boolean isWaitingToPlay() {
		if (mCurrentPlayer == this) {
			return super.isWaitingToPlay();
		} else {
			return mCurrentPlayer.isWaitingToPlay();
		}
	}

	private PlaylistSupportingPlayer newPlayer() {
		PlaylistSupportingPlayer player = new PlaylistSupportingPlayer();
		player.setOnErrorListener(this);
		player.setOnCompletionListener(this);
		player.setStateChangeListener(this);
		if (streamType != -1) {
			player.setAudioStreamType(streamType);
		}
		return player;
	}

	private static class LoadPlaylistTask extends AsyncTask<Void, Void, Uri[]> {

		private final PlaylistSupportingPlayer mPlayer;
		private final Context mContext;
		private final Uri mUri;

		private Uri mFirstUri;
		private Uri[] mPlaylist;

		private LoadPlaylistTask(PlaylistSupportingPlayer player,
				Context context, Uri uri) {
			mPlayer = player;
			mContext = context;
			mUri = uri;
		}

		@Override
		protected Uri[] doInBackground(Void... arg0) {
			mFirstUri = mUri;
			mPlaylist = PlaylistParser.parsePlaylist(mFirstUri);
			for (int depth = 0; depth < 10; depth++) {
				if (mFirstUri.equals(mPlaylist[0]) && mPlaylist.length == 1) {
					return mPlaylist;
				} else if (mPlaylist.length == 1) {
					mFirstUri = mPlaylist[0];
					mPlaylist = PlaylistParser.parsePlaylist(mFirstUri);
				} else {
					return mPlaylist;
				}
				if (isCancelled()) {
					return null;
				}
			}
			throw new IllegalStateException("playlist depth too deep!");
		}

		@Override
		protected void onPostExecute(Uri[] result) {
			if (result.length == 1) {
				mPlayer.setSingleSong(mContext, result[0]);
			} else {
				mPlayer.setPlaylist(mContext, result);
			}
		}

	}
}

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

package org.prx.playerhater.ipc;

import org.prx.playerhater.service.PlayerHaterService;
import org.prx.playerhater.songs.SongHost;
import org.prx.playerhater.wrappers.ThreadsafeServicePlayerHater;

import android.app.Notification;
import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

public class PlayerHaterServer extends IPlayerHaterServer.Stub {

	private final ThreadsafeServicePlayerHater mService;

	public PlayerHaterServer(PlayerHaterService service) {
		mService = new ThreadsafeServicePlayerHater(service);
	}

	public PlayerHaterServer(ThreadsafeServicePlayerHater playerHater) {
		mService = playerHater;
	}

	@Override
	public void setClient(IPlayerHaterClient client) throws RemoteException {
		mService.setClient(client);
	}

	@Override
	public void onRemoteControlButtonPressed(int keyCode)
			throws RemoteException {
		mService.onRemoteControlButtonPressed(keyCode);
	}

	@Override
	public void startForeground(int notificationNu, Notification notification)
			throws RemoteException {
		mService.startForeground(notificationNu, notification);
	}

	@Override
	public void stopForeground(boolean fact) throws RemoteException {
		mService.stopForeground(fact);
	}

	@Override
	public void duck() throws RemoteException {
		mService.duck();
	}

	@Override
	public void unduck() throws RemoteException {
		mService.unduck();
	}

	@Override
	public boolean pause() throws RemoteException {
		return mService.pause();
	}

	@Override
	public boolean stop() throws RemoteException {
		return mService.stop();
	}

	@Override
	public boolean resume() throws RemoteException {
		return mService.play();
	}

	@Override
	public boolean playAtTime(int startTime) throws RemoteException {
		return mService.play(startTime);
	}

	@Override
	public boolean play(int songTag, int startTime) throws RemoteException {
		return mService.play(SongHost.getSong(songTag), startTime);
	}

	@Override
	public boolean seekTo(int startTime) throws RemoteException {
		return mService.seekTo(startTime);
	}

	@Override
	public int enqueue(int songTag) throws RemoteException {
		return mService.enqueue(SongHost.getSong(songTag));
	}

	@Override
	public void enqueueAtPosition(int position, int songTag)
			throws RemoteException {
		mService.enqueue(position, SongHost.getSong(songTag));
	}

	@Override
	public boolean skipTo(int position) throws RemoteException {
		return mService.skipTo(position);
	}

	@Override
	public void skip() throws RemoteException {
		mService.skip();
	}

	@Override
	public void skipBack() throws RemoteException {
		mService.skipBack();
	}

	@Override
	public void emptyQueue() throws RemoteException {
		mService.emptyQueue();
	}

	@Override
	public int getCurrentPosition() throws RemoteException {
		return mService.getCurrentPosition();
	}

	@Override
	public int getDuration() throws RemoteException {
		return mService.getDuration();
	}

	@Override
	public int nowPlaying() throws RemoteException {
		return SongHost.getTag(mService.nowPlaying());
	}

	@Override
	public boolean isPlaying() throws RemoteException {
		return mService.isPlaying();
	}

	@Override
	public boolean isLoading() throws RemoteException {
		return mService.isLoading();
	}

	@Override
	public int getState() throws RemoteException {
		return mService.getState();
	}

	@Override
	public void setTransportControlFlags(int transportControlFlags)
			throws RemoteException {
		mService.setTransportControlFlags(transportControlFlags);
	}

	@Override
	public int getQueueLength() throws RemoteException {
		return mService.getQueueLength();
	}

	@Override
	public int getQueuePosition() throws RemoteException {
		return mService.getQueuePosition();
	}

	@Override
	public boolean removeFromQueue(int position) throws RemoteException {
		return mService.removeFromQueue(position);
	}

	@Override
	public String getSongTitle(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getTitle();
	}

	@Override
	public String getSongArtist(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getArtist();
	}

	@Override
	public String getSongAlbumTitle(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getAlbumTitle();
	}

	@Override
	public Uri getSongAlbumArt(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getAlbumArt();
	}

	@Override
	public Uri getSongUri(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getUri();
	}

	@Override
	public Bundle getSongExtra(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getExtra();
	}

	@Override
	public void setPendingIntent(PendingIntent intent) throws RemoteException {
		mService.setPendingIntent(intent);
	}

	@Override
	public void slurp(int songTag, Bundle songData) throws RemoteException {
		SongHost.slurp(songTag, songData);
	}

	@Override
	public int getTransportControlFlags() throws RemoteException {
		return mService.getTransportControlFlags();
	}
}

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
package org.prx.playerhater.songs;

import org.prx.playerhater.Song;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

class RemoteSong implements Song {

	private static SongHost.Remote getRemote() {
		return SongHost.remote();
	}

	private final int mTag;
	private Song mSong = null;

	RemoteSong(int tag) {
		mTag = tag;
	}

	@Override
	public String getTitle() {
		if (mSong != null) {
			return mSong.getTitle();
		}
		try {
			return getRemote().getSongTitle(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public String getArtist() {
		if (mSong != null) {
			return mSong.getArtist();
		}
		try {
			return getRemote().getSongArtist(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public Uri getAlbumArt() {
		if (mSong != null) {
			return mSong.getAlbumArt();
		}
		try {
			return getRemote().getSongAlbumArt(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public Uri getUri() {
		if (mSong != null) {
			return mSong.getUri();
		}
		try {
			return getRemote().getSongUri(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public Bundle getExtra() {
		if (mSong != null) {
			return mSong.getExtra();
		}
		try {
			return getRemote().getSongExtra(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public String getAlbumTitle() {
		if (mSong != null) {
			return mSong.getAlbumTitle();
		}
		try {
			return getRemote().getSongAlbumTitle(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	void setSong(Song song) {
		mSong = song;
	}
}

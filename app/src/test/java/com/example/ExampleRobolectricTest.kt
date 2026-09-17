package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entities.PlaylistEntity
import com.example.data.local.entities.TrackEntity
import com.example.data.model.MusicPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SoundSync", appName)
  }

  @Test
  fun `verify music platform display names`() {
    assertEquals("Spotify", MusicPlatform.SPOTIFY.displayName)
    assertEquals("YouTube Music", MusicPlatform.YOUTUBE_MUSIC.displayName)
    assertEquals("Unified Hub", MusicPlatform.UNIFIED.displayName)
  }

  @Test
  fun `verify metadata cleaner cleans noise but preserves titles`() {
    val cleanTitle = com.example.data.remote.MetadataCleaner.cleanTrackTitle("Blinding Lights (Official Music Video)")
    assertEquals("Blinding Lights", cleanTitle)

    val cleanArtist = com.example.data.remote.MetadataCleaner.cleanArtistName("The Weeknd - Topic")
    assertEquals("The Weeknd", cleanArtist)
  }
}


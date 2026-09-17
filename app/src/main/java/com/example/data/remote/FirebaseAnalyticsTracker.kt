package com.example.data.remote

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsTracker(context: Context) {
    private val TAG = "FirebaseAnalytics"
    private var firebaseAnalytics: FirebaseAnalytics? = null

    init {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            Log.d(TAG, "Firebase Analytics initialized successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Analytics initialization warning: ${e.message}")
        }
    }

    fun logTrackPlayed(title: String, artist: String, genre: String, platform: String, durationSec: Int) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, title)
                putString(FirebaseAnalytics.Param.ITEM_CATEGORY, genre)
                putString("artist_name", artist)
                putString("platform", platform)
                putInt("duration_sec", durationSec)
            }
            firebaseAnalytics?.logEvent("track_played", bundle)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log track_played: ${e.message}")
        }
    }

    fun logTrackLiked(title: String, artist: String, isLiked: Boolean) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, title)
                putString("artist_name", artist)
                putBoolean("is_liked", isLiked)
            }
            firebaseAnalytics?.logEvent("track_liked", bundle)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log track_liked: ${e.message}")
        }
    }

    fun logSearchQuery(query: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log search: ${e.message}")
        }
    }

    fun logAccountSync(platform: String, trackCount: Int) {
        try {
            val bundle = Bundle().apply {
                putString("platform", platform)
                putInt("tracks_synced", trackCount)
            }
            firebaseAnalytics?.logEvent("account_synced", bundle)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log account_synced: ${e.message}")
        }
    }
}

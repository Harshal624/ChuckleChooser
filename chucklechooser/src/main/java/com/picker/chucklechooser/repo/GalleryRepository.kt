package com.picker.chucklechooser.repo

import android.content.ContentResolver
import android.content.ContentUris
import android.content.res.Resources
import android.database.Cursor
import android.database.MergeCursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.repo.data.AlbumType
import com.picker.chucklechooser.repo.data.MediaItem
import com.picker.chucklechooser.repo.data.MediaType
import com.picker.chucklechooser.repo.util.MediaRepo
import org.jetbrains.annotations.TestOnly
import timber.log.Timber
import java.lang.Exception

class GalleryRepository(
    private val contentResolver: ContentResolver,
    private val resources: Resources
) : MediaRepo {

    override fun fetchAlbums(): List<AlbumItem> {
        Timber.d("fetchAlbums called")
        val mergedCursor = MergeCursor(
            arrayOf(

                // Video albums
                getMediaAlbumCursor(uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI),

                // Image albums
                getMediaAlbumCursor(uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            )
        )

        val list = arrayListOf<AlbumItem>()
        list.add(AlbumItem.getAll(resources = resources))

        try {
            mergedCursor.use { cursor ->
                while (cursor.moveToNext()) {
                    val bucketIdIndex =
                        mergedCursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID)
                    if (bucketIdIndex != -1) {
                        val bucketId =
                            mergedCursor.getString(bucketIdIndex)
                        val nameIndex =
                            mergedCursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val name =
                                mergedCursor.getString(nameIndex)
                            val albumItem = AlbumItem(
                                name = name,
                                albumType = AlbumType.OTHER,
                                bucketId = bucketId
                            )
                            if (!list.contains(albumItem)) {
                                Timber.d("Album ${albumItem.name} added to the list")
                                list.add(albumItem)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("fetchAlbums exception: $e")
        } finally {
            if (!mergedCursor.isClosed) {
                mergedCursor.close()
            }
        }

        return list
    }

    override fun fetchAllMedia(albumItem: AlbumItem, pageSize: Int, offset: Int): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val selection =
            if (albumItem.albumType == AlbumType.ALL) "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?" else "${MediaStore.Images.ImageColumns.BUCKET_ID} =? AND (${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?)"

        val selectionArgs = if (albumItem.albumType == AlbumType.ALL) {
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
        } else {
            arrayOf(
                albumItem.bucketId,
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
        }

        val query = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val bundle = Bundle()
            bundle.apply {
                putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED)
                )
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    selectionArgs
                )
                putInt(
                    ContentResolver.QUERY_ARG_LIMIT,
                    pageSize
                )
                putInt(
                    ContentResolver.QUERY_ARG_OFFSET,
                    offset
                )
            }

            contentResolver.query(
                uri,
                projection,
                bundle,
                null
            )
        } else {
            contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns.DATE_TAKEN + " DESC"} LIMIT $pageSize OFFSET $offset"
            )
        }

        val list = mutableListOf<MediaItem>()

        try {
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val mediaTypeColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val size = cursor.getInt(sizeColumn)
                    val data = cursor.getString(dataColumn)
                    val type = cursor.getInt(mediaTypeColumn)

                    var mediaType: MediaType? = null
                    var mediaUri: Uri? = null

                    when (type) {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                            mediaType = MediaType.VIDEO
                            mediaUri = ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                        }

                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                            mediaType = MediaType.IMAGE
                            mediaUri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                        }
                    }

                    if (mediaType != null && mediaUri != null) {
                        list.add(
                            MediaItem(
                                mediaType = mediaType,
                                id = id,
                                displayName = displayName,
                                size = size,
                                duration = -1,
                                mediaUri = mediaUri
                            )
                        )
                    }
                }
            } ?: Timber.e("Cursor not found")
        } catch (e: Exception) {
            Timber.e("fetchAllMedia exception: $e")
        }
        Timber.d("fetchAllMedia ${list.size} media found")
        return list
    }

    @TestOnly
    fun fetchAllMediaDebug(albumItem: AlbumItem): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val selection =
            if (albumItem.albumType == AlbumType.ALL) "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?" else "${MediaStore.Images.ImageColumns.BUCKET_ID} =? AND (${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?)"

        val selectionArgs = if (albumItem.albumType == AlbumType.ALL) {
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
        } else {
            arrayOf(
                albumItem.bucketId,
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
        }

        val query = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val bundle = Bundle()
            bundle.apply {
                putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED)
                )
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    selectionArgs
                )
            }

            contentResolver.query(
                uri,
                projection,
                bundle,
                null
            )
        } else {
            contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                MediaStore.Files.FileColumns.DATE_TAKEN + " DESC"
            )
        }

        val list = mutableListOf<MediaItem>()
        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val mediaTypeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val size = cursor.getInt(sizeColumn)
                val data = cursor.getString(dataColumn)
                val type = cursor.getInt(mediaTypeColumn)

                var mediaType: MediaType? = null
                var mediaUri: Uri? = null

                when (type) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                        mediaType = MediaType.VIDEO
                        mediaUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    }

                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                        mediaType = MediaType.IMAGE
                        mediaUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    }
                }

                if (mediaType != null && mediaUri != null) {
                    list.add(
                        MediaItem(
                            mediaType = mediaType,
                            id = id,
                            displayName = displayName,
                            size = size,
                            duration = -1,
                            mediaUri = mediaUri
                        )
                    )
                }
            }
        } ?: Timber.e("Cursor not found")

        return list
    }

    private fun getMediaAlbumCursor(uri: Uri): Cursor? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bundle = Bundle()
            bundle.apply {
                putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.MediaColumns.DATE_MODIFIED)
                )
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
            }

            contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.BUCKET_ID
                ),
                bundle,
                null
            )
        } else {
            contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.BUCKET_ID
                ),
                null,
                null,
                MediaStore.MediaColumns.DATE_TAKEN + " DESC"
            )
        }
    }
}
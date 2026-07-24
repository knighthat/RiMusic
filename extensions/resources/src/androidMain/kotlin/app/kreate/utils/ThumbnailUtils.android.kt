package app.kreate.utils

import android.net.Uri
import androidx.core.net.toUri


fun Uri?.thumbnail( size: Int ): Uri? = toString().thumbnail( size )?.toUri()
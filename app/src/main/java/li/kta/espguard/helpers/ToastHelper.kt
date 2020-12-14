package li.kta.espguard.helpers

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object ToastHelper {
    fun toast(context: Context, @StringRes resId: Int, vararg formatArgs: Any): Unit =
            toast(context, context.getString(resId, *formatArgs))

    private fun toast(context: Context, message: String): Unit =
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
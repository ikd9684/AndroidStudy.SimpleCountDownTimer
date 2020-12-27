package jp.co.excite_software.s_ikeda.simple_countdown_timer

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


abstract class CustomDialogFragment(
    @LayoutRes private val layoutResID: Int,
    private val cancellable: Boolean = true
) :
    DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(layoutResID, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(cancellable)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        onViewCreated(dialog, view)

        return dialog
    }

    abstract fun onViewCreated(dialog: Dialog, view: View)
}

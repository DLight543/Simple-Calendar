package com.simplemobiletools.calendar.pro.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.NumberPicker
import com.simplemobiletools.calendar.pro.R

class NumberPickerDialog(
    context: Context,
    minValue: Int,
    maxValue: Int,
    currentValue: Int,
    private val callback: (Int) -> Unit
) : AlertDialog(context) {

    private val numberPicker: NumberPicker

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null)
        numberPicker = view.findViewById(R.id.number_picker)

        numberPicker.minValue = minValue
        numberPicker.maxValue = maxValue
        numberPicker.value = currentValue

        setView(view)
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            callback(numberPicker.value)
        }
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ -> }
    }
}

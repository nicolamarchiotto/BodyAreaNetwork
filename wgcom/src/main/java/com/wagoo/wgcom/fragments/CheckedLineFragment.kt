package com.wagoo.wgcom.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wagoo.wgcom.R
import kotlinx.android.synthetic.main.checked_line.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

enum class CheckedStatus {
    Waiting,
    InProgress,
    Done
}

class CheckedLineFragment(description: String) : Fragment() {


    var description = description
        set(value) {
            GlobalScope.launch(Dispatchers.Main) {
                descriptionText?.text = value
            }
            field = value
        }

    var value = "waiting"
        set(value) {
            GlobalScope.launch(Dispatchers.Main) {
                valueText?.text = value
            }
            field = value
        }

    var checkedStatus: CheckedStatus = CheckedStatus.Waiting
        set(value) {
            field = value
            GlobalScope.launch(Dispatchers.Main) {
                when (field) {
                    CheckedStatus.Waiting -> {
                        this@CheckedLineFragment.value = "waiting"
                        progressBar.visibility = View.VISIBLE
                        checkedBar.uncheck()
                    }
                    CheckedStatus.InProgress -> {
                        this@CheckedLineFragment.value = "in progress..."
                        progressBar.visibility = View.VISIBLE
                        checkedBar.uncheck()
                    }
                    CheckedStatus.Done -> {
                        this@CheckedLineFragment.value = "done."
                        progressBar.visibility = View.INVISIBLE
                        checkedBar.check()
                    }
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.checked_line, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        description = description
        value = value
        checkedStatus = checkedStatus
    }

}
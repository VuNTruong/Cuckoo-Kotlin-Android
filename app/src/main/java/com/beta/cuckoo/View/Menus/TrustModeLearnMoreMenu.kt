package com.beta.cuckoo.View.Menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beta.cuckoo.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TrustModeLearnMoreMenu : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // Return the view
        return inflater.inflate(R.layout.trust_mode_learn_more, container, false)
    }
}
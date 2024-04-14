package org.citra.citra_emu.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import org.citra.citra_emu.NativeLibrary
import org.citra.citra_emu.R

class TweaksDialog(context: Context) : BaseSheetDialog(context) {

    private lateinit var adapterId: SettingsAdapter

    companion object {
        // tweaks
        const val SETTING_CORE_TICKS_HACK = 0
        const val SETTING_SKIP_SLOW_DRAW = 1
        const val SETTING_SKIP_TEXTURE_COPY = 2

        // view type
        const val TYPE_SWITCH = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_tweaks)

        val recyclerView: RecyclerView = findViewById(R.id.list_settings)
        recyclerView.layoutManager = LinearLayoutManager(getContext())
        adapterId = SettingsAdapter(getContext())
        recyclerView.adapter = adapterId
        recyclerView.addItemDecoration(DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL))
    }

    inner class SettingsItem(
        private val settingId: Int,
        private val nameId: String,
        private val typeId: Int,
        private var valueId: Int
    ) {
        fun getType(): Int {
            return typeId
        }

        fun getSetting(): Int {
            return settingId
        }

        fun getName(): String {
            return nameId
        }

        fun getValue(): Int {
            return valueId
        }

        fun setValue(value: Int) {
            valueId = value
        }
    }

    abstract class SettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
            findViews(itemView)
        }

        protected abstract fun findViews(root: View)
        abstract fun bind(item: SettingsItem)
        override fun onClick(clicked: View) {
            // handle click event
        }
    }

    inner class SwitchSettingViewHolder(itemView: View) : SettingViewHolder(itemView), CompoundButton.OnCheckedChangeListener {
        private var itemId: SettingsItem? = null
        private var textSettingNameId: TextView? = null
        private var switchId: MaterialSwitch? = null

        init {
            findViews(itemView)
        }

        override fun findViews(root: View) {
            textSettingNameId = root.findViewById(R.id.text_setting_name)
            switchId = root.findViewById(R.id.switch_widget)
            switchId?.setOnCheckedChangeListener(this)
        }

        override fun bind(item: SettingsItem) {
            itemId = item
            textSettingNameId?.text = item.getName()
            switchId?.isChecked = item.getValue() > 0
        }

        override fun onClick(clicked: View) {
            switchId?.toggle()
            itemId?.setValue(if (switchId?.isChecked == true) 1 else 0)
        }

        override fun onCheckedChanged(view: CompoundButton, isChecked: Boolean) {
            itemId?.setValue(if (isChecked) 1 else 0)
        }
    }

    inner class SettingsAdapter(ctx:Context) : RecyclerView.Adapter<SettingViewHolder>() {
        private var tweaksId: IntArray
        private var settingsId: ArrayList<SettingsItem>

        init {
            var i = 0
            tweaksId = NativeLibrary.getTweaksDialogSettings()
            settingsId = ArrayList()

            // native settings
            settingsId.add(SettingsItem(SETTING_CORE_TICKS_HACK, ctx.getString(R.string.setting_core_ticks_hack), TYPE_SWITCH, tweaksId[i++]))
            settingsId.add(SettingsItem(SETTING_SKIP_SLOW_DRAW, ctx.getString(R.string.setting_skip_slow_draw), TYPE_SWITCH, tweaksId[i++]))
            settingsId.add(SettingsItem(SETTING_SKIP_TEXTURE_COPY, ctx.getString(R.string.setting_skip_texture_copy), TYPE_SWITCH, tweaksId[i++]))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_SWITCH -> {
                    val itemView = inflater.inflate(R.layout.list_item_ingame_switch, parent, false)
                    SwitchSettingViewHolder(itemView)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun getItemCount(): Int {
            return settingsId.size
        }

        override fun getItemViewType(position: Int): Int {
            return settingsId[position].getType()
        }

        override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
            holder.bind(settingsId[position])
        }

        fun saveSettings() {
            // native settings
            var isChanged = false
            val new_settings = IntArray(tweaksId.size)
            for (i in tweaksId.indices) {
                new_settings[i] = settingsId[i].getValue()
                if (new_settings[i] != tweaksId[i]) {
                    isChanged = true
                }
            }
            // apply settings if changes are detected
            if (isChanged) {
                NativeLibrary.setTweaksDialogSettings(new_settings)
            }
        }
    }
}

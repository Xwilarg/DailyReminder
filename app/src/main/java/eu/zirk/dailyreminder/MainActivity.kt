package eu.zirk.dailyreminder

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var calendarView: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            calendarView = v.findViewById<CalendarView>(R.id.calendarView)
            val today = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
                // Called only when a new container is needed.
                override fun create(view: View) = DayViewContainer(view)

                // Called every time we need to reuse a container.
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.textView.text = data.date.dayOfMonth.toString()

                    val preferences = applicationContext.getSharedPreferences("progress", Context.MODE_PRIVATE)
                    val dates = preferences.getStringSet("productMain", emptySet())
                    val isOk = dates!!.contains(data.date.toString())

                    if (data.position == DayPosition.MonthDate) {

                        if (today.year == data.date.year && today.monthValue == data.date.monthValue && today.dayOfMonth == data.date.dayOfMonth) {
                            container.textView.setBackgroundColor(if (isOk) {
                                applicationContext.getColor(R.color.day_validation)
                            } else {
                                Color.GRAY
                            })
                            container.textView.setTextColor(Color.WHITE)
                        } else {
                            container.textView.setTextColor(if (isOk) {
                                Color.GREEN
                            } else {
                                Color.BLACK
                            })
                        }
                    } else {
                        container.textView.setTextColor(if (isOk) {
                            applicationContext.getColor(R.color.day_validation)
                        } else {
                            Color.GRAY
                        })
                    }
                }
            }

            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth.minusMonths(10)
            val lastMonth = currentMonth.plusMonths(10)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
            calendarView.scrollToMonth(currentMonth)

            val preferences = applicationContext.getSharedPreferences("progress", Context.MODE_PRIVATE)
            val dates = preferences.getStringSet("productMain", emptySet())

            if (dates!!.contains(LocalDate.now().toString())) {
                val btn = v.findViewById<Button>(R.id.validate)
                btn.isEnabled = false
                btn.text = applicationContext.getString(R.string.validate_already_done)
            }

            insets
        }
    }

    fun validateToday(v: View) {
        val preferences = applicationContext.getSharedPreferences("progress", Context.MODE_PRIVATE)
        val dates = preferences.getStringSet("productMain", emptySet())
        val today = LocalDate.now().toString()

        AlertDialog.Builder(this)
            .setMessage(applicationContext.getString(R.string.validate_popup))
            .setPositiveButton(R.string.yes, DialogInterface.OnClickListener() { dialog, which ->
                with (preferences.edit()) {
                    val tmp = dates!!.toMutableSet()
                    tmp.add(today)
                    putStringSet("productMain", tmp)
                    apply()
                }
                calendarView.notifyDateChanged(LocalDate.now())
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }
}
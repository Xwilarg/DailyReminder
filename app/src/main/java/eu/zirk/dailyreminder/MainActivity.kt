package eu.zirk.dailyreminder

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            val calendarView = v.findViewById<CalendarView>(R.id.calendarView)
            val today = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
                // Called only when a new container is needed.
                override fun create(view: View) = DayViewContainer(view)

                // Called every time we need to reuse a container.
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.textView.text = data.date.dayOfMonth.toString()
                    if (data.position == DayPosition.MonthDate) {

                        val preferences = applicationContext.getSharedPreferences("progress", Context.MODE_PRIVATE)
                        val dates = preferences.getStringSet("productMain", emptySet())
                        val isOk = dates!!.contains(data.date.toString())

                        if (today.year == data.date.year && today.monthValue == data.date.monthValue && today.dayOfMonth == data.date.dayOfMonth) {
                            container.textView.textSize = 25F
                        }
                        container.textView.setTextColor(if (isOk) {
                            Color.GREEN
                        } else {
                            Color.BLACK
                        })
                    } else {
                        container.textView.setTextColor(Color.GRAY)
                    }
                }
            }

            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth.minusMonths(10)
            val lastMonth = currentMonth.plusMonths(10)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
            calendarView.scrollToMonth(currentMonth)

            insets
        }
    }
}
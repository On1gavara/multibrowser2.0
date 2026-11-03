// ВАЖНО: первая строка (package) должна совпадать с пакетом твоего проекта.
// Если Android Studio создала другой пакет — оставь свою строку package, а всё остальное замени.
package com.example.multibrowser

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

// Одно "окно браузера" = контейнер (адресная строка + сама страница).
// data class — удобный способ хранить несколько связанных значений вместе.
data class Pane(val layout: LinearLayout, val web: WebView)

class MainActivity : AppCompatActivity() {

    // Контейнер, в котором лежат все окна браузера.
    private lateinit var container: LinearLayout

    // Список всех созданных окон — нужен, чтобы добавлять/удалять и чистить память.
    private val panes = mutableListOf<Pane>()

    // WebView, которого пользователь трогал последним — ему адресуется кнопка "Назад".
    private var lastTouched: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Корневой layout: сверху панель кнопок, ниже — контейнер с окнами.
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true // чтобы контент не залезал под статус-бар
        }

        // ---------- Панель управления ----------
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val addBtn = Button(this).apply {
            text = "+ окно"
            setOnClickListener { addPane() }
        }
        val removeBtn = Button(this).apply {
            text = "− окно"
            setOnClickListener { removePane() }
        }
        val rotateBtn = Button(this).apply {
            text = "↔ / ↕"
            setOnClickListener { toggleOrientation() }
        }
        bar.addView(addBtn)
        bar.addView(removeBtn)
        bar.addView(rotateBtn)

        // ---------- Контейнер окон ----------
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            // weight = 1 -> контейнер занимает всё оставшееся место под панелью
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
        }

        root.addView(bar)
        root.addView(container)
        setContentView(root)

        // Стартуем с двух окон.
        repeat(2) { addPane() }

        // ---------- Кнопка "Назад" ----------
        // Если в последнем тронутом окне есть история — идём назад в нём,
        // иначе закрываем приложение как обычно.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val web = lastTouched
                if (web != null && web.canGoBack()) {
                    web.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // Создаёт одно окно браузера и добавляет его в контейнер.
    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun addPane() {
        if (panes.size >= 6) return // ограничение, чтобы телефон не умер от нагрузки

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Строка: поле адреса + кнопка "Go".
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val urlField = EditText(this).apply {
            setText("https://www.google.com")
            setSingleLine()
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO // кнопка "Перейти" на клавиатуре
            // weight = 1 -> поле растягивается, кнопка остаётся справа
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        }
        val goBtn = Button(this).apply { text = "Go" }

        row.addView(urlField)
        row.addView(goBtn)

        // Сама страница. У каждого WebView своя история и своё состояние,
        // поэтому окна независимы и работают одновременно.
        val web = WebView(this).apply {
            settings.javaScriptEnabled = true  // без JS большинство сайтов не работает
            settings.domStorageEnabled = true  // localStorage, нужен многим сайтам

            // WebViewClient заставляет открывать ссылки ВНУТРИ WebView,
            // а не выкидывать пользователя в внешний браузер.
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Когда страница загрузилась — показываем её реальный адрес в поле.
                    if (url != null) urlField.setText(url)
                }
            }

            // Запоминаем, какого окна пользователь касался последним (для "Назад").
            // Возвращаем false, чтобы касание продолжало обрабатываться страницей.
            setOnTouchListener { _, _ ->
                lastTouched = this
                false
            }
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
        }

        // Общая функция перехода по адресу.
        val go = {
            var url = urlField.text.toString().trim()
            // Если пользователь не написал "https://", добавляем сами.
            if (!url.contains("://")) url = "https://$url"
            web.loadUrl(url)
        }
        goBtn.setOnClickListener { go() }
        urlField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) { go(); true } else false
        }

        layout.addView(row)
        layout.addView(web)

        panes.add(Pane(layout, web))
        container.addView(layout)
        applyOrientation()
        go() // сразу открываем стартовую страницу
    }

    // Удаляет последнее окно.
    private fun removePane() {
        if (panes.size <= 1) return // минимум одно окно оставляем
        val pane = panes.removeAt(panes.lastIndex)
        container.removeView(pane.layout)
        pane.web.destroy() // освобождаем память — важно для WebView
        applyOrientation()
    }

    // Переключает расположение окон: друг под другом <-> рядом.
    private fun toggleOrientation() {
        container.orientation =
            if (container.orientation == LinearLayout.VERTICAL) LinearLayout.HORIZONTAL
            else LinearLayout.VERTICAL
        applyOrientation()
    }

    // Подгоняет размеры окон под текущую ориентацию.
    // Вертикально: ширина на весь экран, высота делится поровну (weight).
    // Горизонтально: наоборот.
    private fun applyOrientation() {
        val vertical = container.orientation == LinearLayout.VERTICAL
        for (pane in panes) {
            pane.layout.layoutParams = if (vertical)
                LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
            else
                LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
        }
    }

    // Когда приложение закрывается — уничтожаем все WebView, чтобы не было утечек памяти.
    override fun onDestroy() {
        panes.forEach { it.web.destroy() }
        super.onDestroy()
    }
}

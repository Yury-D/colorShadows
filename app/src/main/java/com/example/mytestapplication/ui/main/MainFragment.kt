package com.example.mytestapplication.ui.main

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mytestapplication.R
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ждем когда вьюха отрисуется чтобы узнать ее размеры
        targetView.doOnNextLayout {
            val colors = intArrayOf(
                Color.WHITE,
                Color.RED,
                Color.WHITE
            )
            val cornerRadius = 16f.dp
            val padding = 30.dp
            val centerX = it.width.toFloat() / 2 - padding
            val centerY = it.height.toFloat() / 2 - padding

            val shadowDrawable = createShadowDrawable(
                colors = colors,
                cornerRadius = cornerRadius,
                elevation = padding / 2f,
                centerX = centerX,
                centerY = centerY
            )
            val colorDrawable = createColorDrawable(
                backgroundColor = Color.DKGRAY,
                cornerRadius = cornerRadius
            )

            it.setColorShadowBackground(
                shadowDrawable = shadowDrawable,
                colorDrawable = colorDrawable,
                padding = 30.dp
            )

            // Второй массив с цветами. Размер массивов должен быть одинаковый.
            val endColors = intArrayOf(
                Color.RED,
                Color.WHITE,
                Color.RED
            )

            animateShadow(
                shapeDrawable = shadowDrawable,
                startColors = colors,
                endColors = endColors,
                duration = 2000,
                centerX = centerX,
                centerY = centerY
            )
        }

    }

    /**
     * Создание drawable с градиентом-тенью
     */
    private fun createShadowDrawable(
        @ColorInt colors: IntArray,
        cornerRadius: Float,
        elevation: Float,
        centerX: Float,
        centerY: Float
    ): ShapeDrawable {

        val shadowDrawable = ShapeDrawable()

        // Устанавливаем черную тень по умолчанию
        shadowDrawable.paint.setShadowLayer(
            elevation, // размер тени
            0f, // смещение тени по оси Х
            0f, // по У
            Color.BLACK // цвет тени
        )

        /**
         * Применяем покраску градиентом
         *
         * @param centerX - Центр SweepGradient по оси Х. Берем центр вьюхи
         * @param centerY - Центр по оси У
         * @param colors - Цвета градиента. Последний цвет должен быть равен первому,
         * иначе между ними не будет плавного перехода
         * @param position - позиции смещения градиента одного цвета относительно другого от 0 до 1.
         * В нашем случае null т.к. нам нужен равномерный градиент
         */
        shadowDrawable.paint.shader = SweepGradient(
            centerX,
            centerY,
            colors,
            null
        )

        // Делаем закугление углов
        val outerRadius = FloatArray(8) { cornerRadius }
        shadowDrawable.shape = RoundRectShape(outerRadius, null, null)

        return shadowDrawable
    }

    /**
     * Создание цветного drawable с закругленными углами
     * Это будет основной цвет нашего контейнера
     */
    private fun createColorDrawable(
        @ColorInt backgroundColor: Int,
        cornerRadius: Float
    ) = GradientDrawable().apply {
        setColor(backgroundColor)
        setCornerRadius(cornerRadius)
    }

    /**
     * Устанавливаем бэкграунд с тенью на вьюху, учитывая padding
     */
    private fun View.setColorShadowBackground(
        shadowDrawable: ShapeDrawable,
        colorDrawable: Drawable,
        padding: Int
    ) {
        val drawable = LayerDrawable(arrayOf(shadowDrawable, colorDrawable))
        drawable.setLayerInset(0, padding, padding, padding, padding)
        drawable.setLayerInset(1, padding, padding, padding, padding)
        setPadding(padding, padding, padding, padding)
        background = drawable
    }

    /**
     * Анимация drawable-градиента
     */
    private fun animateShadow(
        shapeDrawable: ShapeDrawable,
        @ColorInt startColors: IntArray,
        @ColorInt endColors: IntArray,
        duration: Long,
        centerX: Float,
        centerY: Float
    ) {
        /**
         * Меняем значение с 0f до 1f для применения плавного изменения
         * цвета с помощью [ColorUtils.blendARGB]
         */
        ValueAnimator.ofFloat(0f, 1f).apply {
            // Задержка перерисовки тени. Грубо говоря, фпс анимации
            val invalidateDelay = 100
            var deltaTime = System.currentTimeMillis()

            // Новый массив со смешанными цветами
            val mixedColors = IntArray(startColors.size)

            addUpdateListener { animation ->
                if (System.currentTimeMillis() - deltaTime > invalidateDelay) {
                    val animatedFraction = animation.animatedValue as Float
                    deltaTime = System.currentTimeMillis()

                    // Смешиваем цвета
                    for (i in 0..mixedColors.lastIndex) {
                        mixedColors[i] =
                            ColorUtils.blendARGB(startColors[i], endColors[i], animatedFraction)
                    }

                    // Устанавливаем новую тень
                    shapeDrawable.paint.shader = SweepGradient(
                        centerX,
                        centerY,
                        mixedColors,
                        null
                    )
                    shapeDrawable.invalidateSelf()
                }
            }
            repeatMode = ValueAnimator.REVERSE
            repeatCount = Animation.INFINITE
            setDuration(duration)
            start()
        }
    }

}